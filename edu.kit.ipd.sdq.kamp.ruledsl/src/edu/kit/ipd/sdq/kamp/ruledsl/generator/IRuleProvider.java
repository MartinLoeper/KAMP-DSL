package edu.kit.ipd.sdq.kamp.ruledsl.generator;

public interface IRuleProvider {
	void applyAllRules();
	<T extends IRule> void register(T rule);
	void onRegistryReady();
	boolean areStandardRulesEnabled();
}
