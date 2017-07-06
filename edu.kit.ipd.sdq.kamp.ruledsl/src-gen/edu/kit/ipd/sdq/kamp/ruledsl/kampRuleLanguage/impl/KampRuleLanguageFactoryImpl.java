/**
 * generated by Xtext 2.11.0
 */
package edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.impl;

import edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.*;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class KampRuleLanguageFactoryImpl extends EFactoryImpl implements KampRuleLanguageFactory
{
  /**
   * Creates the default factory implementation.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public static KampRuleLanguageFactory init()
  {
    try
    {
      KampRuleLanguageFactory theKampRuleLanguageFactory = (KampRuleLanguageFactory)EPackage.Registry.INSTANCE.getEFactory(KampRuleLanguagePackage.eNS_URI);
      if (theKampRuleLanguageFactory != null)
      {
        return theKampRuleLanguageFactory;
      }
    }
    catch (Exception exception)
    {
      EcorePlugin.INSTANCE.log(exception);
    }
    return new KampRuleLanguageFactoryImpl();
  }

  /**
   * Creates an instance of the factory.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public KampRuleLanguageFactoryImpl()
  {
    super();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public EObject create(EClass eClass)
  {
    switch (eClass.getClassifierID())
    {
      case KampRuleLanguagePackage.RULE_FILE: return createRuleFile();
      case KampRuleLanguagePackage.KAMP_RULE: return createKampRule();
      case KampRuleLanguagePackage.LOOKUP: return createLookup();
      case KampRuleLanguagePackage.PROPAGATION_REFERENCE: return createPropagationReference();
      case KampRuleLanguagePackage.FORWARD_EREFERENCE: return createForwardEReference();
      case KampRuleLanguagePackage.BACKWARD_EREFERENCE: return createBackwardEReference();
      default:
        throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
    }
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public RuleFile createRuleFile()
  {
    RuleFileImpl ruleFile = new RuleFileImpl();
    return ruleFile;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public KampRule createKampRule()
  {
    KampRuleImpl kampRule = new KampRuleImpl();
    return kampRule;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public Lookup createLookup()
  {
    LookupImpl lookup = new LookupImpl();
    return lookup;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public PropagationReference createPropagationReference()
  {
    PropagationReferenceImpl propagationReference = new PropagationReferenceImpl();
    return propagationReference;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ForwardEReference createForwardEReference()
  {
    ForwardEReferenceImpl forwardEReference = new ForwardEReferenceImpl();
    return forwardEReference;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public BackwardEReference createBackwardEReference()
  {
    BackwardEReferenceImpl backwardEReference = new BackwardEReferenceImpl();
    return backwardEReference;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public KampRuleLanguagePackage getKampRuleLanguagePackage()
  {
    return (KampRuleLanguagePackage)getEPackage();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @deprecated
   * @generated
   */
  @Deprecated
  public static KampRuleLanguagePackage getPackage()
  {
    return KampRuleLanguagePackage.eINSTANCE;
  }

} //KampRuleLanguageFactoryImpl