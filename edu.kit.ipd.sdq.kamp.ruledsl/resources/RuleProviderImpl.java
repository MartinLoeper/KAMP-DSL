package src;

import gen.RuleProviderBase;
import javax.swing.JOptionPane;

import edu.kit.ipd.sdq.kamp.architecture.AbstractArchitectureVersion;
import edu.kit.ipd.sdq.kamp.propagation.AbstractChangePropagationAnalysis;
import edu.kit.ipd.sdq.kamp.ruledsl.support.ChangePropagationStepRegistry;
import edu.kit.ipd.sdq.kamp.ruledsl.support.IRule;
import edu.kit.ipd.sdq.kamp.util.LookupUtil;

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
			public void apply(AbstractArchitectureVersion version, ChangePropagationStepRegistry registry, AbstractChangePropagationAnalysis changePropagationAnalysis) {
				JOptionPane.showMessageDialog(null, "Custom rule is working!");
			}
		});
	}
}