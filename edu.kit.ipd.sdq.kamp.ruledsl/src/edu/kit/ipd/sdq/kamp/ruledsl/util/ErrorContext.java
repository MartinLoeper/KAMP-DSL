package edu.kit.ipd.sdq.kamp.ruledsl.util;

public enum ErrorContext {
	CUSTOM_RULE("custom-rule"),
	CUSTOM_RULE_REGISTRATION("custom-rule-registration"),
	PROJECT_BUILD("project-build"),
	PROJECT_BUILD_INITIAL("project-build-initial");
	
	private final String name;
	
	private ErrorContext(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
}