package edu.kit.ipd.sdq.kamp.ruledsl.service;

import edu.kit.ipd.sdq.kamp4bp.core.BPArchitectureVersion;

public interface IRuleProvider {
	void applyAllRules(BPArchitectureVersion version);
	<T extends IRule> void register(T rule);
	void onRegistryReady();
	boolean areStandardRulesEnabled();
}
