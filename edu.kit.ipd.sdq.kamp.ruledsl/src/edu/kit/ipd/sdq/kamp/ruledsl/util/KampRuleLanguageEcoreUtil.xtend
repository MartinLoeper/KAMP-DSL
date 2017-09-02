package edu.kit.ipd.sdq.kamp.ruledsl.util

import edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.BackwardEReference
//import edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.CollectElements
import edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.ForwardEReference
import edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.Lookup
import edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.PropagationReference
import org.eclipse.emf.ecore.EClass
import tools.vitruv.dsls.mirbase.mirBase.MetaclassReference
import static edu.kit.ipd.sdq.kamp.ruledsl.util.EcoreUtil.*
import edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.RuleReference

final class KampRuleLanguageEcoreUtil {
	private new() {
		
	}
	
	/**
	 * <p>Returns the Metaclass ({@link EClass}) that is associated with a
	 * grammar element.
	 * 
	 * <p>The 'associated' Metaclass is the class referenced by a {@link MetaclassReference},
	 * or the type of the (set of) Metaclass(es) returned by a {@link PropagationReference}. 
	 */
	def static dispatch EClass getMetaclass(ForwardEReference ref) {
		ref.feature.EType as EClass
	}
	
	/** see #getMetaclass(ForwardEReference) */
	def static dispatch EClass getMetaclass(MetaclassReference ref) {
		ref.metaclass
	}
	
	def static dispatch EClass getMetaclass(BackwardEReference ref) {
		// TODO is this cast risky?? which of those subclasses of eclassifier is possible? EClassifierImpl, EClassImpl, EDataTypeImpl, EEnumImpl
		ref.mclass.metaclass as EClass;
	}
	
	def static dispatch EClass getMetaclass(RuleReference ref) {
		val Lookup lastLookup = ref.rule.instructions.filter[i | i instanceof Lookup].map(i | Lookup.cast(i)).last;
		getMetaclass(lastLookup);
	}
	
	/**
	 * Returns the metaclass that is previous to the given {@link PropagationReference}.
	 */
	def static getPreviousMetaclass(Lookup ref) {
		getMetaclass(
			getPreviousSiblingOfType(ref, MetaclassReference, Lookup)
		)
	}	
}