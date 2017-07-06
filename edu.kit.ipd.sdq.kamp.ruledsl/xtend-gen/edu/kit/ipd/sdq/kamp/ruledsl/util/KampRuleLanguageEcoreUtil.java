package edu.kit.ipd.sdq.kamp.ruledsl.util;

import edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.BackwardEReference;
import edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.ForwardEReference;
import edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.Lookup;
import edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.PropagationReference;
import edu.kit.ipd.sdq.kamp.ruledsl.util.EcoreUtil;
import java.util.Arrays;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import tools.vitruv.dsls.mirbase.mirBase.MetaclassReference;

@SuppressWarnings("all")
public final class KampRuleLanguageEcoreUtil {
  private KampRuleLanguageEcoreUtil() {
  }
  
  /**
   * <p>Returns the Metaclass ({@link EClass}) that is associated with a
   * grammar element.
   * 
   * <p>The 'associated' Metaclass is the class referenced by a {@link MetaclassReference},
   * or the type of the (set of) Metaclass(es) returned by a {@link PropagationReference}.
   */
  protected static EClass _getMetaclass(final ForwardEReference ref) {
    EClassifier _eType = ref.getFeature().getEType();
    return ((EClass) _eType);
  }
  
  /**
   * see #getMetaclass(ForwardEReference)
   */
  protected static EClass _getMetaclass(final BackwardEReference ref) {
    EObject _eContainer = ref.getFeature().eContainer();
    return ((EClass) _eContainer);
  }
  
  /**
   * see #getMetaclass(ForwardEReference)
   */
  protected static EClass _getMetaclass(final MetaclassReference ref) {
    return ref.getMetaclass();
  }
  
  /**
   * Returns the metaclass that is previous to the given {@link PropagationReference}.
   */
  public static EClass getPreviousMetaclass(final Lookup ref) {
    return KampRuleLanguageEcoreUtil.getMetaclass(
      EcoreUtil.<EObject>getPreviousSiblingOfType(ref, MetaclassReference.class, Lookup.class));
  }
  
  public static EClass getMetaclass(final EObject ref) {
    if (ref instanceof BackwardEReference) {
      return _getMetaclass((BackwardEReference)ref);
    } else if (ref instanceof ForwardEReference) {
      return _getMetaclass((ForwardEReference)ref);
    } else if (ref instanceof MetaclassReference) {
      return _getMetaclass((MetaclassReference)ref);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(ref).toString());
    }
  }
}
