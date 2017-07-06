/**
 * generated by Xtext 2.11.0
 */
package edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

import tools.vitruv.dsls.mirbase.mirBase.MetaclassReference;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Kamp Rule</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.KampRule#getName <em>Name</em>}</li>
 *   <li>{@link edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.KampRule#getSource <em>Source</em>}</li>
 *   <li>{@link edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.KampRule#getLookups <em>Lookups</em>}</li>
 * </ul>
 *
 * @see edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.KampRuleLanguagePackage#getKampRule()
 * @model
 * @generated
 */
public interface KampRule extends EObject
{
  /**
   * Returns the value of the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Name</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Name</em>' attribute.
   * @see #setName(String)
   * @see edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.KampRuleLanguagePackage#getKampRule_Name()
   * @model
   * @generated
   */
  String getName();

  /**
   * Sets the value of the '{@link edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.KampRule#getName <em>Name</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Name</em>' attribute.
   * @see #getName()
   * @generated
   */
  void setName(String value);

  /**
   * Returns the value of the '<em><b>Source</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Source</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Source</em>' containment reference.
   * @see #setSource(MetaclassReference)
   * @see edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.KampRuleLanguagePackage#getKampRule_Source()
   * @model containment="true"
   * @generated
   */
  MetaclassReference getSource();

  /**
   * Sets the value of the '{@link edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.KampRule#getSource <em>Source</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Source</em>' containment reference.
   * @see #getSource()
   * @generated
   */
  void setSource(MetaclassReference value);

  /**
   * Returns the value of the '<em><b>Lookups</b></em>' containment reference list.
   * The list contents are of type {@link edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.Lookup}.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Lookups</em>' containment reference list isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Lookups</em>' containment reference list.
   * @see edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.KampRuleLanguagePackage#getKampRule_Lookups()
   * @model containment="true"
   * @generated
   */
  EList<Lookup> getLookups();

} // KampRule
