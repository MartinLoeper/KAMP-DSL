package edu.kit.ipd.sdq.kamp.ruledsl.scoping

import edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.BackwardEReference
import edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.KampRule
import edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.Lookup
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EReference
import org.eclipse.xtext.resource.EObjectDescription
import org.eclipse.xtext.scoping.IScope
import tools.vitruv.dsls.mirbase.scoping.MirBaseScopeProviderDelegate

import static edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.KampRuleLanguagePackage.Literals.*
import static tools.vitruv.dsls.mirbase.mirBase.MirBasePackage.Literals.*

import static extension edu.kit.ipd.sdq.kamp.ruledsl.util.KampRuleLanguageEcoreUtil.*

class KampRuleLanguageScopeProviderDelegate extends MirBaseScopeProviderDelegate {
	override getScope(EObject context, EReference reference) {
		if (context instanceof KampRule && reference.equals(METACLASS_REFERENCE__METACLASS))
			return IScope.NULLSCOPE
		else if (context instanceof BackwardEReference && reference.equals(LOOKUP))
			return createFilteredEReferenceScope((context as BackwardEReference)?.metaclass,
				(context as Lookup)?.previousMetaclass)
		else if (context instanceof Lookup && reference.equals(LOOKUP))
			return createEReferenceScope((context as Lookup)?.previousMetaclass)

		return super.getScope(context, reference)
	}
	
	/**
	 * Creates a scope for {@link EReference EReferences} of {@source sourceEClass}.
	 * 
	 * TODO: move to MirBaseScopeProviderDelegate
	 */
	override createEReferenceScope(EClass sourceEClass) {
		if (sourceEClass != null) {
			createScope(IScope.NULLSCOPE, sourceEClass.EAllReferences.iterator, [
				EObjectDescription.create(it.name, it)
			])
		} else {
			return IScope.NULLSCOPE
		}
	}

	/**
	 * Creates a scope for {@link EReference EReferences} of {@source sourceEClass}
	 * that have the type {@source targetEClass} as their {@link EReference#getEReferenceType target type}.
	 * 
	 * TODO: move to MirBaseScopeProviderDelegate
	 */
	def createFilteredEReferenceScope(EClass sourceEClass, EClass targetEClass) {
		if (sourceEClass != null && targetEClass != null) {
			createScope(IScope.NULLSCOPE, sourceEClass.EAllReferences.filter[
				EReferenceType.isSuperTypeOf(targetEClass)
			].iterator, [
				EObjectDescription.create(it.name, it)
			])
		} else {
			return IScope.NULLSCOPE
		}
	}
}