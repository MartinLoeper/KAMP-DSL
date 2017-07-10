package edu.kit.ipd.sdq.kamp.ruledsl.service;

import edu.kit.ipd.sdq.kamp4bp.core.BPArchitectureVersion;
import edu.kit.ipd.sdq.kamp4is.core.AbstractISChangePropagationAnalysis;
import edu.kit.ipd.sdq.kamp4is.core.ISArchitectureVersion;
import edu.kit.ipd.sdq.kamp4is.model.modificationmarks.ISChangePropagationDueToDataDependencies;

public interface IRuleProvider {
	void applyAllRules(BPArchitectureVersion version, AbstractISChangePropagationAnalysis<? extends ISArchitectureVersion, 
			? extends ISChangePropagationDueToDataDependencies> changePropagationAnalysis);
	<T extends IRule> void register(T rule);
	void onRegistryReady();
	boolean areStandardRulesEnabled();
}
