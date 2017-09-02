
package edu.kit.ipd.sdq.kamp.ruledsl.scoping

import edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.BackwardEReference
import edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.KampRule
import edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.Lookup
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EClassifier
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EReference
import org.eclipse.xtext.resource.EObjectDescription
import org.eclipse.xtext.scoping.IScope
import tools.vitruv.dsls.mirbase.mirBase.MetaclassReference
import tools.vitruv.dsls.mirbase.scoping.MirBaseScopeProviderDelegate

import static tools.vitruv.dsls.mirbase.mirBase.MirBasePackage.Literals.*

import static extension edu.kit.ipd.sdq.kamp.ruledsl.util.KampRuleLanguageEcoreUtil.*
import tools.vitruv.dsls.mirbase.mirBase.MetamodelImport
import org.eclipse.emf.ecore.EcorePackage
import org.eclipse.xtext.scoping.impl.SimpleScope

class KampRuleLanguageScopeProviderDelegate extends MirBaseScopeProviderDelegate {
	override getScope(EObject context, EReference reference) {
		if (context instanceof KampRule && reference.equals(METACLASS_REFERENCE__METACLASS))
			return IScope.NULLSCOPE 
		else if (context instanceof BackwardEReference && reference.class.isAssignableFrom(METACLASS_REFERENCE__METACLASS.class))
			return createFilteredEReferenceScope((context as BackwardEReference)?.mclass, 
				(context as Lookup)?.previousMetaclass) 
		else if (context instanceof Lookup && reference.class.isAssignableFrom(METACLASS_REFERENCE__METACLASS.class)) {
			return createEReferenceScope((context as Lookup).previousMetaclass)
		} 
//		else if(context instanceof ModificationMark && reference == KampRuleLanguagePackage.Literals.MODIFICATION_MARK__TYPE) {
//			val mm = (context as ModificationMark)
//			if(mm.type !== null && !(mm.type instanceof JvmVoid)) {				
//				if(mm.type instanceof EFactory) {
//					return super.getScope(context, reference)
//				} else {
//					return IScope.NULLSCOPE 
//				}
//			}
//		}

		if (reference.equals(METACLASS_REFERENCE__METACLASS))
			return createQualifiedEClassScope((context as MetaclassReference).metamodel, true, false);
				
		return super.getScope(context, reference)
	}
	
	// copied from MirBaseScopeProviderDelegate
	private def createQualifiedEClassScope(MetamodelImport metamodelImport, boolean includeAbstract, boolean includeEObject) {
		val classifierDescriptions = 
			if (metamodelImport === null || metamodelImport.package === null) {
				if (includeEObject) {
					#[createEObjectDescription(EcorePackage.Literals.EOBJECT, false)];
				} else {
					#[];
				}
			} else { 
				collectObjectDescriptions(metamodelImport.package, 
					true, includeAbstract, metamodelImport.useQualifiedNames)
			}

		var resultScope = new SimpleScope(IScope.NULLSCOPE, classifierDescriptions)
		return resultScope
	}
	
	/**
	 * Creates a scope for {@link EReference EReferences} of {@source sourceEClass}.
	 * 
	 * TODO: move to MirBaseScopeProviderDelegate
	 */
	override createEReferenceScope(EClass eClass) {		
		if (eClass != null) {
			createScope(IScope.NULLSCOPE, eClass.EAllReferences.iterator, [
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
	def createFilteredEReferenceScope(MetaclassReference sourceEClass, EClass targetEClass) {
		val featuresOfSource = sourceEClass.metaclass.EAllStructuralFeatures;
		if (sourceEClass !== null && targetEClass !== null) {
			createScope(IScope.NULLSCOPE, featuresOfSource.filter[feature | feature.EType.isSubtype(targetEClass)].iterator, [
				EObjectDescription.create(it.name, it)
			])
		} else {
			return IScope.NULLSCOPE
		}
	}
	
	// TODO check again if this is correct!
	def Boolean isSubtype(EClassifier superType, EClass subType) {
		if(subType.equals(superType)) {
			return true
		}
		
		return subType.EAllSuperTypes.exists[sType | sType.equals(subType)]
	}
}