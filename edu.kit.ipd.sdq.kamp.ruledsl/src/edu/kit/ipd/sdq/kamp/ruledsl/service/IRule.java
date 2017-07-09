package edu.kit.ipd.sdq.kamp.ruledsl.service;

import edu.kit.ipd.sdq.kamp4bp.core.BPArchitectureVersion;

public interface IRule {
	void apply(BPArchitectureVersion version);
}
