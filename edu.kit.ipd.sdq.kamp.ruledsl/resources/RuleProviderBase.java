package gen;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import edu.kit.ipd.sdq.kamp.ruledsl.support.IRule;
import edu.kit.ipd.sdq.kamp.ruledsl.support.IRuleProvider;
import edu.kit.ipd.sdq.kamp.ruledsl.support.ChangePropagationStepRegistry;
import edu.kit.ipd.sdq.kamp.propagation.AbstractChangePropagationAnalysis;
import edu.kit.ipd.sdq.kamp.architecture.AbstractArchitectureVersion;
import edu.kit.ipd.sdq.kamp.ruledsl.util.RollbarExceptionReporting;
import edu.kit.ipd.sdq.kamp.ruledsl.util.ErrorContext;

public abstract class RuleProviderBase implements IRuleProvider {
	
	private Collection<IRule> rules = new HashSet<IRule>();
	private static final RollbarExceptionReporting REPORTING = RollbarExceptionReporting.INSTANCE;
	
	@Override
	public final void applyAllRules(AbstractArchitectureVersion version, ChangePropagationStepRegistry registry, AbstractChangePropagationAnalysis changePropagationAnalysis) {
		if(!REPORTING.isInitialized()) {
			REPORTING.init();
		}
		
		System.out.println("Applying all custom dsl rules...");
		
		for(final IRule cRule : this.rules) {
			System.out.println("Running rule: " + cRule.getClass().toString());
			try {
				cRule.apply(version, registry, changePropagationAnalysis);
			} catch(final Exception e) {
				// send exception to our rollbar server for examination and bug tracking
				REPORTING.log(e, ErrorContext.CUSTOM_RULE, null);
				
				// show the exception in the log
				e.printStackTrace();
				
				// display message to user
				Display.getDefault().syncExec(new Runnable() {
				    public void run() {
				    	MultiStatus status = Activator.createMultiStatus(e.getLocalizedMessage(), e);
						Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		                ErrorDialog.openError(shell, "Rule caused error", "The following rule caused an " + e.getClass().getSimpleName() + ": " + cRule.getClass(), status);
				    }
				});
			}
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
			
			if(rule.getClass().isAssignableFrom(cRule.getClass())) {
				throw new IllegalStateException("There is already a special type of the given class available, which is not allowed: " + rule.getClass() + ". You must not insert a generalized type of a rule which is already registered. Subclass it instead! The rule will be omitted and registration does not continue.");
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
	public final <T extends IRule> void override(T newRule) {
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