package gen;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import edu.kit.ipd.sdq.kamp.ruledsl.generator.IRule;

import edu.kit.ipd.sdq.kamp.ruledsl.generator.IRuleProvider;

public abstract class RuleProviderBase implements IRuleProvider {
	
	private Collection<IRule> rules = new ArrayList<>();
	
	@Override
	public final void applyAllRules() {
		System.out.println("Applying all custom dsl rules...");
		
		for(IRule cRule : this.rules) {
			System.out.println("Running rule: " + cRule.getClass().getSimpleName());
			cRule.apply();
		}
	}
	
	/**
	 * Registers the given {@code rule}.
	 * @param rule the rule which will be registered
	 */
	@Override
	public final <T extends IRule> void register(T rule) {		
		this.rules.add(rule);
	}
	
	/**
	 * Overrides an existing {@code oldRule} with the given class and replaces it with the given {@code newRule}.
	 * @param oldRule the rule which will be replaced
	 * @param newRule the new instance of the rule
	 */
	public final void override(Class<? extends IRule> oldRule, IRule newRule) {
		// if rule is already defined, replace it
		// also replace already registered rules
		// this allows the user to register custom rules
		
		// remove given oldRule instances
		for(Iterator<? extends IRule> it = this.rules.iterator(); it.hasNext();) {
			IRule rule = it.next();
			// check if rule to insert is a subclass of a rule which is already present
			if(rule.getClass().equals(oldRule.getClass())) {
				it.remove();
			}
		}
		
		// register new rule
		register(newRule);
	}
	
	@Override
	public boolean areStandardRulesEnabled() {
		return true;
	}
}