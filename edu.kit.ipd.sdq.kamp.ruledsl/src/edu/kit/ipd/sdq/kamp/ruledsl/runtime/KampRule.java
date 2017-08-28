package edu.kit.ipd.sdq.kamp.ruledsl.runtime;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import edu.kit.ipd.sdq.kamp.ruledsl.support.IRule;

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
	Class<? extends IRule> parent() default IRule.class;
	boolean disableParent() default true;
	boolean enabled() default true;
}