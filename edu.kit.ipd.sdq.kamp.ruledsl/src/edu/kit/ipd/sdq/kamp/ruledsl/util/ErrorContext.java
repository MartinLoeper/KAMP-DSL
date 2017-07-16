package edu.kit.ipd.sdq.kamp.ruledsl.util;

/**
 * This is a label for the errors which are exported to Rollbar.
 * It is a hint in which context the error has happened.
 * If e.g. the exception was not caused by the framework but instead by client code, we may filter this context out and hide it in Rollbar.
 * 
 * @author Martin Löper
 *
 */
public enum ErrorContext {
	/**
	 * The error was caused by a rule which was coded by the enduser.
	 */
	CUSTOM_RULE("custom-rule"),
	
	/**
	 * The error was caused in the onRegistryReady callback of the user's custom RuleProvider implementation.
	 */
	CUSTOM_RULE_REGISTRATION("custom-rule-registration"),
	
	/**
	 * The error was caused during the update of the rules project.
	 */
	PROJECT_BUILD("project-build"),
	
	/**
	 * The error was caused during the creation of the rules project.
	 */
	PROJECT_BUILD_INITIAL("project-build-initial"),
	
	/**
	 * The error was caused by a syntax error in the rules project. Most probably in the source package if the user did not modify any gen resource.
	 */
	SYNTAX_ERROR("syntax-error");
	
	private final String name;
	
	private ErrorContext(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
}