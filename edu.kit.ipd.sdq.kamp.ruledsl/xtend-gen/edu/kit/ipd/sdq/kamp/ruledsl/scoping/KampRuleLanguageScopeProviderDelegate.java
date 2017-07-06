package edu.kit.ipd.sdq.kamp.ruledsl.scoping;

import com.google.common.base.Objects;
import edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.BackwardEReference;
import edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.KampRule;
import edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.KampRuleLanguagePackage;
import edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.Lookup;
import edu.kit.ipd.sdq.kamp.ruledsl.util.KampRuleLanguageEcoreUtil;
import java.util.function.Function;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.xtext.resource.EObjectDescription;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import tools.vitruv.dsls.mirbase.mirBase.MirBasePackage;
import tools.vitruv.dsls.mirbase.scoping.MirBaseScopeProviderDelegate;

@SuppressWarnings("all")
public class KampRuleLanguageScopeProviderDelegate extends MirBaseScopeProviderDelegate {
  @Override
  public IScope getScope(final EObject context, final EReference reference) {
    if (((context instanceof KampRule) && reference.equals(MirBasePackage.Literals.METACLASS_REFERENCE__METACLASS))) {
      return IScope.NULLSCOPE;
    } else {
      if (((context instanceof BackwardEReference) && reference.equals(KampRuleLanguagePackage.Literals.LOOKUP))) {
        EClass _metaclass = null;
        if (((BackwardEReference) context)!=null) {
          _metaclass=((BackwardEReference) context).getMetaclass();
        }
        EClass _previousMetaclass = null;
        if (((Lookup) context)!=null) {
          _previousMetaclass=KampRuleLanguageEcoreUtil.getPreviousMetaclass(((Lookup) context));
        }
        return this.createFilteredEReferenceScope(_metaclass, _previousMetaclass);
      } else {
        if (((context instanceof Lookup) && reference.equals(KampRuleLanguagePackage.Literals.LOOKUP))) {
          EClass _previousMetaclass_1 = null;
          if (((Lookup) context)!=null) {
            _previousMetaclass_1=KampRuleLanguageEcoreUtil.getPreviousMetaclass(((Lookup) context));
          }
          return this.createEReferenceScope(_previousMetaclass_1);
        }
      }
    }
    return super.getScope(context, reference);
  }
  
  /**
   * Creates a scope for {@link EReference EReferences} of {@source sourceEClass}.
   * 
   * TODO: move to MirBaseScopeProviderDelegate
   */
  @Override
  public IScope createEReferenceScope(final EClass sourceEClass) {
    IScope _xifexpression = null;
    boolean _notEquals = (!Objects.equal(sourceEClass, null));
    if (_notEquals) {
      final Function<EReference, IEObjectDescription> _function = (EReference it) -> {
        return EObjectDescription.create(it.getName(), it);
      };
      _xifexpression = this.<EReference>createScope(IScope.NULLSCOPE, sourceEClass.getEAllReferences().iterator(), _function);
    } else {
      return IScope.NULLSCOPE;
    }
    return _xifexpression;
  }
  
  /**
   * Creates a scope for {@link EReference EReferences} of {@source sourceEClass}
   * that have the type {@source targetEClass} as their {@link EReference#getEReferenceType target type}.
   * 
   * TODO: move to MirBaseScopeProviderDelegate
   */
  public IScope createFilteredEReferenceScope(final EClass sourceEClass, final EClass targetEClass) {
    IScope _xifexpression = null;
    if (((!Objects.equal(sourceEClass, null)) && (!Objects.equal(targetEClass, null)))) {
      final Function1<EReference, Boolean> _function = (EReference it) -> {
        return Boolean.valueOf(it.getEReferenceType().isSuperTypeOf(targetEClass));
      };
      final Function<EReference, IEObjectDescription> _function_1 = (EReference it) -> {
        return EObjectDescription.create(it.getName(), it);
      };
      _xifexpression = this.<EReference>createScope(IScope.NULLSCOPE, IterableExtensions.<EReference>filter(sourceEClass.getEAllReferences(), _function).iterator(), _function_1);
    } else {
      return IScope.NULLSCOPE;
    }
    return _xifexpression;
  }
}
