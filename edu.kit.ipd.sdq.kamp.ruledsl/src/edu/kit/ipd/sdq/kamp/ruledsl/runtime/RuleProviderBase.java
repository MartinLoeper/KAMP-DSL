package edu.kit.ipd.sdq.kamp.ruledsl.runtime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import edu.kit.ipd.sdq.kamp.architecture.AbstractArchitectureVersion;
import edu.kit.ipd.sdq.kamp.propagation.AbstractChangePropagationAnalysis;
import edu.kit.ipd.sdq.kamp.ruledsl.support.ChangePropagationStepRegistry;
import edu.kit.ipd.sdq.kamp.ruledsl.support.IRule;
import edu.kit.ipd.sdq.kamp.ruledsl.support.IRuleProvider;
import edu.kit.ipd.sdq.kamp.ruledsl.support.KampRuleLanguageUtil;
import edu.kit.ipd.sdq.kamp.ruledsl.util.ErrorContext;
import edu.kit.ipd.sdq.kamp.ruledsl.util.RollbarExceptionReporting;

public class RuleProviderBase implements IRuleProvider {
	
	private Map<IRule, Boolean> rules = new HashMap<IRule, Boolean>();
	private Set<Class<? extends IRule>> parentRulesRemovalRequested = new HashSet<Class<? extends IRule>>();
	private static final RollbarExceptionReporting REPORTING = RollbarExceptionReporting.INSTANCE;
	private Set<IRule> singletonRuleRegistry = new HashSet<>();
	
	public <T extends IRule> T getInstance(Class<T> clazz, T newInstance) {
		for(IRule cInstance : this.singletonRuleRegistry) {
			if(cInstance.getClass().equals(clazz)) {
				return (T) cInstance;
			}
		}
		
		this.singletonRuleRegistry.add(newInstance);
		return newInstance;
	}
	
	@Override
	public final void applyAllRules(AbstractArchitectureVersion version, ChangePropagationStepRegistry registry, AbstractChangePropagationAnalysis changePropagationAnalysis) {
		if(!REPORTING.isInitialized()) {
			REPORTING.init();
		}
		
		System.out.println("Removing all parent rules lazily...");
		// TODO remove rules lazily
		System.out.println(this.parentRulesRemovalRequested);
		
		System.out.println("Applying all custom dsl rules...");
		
		for(final Entry<IRule, Boolean> cRuleEntry : this.rules.entrySet()) {
			System.out.println("[RULE][" + cRuleEntry.getValue() + "]" + cRuleEntry.getKey().getClass().getName());
			
			// if rule is disabled, ignore it in the current iteration
			if(!cRuleEntry.getValue()) {
				continue;
			}
			
			IRule cRule = cRuleEntry.getKey();
			// System.out.println("Running rule: " + cRule.getClass().toString());
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
				    	MultiStatus status = createMultiStatus(e.getLocalizedMessage(), e);
						Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		                ErrorDialog.openError(shell, "Rule caused error", "The following rule caused an " + e.getClass().getSimpleName() + ": " + cRule.getClass(), status);
				    }
				});
			}
		}
	}
	
    public static MultiStatus createMultiStatus(String msg, Throwable t) {
        List<Status> childStatuses = new ArrayList<>();
        StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();

        for (StackTraceElement stackTrace: stackTraces) {
            Status status = new Status(IStatus.ERROR, KampRuleLanguageUtil.BUNDLE_NAME + ".xxxxxxxx", stackTrace.toString());
            childStatuses.add(status);
        }

        MultiStatus ms = new MultiStatus(KampRuleLanguageUtil.BUNDLE_NAME + ".xxxxxxxx",
                IStatus.ERROR, childStatuses.toArray(new Status[] {}), t.toString(), t);
        
        return ms;
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
//		for(IRule cRule : this.rules.keySet()) {
//			if(cRule.getClass().isAssignableFrom(rule.getClass())) {
//				throw new IllegalStateException("There is already a supertype of the given class available, which is not allowed: " + cRule.getClass() + ". The rule will be omitted and registration does not continue.");
//			}
//			
//			if(rule.getClass().isAssignableFrom(cRule.getClass())) {
//				throw new IllegalStateException("There is already a special type of the given class available, which is not allowed: " + rule.getClass() + ". You must not insert a generalized type of a rule which is already registered. Subclass it instead! The rule will be omitted and registration does not continue.");
//			}
//		}
		
		this.rules.put(rule, true);
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
		removeSubtypes(newRule);
		
		// register new rule
		register(newRule);
	}
	
	private final <T extends IRule> void removeSubtypes(T ruleType) {
		for(Iterator<Entry<IRule, Boolean>> it = this.rules.entrySet().iterator(); it.hasNext();) {
			Entry<IRule, Boolean> ruleEntry = it.next();
			IRule rule = ruleEntry.getKey();
			// check if rule to insert is a subclass of a rule which is already present
			if(rule.getClass().isAssignableFrom(ruleType.getClass())) {
				System.out.println("rule override: " + rule.getClass() + " with: " + ruleType.getClass());
				ruleEntry.setValue(false);
			}
		}
	}

	@Override
	public boolean areStandardRulesEnabled() {
		return true;
	}

	@Override
	public <T extends IRule> void removeParentRulesLazily(Class<T> rule) {
		this.parentRulesRemovalRequested.add(rule);
	}
}