package src;

import gen.RuleProviderBase;
import javax.swing.JOptionPane;

import edu.kit.ipd.sdq.kamp4bp.core.BPArchitectureVersion;
import edu.kit.ipd.sdq.kamp4bp.ruledsl.support.IRule;
import edu.kit.ipd.sdq.kamp4is.core.AbstractISChangePropagationAnalysis;
import edu.kit.ipd.sdq.kamp4is.core.ISArchitectureVersion;
import edu.kit.ipd.sdq.kamp4is.model.modificationmarks.ISChangePropagationDueToDataDependencies;


public class RuleProviderImpl extends RuleProviderBase {
	@Override
	public void onRegistryReady() {
	 	// extend the existing rules and register them using override
	 	// Please note: 
		// - custom rules need a public default constructor
	 	// - create your own rules inside this src package
	 	//    or use anonymous classes like the following one:
	 	
	 	override(new IRule() {
	 	
			@Override
			public void apply(AbstractArchitectureVersion version, AbstractChangePropagationAnalysis changePropagationAnalysis) {
				JOptionPane.showMessageDialog(null, "Custom rule is working!");
			}
		});
	}
}