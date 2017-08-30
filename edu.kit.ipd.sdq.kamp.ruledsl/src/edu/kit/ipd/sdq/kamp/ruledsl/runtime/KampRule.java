package edu.kit.ipd.sdq.kamp.ruledsl.runtime;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import edu.kit.ipd.sdq.kamp.ruledsl.support.IRule;
import edu.kit.ipd.sdq.kamp.ruledsl.support.IRuleProvider;

/**
 * Indicates that the annotated class is a KAMP rule.
 * Rules are automatically run by the KAMP framework by reflective calls.
 * If a parent rule is set, the rule will be injected as first constructor parameter.
 * Otherwise if parent is unset, a standard constructor is used to create the rule.
 * 
 * If a parent rule is set, the disableParent option is taken into account.
 * This attribute is evaluated per rule and is set to false by default.
 * If you define a parent, the disableParent option is set to true by default.
 * This instructs the KAMP framework to exclude <b>all</b> parent rules.
 * <br /><br />
 * This exclusion feature is implemented, because users typically write specific subrules
 * and do not want the parent rules to be run.
 * 
 * @author Martin Loeper
 *
 */
@Target(value = ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface KampRule {
	/**
	 * Sets the given rule as parent of this one. Each rule can have max. one parent. Cycles are not allowed and result in a RuntimeException being thrown.
	 * This instructs the registry to create the parent rule first and subsequently inject it into this one.
	 * Please keep in mind, that you must implement a constructor which takes exactly the type of the parent rule as first parameter.
	 * @return the parent of this rule
	 */
	Class<? extends IRule> parent() default IRule.class;
	
	/**
	 * If set true, it will disable all ancestor rules. This means, they are instantiated and injected but not run on their own.
	 * @return whether the state of all ancestors is set to disabled
	 */
	boolean disableAncestors() default true;
	
	/**
	 * If set true, this rule is run on its own. This means that the {@link IRuleProvider#applyAllRules(edu.kit.ipd.sdq.kamp.architecture.AbstractArchitectureVersion, edu.kit.ipd.sdq.kamp.ruledsl.support.ChangePropagationStepRegistry, edu.kit.ipd.sdq.kamp.propagation.AbstractChangePropagationAnalysis)} method invokes this rule explicitly.
	 * Please note that even if this rule is disabled, it may be invoked by a child rule.
	 * @return whether this rule is run by the registry explicitly
	 */
	boolean enabled() default true;
}