/*
 * generated by Xtext 2.10.0
 */
package edu.kit.ipd.sdq.kamp.ruledsl.ui.contentassist

import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.Assignment
import org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext
import org.eclipse.xtext.ui.editor.contentassist.ICompletionProposalAcceptor
import edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.ModificationMark
import org.eclipse.xtext.common.types.JvmGenericType
import org.eclipse.xtext.common.types.JvmMember
import org.eclipse.xtext.RuleCall
import org.eclipse.xtext.CrossReference
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.common.types.access.IJvmTypeProvider
import com.google.inject.Inject
import org.eclipse.xtext.common.types.JvmType
import org.eclipse.emf.ecore.EFactory
import org.eclipse.xtext.common.types.xtext.ui.TypeMatchFilters
import org.eclipse.xtext.common.types.xtext.ui.ITypesProposalProvider
import edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.KampRuleLanguageFactory
import edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.KampRuleLanguagePackage
import org.eclipse.emf.ecore.EcoreFactory
import edu.kit.ipd.sdq.kamp.model.modificationmarks.ChangePropagationStep
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.search.IJavaSearchConstants;

/**
 * See https://www.eclipse.org/Xtext/documentation/304_ide_concepts.html#content-assist
 * on how to customize the content assistant.
 */
class KampRuleLanguageProposalProvider extends AbstractKampRuleLanguageProposalProvider {
	
	@Inject
    private IJvmTypeProvider.Factory jvmTypeProviderFactory;
    
    @Inject
    private ITypesProposalProvider typeProposalProvider;
	
	override completeModificationMark_MemberRef(EObject model, Assignment assignment, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
		super.completeRuleCall(assignment.getTerminal() as RuleCall, context, acceptor);
		
		if(model instanceof ModificationMark) {
			if(model.getType() !== null && model.getType() instanceof JvmGenericType) {
				val JvmGenericType jvmGenType = model.getType() as JvmGenericType

				for(JvmMember jvmMember : jvmGenType.getMembers()) {
					if(jvmMember.getSimpleName().startsWith("create"))
						acceptor.accept(createCompletionProposal(jvmMember.getSimpleName(), context)); 
				}
			}
		}
	}
	
	override completeModificationMark_Type(EObject model, Assignment assignment, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
//		lookupCrossReference(assignment.getTerminal() as CrossReference, 
//	      context, acceptor, [
//	      	if(model instanceof ModificationMark) {
//	      		val aa = it.EObjectOrProxy
//	      		println(it.EObjectOrProxy)
//	      	} 
//	        true
//	      ]);
//	      

	    if (EcoreUtil2.getContainerOfType(model, ModificationMark) !== null) {
            val IJvmTypeProvider jvmTypeProvider = jvmTypeProviderFactory.createTypeProvider(model.eContainer.eResource().getResourceSet());
            val JvmType interfaceToImplement = jvmTypeProvider.findTypeByName(EFactory.getName());
            typeProposalProvider.createSubTypeProposals(interfaceToImplement, this, context, KampRuleLanguagePackage.Literals.MODIFICATION_MARK__TYPE, new IsInterface(), acceptor);
        } else {
            super.completeJvmParameterizedTypeReference_Type(model, assignment, context, acceptor);
        }
	}
	
	override completeModificationMark_Target(EObject model, Assignment assignment, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
		if (EcoreUtil2.getContainerOfType(model, ModificationMark) !== null) {
            val IJvmTypeProvider jvmTypeProvider = jvmTypeProviderFactory.createTypeProvider(model.eContainer.eResource().getResourceSet());
            val JvmType interfaceToImplement = jvmTypeProvider.findTypeByName(ChangePropagationStep.getName());
            typeProposalProvider.createSubTypeProposals(interfaceToImplement, this, context, KampRuleLanguagePackage.Literals.MODIFICATION_MARK__TYPE, new IsInterface(), acceptor);
        } else {
            super.completeJvmParameterizedTypeReference_Type(model, assignment, context, acceptor);
        }
	}
	
	override completeModificationMark_TargetMethod(EObject model, Assignment assignment, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
		super.completeRuleCall(assignment.getTerminal() as RuleCall, context, acceptor);
		
		if(model instanceof ModificationMark) {
			if(model.target !== null && model.target instanceof JvmGenericType) {
				val JvmGenericType jvmGenType = model.target as JvmGenericType

				for(JvmMember jvmMember : jvmGenType.getMembers()) {
					if(jvmMember.getSimpleName().startsWith("get"))
						acceptor.accept(createCompletionProposal(jvmMember.getSimpleName(), context)); 
				}
			}
		}
	}

	public static class IsInterface implements ITypesProposalProvider.Filter {
		override accept(int modifiers, char[] packageName, char[] simpleTypeName,
				char[][] enclosingTypeNames, String path) {

			return Flags.isInterface(modifiers);
		}
		
		override getSearchFor() {
			return IJavaSearchConstants.INTERFACE;
		}
	}	
}
