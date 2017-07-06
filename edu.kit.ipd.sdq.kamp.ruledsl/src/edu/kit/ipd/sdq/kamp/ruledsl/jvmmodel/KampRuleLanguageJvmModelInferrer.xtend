/*
 * generated by Xtext 2.10.0
 */
package edu.kit.ipd.sdq.kamp.ruledsl.jvmmodel

import com.google.inject.Inject
import edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.BackwardEReference
import edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.ForwardEReference
import edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.KampRule
import edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.Lookup
import edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.RuleFile
import edu.kit.ipd.sdq.kamp.ruledsl.util.EcoreUtil
import java.util.Collections
import java.util.Map
import java.util.Set
import java.util.stream.Collectors
import java.util.stream.Stream
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.common.types.JvmGenericType
import org.eclipse.xtext.xbase.jvmmodel.AbstractModelInferrer
import org.eclipse.xtext.xbase.jvmmodel.IJvmDeclaredTypeAcceptor
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder

import static edu.kit.ipd.sdq.kamp.ruledsl.util.EcoreUtil.*

import static extension edu.kit.ipd.sdq.kamp.ruledsl.util.KampRuleLanguageEcoreUtil.*

/**
 * <p>Infers a JVM model from the source model.</p> 
 *
 * <p>The JVM model should contain all elements that would appear in the Java code 
 * which is generated from the source model. Other models link against the JVM model rather than the source model.</p>     
 */
class KampRuleLanguageJvmModelInferrer extends AbstractModelInferrer {

	/**
	 * convenience API to build and initialize JVM types and their members.
	 */
	@Inject extension JvmTypesBuilder

	/** associates a variable name with a {@link Lookup} */
	private Map<Lookup, String> nameForLookup;
	
	/**
	 * The dispatch method {@code infer} is called for each instance of the
	 * given element's type that is contained in a resource.
	 * 
	 * @param element
	 *            the model to create one or more
	 *            {@link JvmDeclaredType declared
	 *            types} from.
	 * @param acceptor
	 *            each created
	 *            {@link JvmDeclaredType type}
	 *            without a container should be passed to the acceptor in order
	 *            get attached to the current resource. The acceptor's
	 *            {@link IJvmDeclaredTypeAcceptor#accept(org.eclipse.xtext.common.types.JvmDeclaredType)
	 *            accept(..)} method takes the constructed empty type for the
	 *            pre-indexing phase. This one is further initialized in the
	 *            indexing phase using the lambda you pass as the last argument.
	 * @param isPreIndexingPhase
	 *            whether the method is called in a pre-indexing phase, i.e.
	 *            when the global index is not yet fully updated. You must not
	 *            rely on linking using the index if isPreIndexingPhase is
	 *            <code>true</code>.
	 */
	def dispatch void infer(RuleFile element, IJvmDeclaredTypeAcceptor acceptor, boolean isPreIndexingPhase) {
		acceptor.accept(element.toClass(element.name),
			[ theClass |
				nameForLookup = newHashMap
				
				theClass.members += element.rules.map [ rule |
					rule.toMethod(rule.lookupMethodName, typeRef(Set, typeRef(rule.returnType.instanceTypeName))) [
						parameters += rule.toParameter(rule.source.metaclass.name.toFirstLower, typeRef(rule.source.metaclass.instanceTypeName))
						
						nameForLookup.put(null, "input")
						body = '''
							«typeRef(Set, typeRef(Resource))» allResources = «Collections».emptySet();
	
							«typeRef(Stream, typeRef(rule.source.metaclass.instanceTypeName))» input =
								«typeRef(Stream)».of(«rule.source.metaclass.name.toFirstLower»);
							
							«FOR x : rule.lookups»
								«x.generateCodeForRule(theClass)»
							«ENDFOR»
							
							return «nameForLookup.get(rule.lookups.last)».collect(«typeRef(Collectors)».toSet());
						'''
					]
				]
			]);
	}
	
	
	def EClass getReturnType(KampRule rule) {
		return rule.lookups.last.getMetaclass
	}
	
	def String getLookupMethodName(KampRule rule)
		'''lookup«rule.name.toFirstUpper»From«rule.source.metaclass.name.toFirstUpper»'''
	
	
	/**
	 * <p>Generates the code that is embedded in the method if the given lookup
	 * is found in a chain of lookups of one rule.
	 * 
	 * <p>The rule file can be navigated using the given lookup and methods in
	 * {@link EcoreUtil}.
	 * 
	 * @param lookup the Lookup to generate the code for. There should be a
	 * dispatch method for every sub type. Otherwise the generator will throw
	 * a runtime exception.
	 * 
	 * @param typeToAddTo the class that is currently generated. Can be used to add further
	 * methods or fields with the injected extension {@link JvmTypesBuilder}.
	 */	
	def dispatch generateCodeForRule(Lookup lookup, JvmGenericType typeToAddTo) {
		'''// rule: «lookup?.toString», pre: «getPreviousSiblingOfType(lookup, Lookup)?.toString»'''
	}
	
	/**
	 * @see #generateCodeForRule(Lookup, JvmGenericType)
	 */
	def dispatch generateCodeForRule(ForwardEReference ref, JvmGenericType typeToAddTo) {
		var varName = '''marked«ref.metaclass.name.toFirstUpper»'''
		nameForLookup.put(ref, varName)
		'''
			Stream<«ref.metaclass.instanceTypeName»> «varName» = «nameForLookup.get(getPreviousSiblingOfType(ref, Lookup))»
				«IF ref.feature.many»
					.flatMap(it -> 
						it.get«ref.feature.name.toFirstUpper»().stream());
				«ELSE»
					.map(it -> it.get«ref.feature.name.toFirstUpper()»());
				«ENDIF»
		'''
	}

	/**
	 * @see #generateCodeForRule(Lookup, JvmGenericType)
	 */
	def dispatch generateCodeForRule(BackwardEReference ref, JvmGenericType typeToAddTo) {
		var varName = '''backmarked«ref.metaclass.name.toFirstUpper»'''
		nameForLookup.put(ref, varName)
		
		'''
			Stream<«ref.metaclass.instanceTypeName»> «varName» = null;
			// iterate over all resources and filter stuff out
		'''
	}
}
