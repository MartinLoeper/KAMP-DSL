/**
 * generated by Xtext 2.11.0
 */
package edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.impl;

import edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.BackwardEReference;
import edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.ForwardEReference;
import edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.KampRule;
import edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.KampRuleLanguageFactory;
import edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.KampRuleLanguagePackage;
import edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.Lookup;
import edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.PropagationReference;
import edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.RuleFile;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import tools.vitruv.dsls.mirbase.mirBase.MirBasePackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class KampRuleLanguagePackageImpl extends EPackageImpl implements KampRuleLanguagePackage
{
  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass ruleFileEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass kampRuleEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass lookupEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass propagationReferenceEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass forwardEReferenceEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass backwardEReferenceEClass = null;

  /**
   * Creates an instance of the model <b>Package</b>, registered with
   * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
   * package URI value.
   * <p>Note: the correct way to create the package is via the static
   * factory method {@link #init init()}, which also performs
   * initialization of the package, or returns the registered package,
   * if one already exists.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.eclipse.emf.ecore.EPackage.Registry
   * @see edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.KampRuleLanguagePackage#eNS_URI
   * @see #init()
   * @generated
   */
  private KampRuleLanguagePackageImpl()
  {
    super(eNS_URI, KampRuleLanguageFactory.eINSTANCE);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private static boolean isInited = false;

  /**
   * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
   * 
   * <p>This method is used to initialize {@link KampRuleLanguagePackage#eINSTANCE} when that field is accessed.
   * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #eNS_URI
   * @see #createPackageContents()
   * @see #initializePackageContents()
   * @generated
   */
  public static KampRuleLanguagePackage init()
  {
    if (isInited) return (KampRuleLanguagePackage)EPackage.Registry.INSTANCE.getEPackage(KampRuleLanguagePackage.eNS_URI);

    // Obtain or create and register package
    KampRuleLanguagePackageImpl theKampRuleLanguagePackage = (KampRuleLanguagePackageImpl)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof KampRuleLanguagePackageImpl ? EPackage.Registry.INSTANCE.get(eNS_URI) : new KampRuleLanguagePackageImpl());

    isInited = true;

    // Initialize simple dependencies
    MirBasePackage.eINSTANCE.eClass();

    // Create package meta-data objects
    theKampRuleLanguagePackage.createPackageContents();

    // Initialize created meta-data
    theKampRuleLanguagePackage.initializePackageContents();

    // Mark meta-data to indicate it can't be changed
    theKampRuleLanguagePackage.freeze();

  
    // Update the registry and return the package
    EPackage.Registry.INSTANCE.put(KampRuleLanguagePackage.eNS_URI, theKampRuleLanguagePackage);
    return theKampRuleLanguagePackage;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getRuleFile()
  {
    return ruleFileEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EAttribute getRuleFile_Name()
  {
    return (EAttribute)ruleFileEClass.getEStructuralFeatures().get(0);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getRuleFile_Rules()
  {
    return (EReference)ruleFileEClass.getEStructuralFeatures().get(1);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getKampRule()
  {
    return kampRuleEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EAttribute getKampRule_Name()
  {
    return (EAttribute)kampRuleEClass.getEStructuralFeatures().get(0);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getKampRule_Source()
  {
    return (EReference)kampRuleEClass.getEStructuralFeatures().get(1);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getKampRule_Lookups()
  {
    return (EReference)kampRuleEClass.getEStructuralFeatures().get(2);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getLookup()
  {
    return lookupEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getPropagationReference()
  {
    return propagationReferenceEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getPropagationReference_Feature()
  {
    return (EReference)propagationReferenceEClass.getEStructuralFeatures().get(0);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getForwardEReference()
  {
    return forwardEReferenceEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getBackwardEReference()
  {
    return backwardEReferenceEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public KampRuleLanguageFactory getKampRuleLanguageFactory()
  {
    return (KampRuleLanguageFactory)getEFactoryInstance();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private boolean isCreated = false;

  /**
   * Creates the meta-model objects for the package.  This method is
   * guarded to have no affect on any invocation but its first.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void createPackageContents()
  {
    if (isCreated) return;
    isCreated = true;

    // Create classes and their features
    ruleFileEClass = createEClass(RULE_FILE);
    createEAttribute(ruleFileEClass, RULE_FILE__NAME);
    createEReference(ruleFileEClass, RULE_FILE__RULES);

    kampRuleEClass = createEClass(KAMP_RULE);
    createEAttribute(kampRuleEClass, KAMP_RULE__NAME);
    createEReference(kampRuleEClass, KAMP_RULE__SOURCE);
    createEReference(kampRuleEClass, KAMP_RULE__LOOKUPS);

    lookupEClass = createEClass(LOOKUP);

    propagationReferenceEClass = createEClass(PROPAGATION_REFERENCE);
    createEReference(propagationReferenceEClass, PROPAGATION_REFERENCE__FEATURE);

    forwardEReferenceEClass = createEClass(FORWARD_EREFERENCE);

    backwardEReferenceEClass = createEClass(BACKWARD_EREFERENCE);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private boolean isInitialized = false;

  /**
   * Complete the initialization of the package and its meta-model.  This
   * method is guarded to have no affect on any invocation but its first.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void initializePackageContents()
  {
    if (isInitialized) return;
    isInitialized = true;

    // Initialize package
    setName(eNAME);
    setNsPrefix(eNS_PREFIX);
    setNsURI(eNS_URI);

    // Obtain other dependent packages
    MirBasePackage theMirBasePackage = (MirBasePackage)EPackage.Registry.INSTANCE.getEPackage(MirBasePackage.eNS_URI);

    // Create type parameters

    // Set bounds for type parameters

    // Add supertypes to classes
    ruleFileEClass.getESuperTypes().add(theMirBasePackage.getMirBaseFile());
    propagationReferenceEClass.getESuperTypes().add(this.getLookup());
    forwardEReferenceEClass.getESuperTypes().add(this.getPropagationReference());
    backwardEReferenceEClass.getESuperTypes().add(this.getPropagationReference());
    backwardEReferenceEClass.getESuperTypes().add(theMirBasePackage.getMetaclassReference());

    // Initialize classes and features; add operations and parameters
    initEClass(ruleFileEClass, RuleFile.class, "RuleFile", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
    initEAttribute(getRuleFile_Name(), ecorePackage.getEString(), "name", null, 0, 1, RuleFile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
    initEReference(getRuleFile_Rules(), this.getKampRule(), null, "rules", null, 0, -1, RuleFile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

    initEClass(kampRuleEClass, KampRule.class, "KampRule", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
    initEAttribute(getKampRule_Name(), ecorePackage.getEString(), "name", null, 0, 1, KampRule.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
    initEReference(getKampRule_Source(), theMirBasePackage.getMetaclassReference(), null, "source", null, 0, 1, KampRule.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
    initEReference(getKampRule_Lookups(), this.getLookup(), null, "lookups", null, 0, -1, KampRule.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

    initEClass(lookupEClass, Lookup.class, "Lookup", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

    initEClass(propagationReferenceEClass, PropagationReference.class, "PropagationReference", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
    initEReference(getPropagationReference_Feature(), ecorePackage.getEStructuralFeature(), null, "feature", null, 0, 1, PropagationReference.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

    initEClass(forwardEReferenceEClass, ForwardEReference.class, "ForwardEReference", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

    initEClass(backwardEReferenceEClass, BackwardEReference.class, "BackwardEReference", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

    // Create resource
    createResource(eNS_URI);
  }

} //KampRuleLanguagePackageImpl
