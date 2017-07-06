/**
 * generated by Xtext 2.11.0
 */
package edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage;

import org.eclipse.emf.common.util.EList;

import tools.vitruv.dsls.mirbase.mirBase.MirBaseFile;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Rule File</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.RuleFile#getName <em>Name</em>}</li>
 *   <li>{@link edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.RuleFile#getRules <em>Rules</em>}</li>
 * </ul>
 *
 * @see edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.KampRuleLanguagePackage#getRuleFile()
 * @model
 * @generated
 */
public interface RuleFile extends MirBaseFile
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
   * @see edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.KampRuleLanguagePackage#getRuleFile_Name()
   * @model
   * @generated
   */
  String getName();

  /**
   * Sets the value of the '{@link edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.RuleFile#getName <em>Name</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Name</em>' attribute.
   * @see #getName()
   * @generated
   */
  void setName(String value);

  /**
   * Returns the value of the '<em><b>Rules</b></em>' containment reference list.
   * The list contents are of type {@link edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.KampRule}.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Rules</em>' containment reference list isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Rules</em>' containment reference list.
   * @see edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.KampRuleLanguagePackage#getRuleFile_Rules()
   * @model containment="true"
   * @generated
   */
  EList<KampRule> getRules();

} // RuleFile