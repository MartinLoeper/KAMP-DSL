package edu.kit.ipd.sdq.kamp.ruledsl.service;

public interface IRuleProvider {
	void applyAllRules();
	<T extends IRule> void register(T rule);
	void onRegistryReady();
	boolean areStandardRulesEnabled();
}
