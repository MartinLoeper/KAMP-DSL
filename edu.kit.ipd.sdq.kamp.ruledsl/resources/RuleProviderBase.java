package gen;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import edu.kit.ipd.sdq.kamp.ruledsl.service.IRule;
import edu.kit.ipd.sdq.kamp.ruledsl.service.IRuleProvider;
import edu.kit.ipd.sdq.kamp4bp.core.BPArchitectureVersion;

public abstract class RuleProviderBase implements IRuleProvider {
	
	private Collection<IRule> rules = new HashSet<>();
	
	@Override
	public final void applyAllRules(BPArchitectureVersion version) {
		System.out.println("Applying all custom dsl rules...");
		
		for(IRule cRule : this.rules) {
			System.out.println("Running rule: " + cRule.getClass().getSimpleName());
			cRule.apply(version);
		}
	}
	
	/**
	 * Registers the given {@code rule}.
	 * Each concrete rule class may be present in registry only once.
	 *  
	 * @param rule the rule which will be registered
	 * @throws IllegalStateException thrown if you try to register a rule (or one of its subclasses) which is already present in registry
	 */
	@Override
	public final <T extends IRule> void register(T rule) {	
		// check if no rule with same class or superclass does exist
		for(IRule cRule : this.rules) {
			if(cRule.getClass().isAssignableFrom(rule.getClass())) {
				throw new IllegalStateException("There is already a supertype of the given class available, which is not allowed: " + cRule.getClass() + ". The rule will be omitted and registration does not continue.");
			}
		}
		
		this.rules.add(rule);
	}
	
	/**
	 * Overrides all super instances with the given {@code newRule}.
	 * If no old rules are present this method behaves exactly as {@link #register(IRule)}
	 * 
	 * @param oldRule the rule which will be replaced
	 * @param newRule the new instance of the rule
	 */
	public final <T extends IRule, U extends T> void override(U newRule) {
		// if rule is already defined, replace it
		// also replace already registered rules
		// this allows the user to register custom rules
		
		// remove given oldRule instances
		for(Iterator<? extends IRule> it = this.rules.iterator(); it.hasNext();) {
			IRule rule = it.next();
			// check if rule to insert is a subclass of a rule which is already present
			if(rule.getClass().isAssignableFrom(newRule.getClass())) {
				System.out.println("rule override: " + rule.getClass() + " with: " + newRule.getClass());
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