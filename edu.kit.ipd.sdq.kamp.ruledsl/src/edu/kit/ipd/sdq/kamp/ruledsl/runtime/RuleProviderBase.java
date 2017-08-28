package edu.kit.ipd.sdq.kamp.ruledsl.runtime;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

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
import edu.kit.ipd.sdq.kamp.ruledsl.support.KampRuleStub;
import edu.kit.ipd.sdq.kamp.ruledsl.support.RegistryException;
import edu.kit.ipd.sdq.kamp.ruledsl.util.ErrorContext;
import edu.kit.ipd.sdq.kamp.ruledsl.util.RollbarExceptionReporting;

public class RuleProviderBase implements IRuleProvider {
	
	private static final RollbarExceptionReporting REPORTING = RollbarExceptionReporting.INSTANCE;
	private final Map<IRule, KampRuleStub> rules = new HashMap<>();
	private Consumer<Set<IRule>> preHook;

	@Override
	public final void applyAllRules(AbstractArchitectureVersion version, ChangePropagationStepRegistry registry, AbstractChangePropagationAnalysis changePropagationAnalysis) {
		if(!REPORTING.isInitialized()) {
			REPORTING.init();
		}
				
		System.out.println("Applying all custom dsl rules...");
		
		if(this.preHook != null) {
			System.out.println("Running pre hook...");
			this.preHook.accept(this.rules.keySet());
		}
		
		for(final Entry<IRule, KampRuleStub> cRuleEntry : this.rules.entrySet()) {
			if(!cRuleEntry.getValue().isActive()) {
				continue;
			}
			
			IRule cRule = cRuleEntry.getKey();
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
				    	MultiStatus status = createMultiStatus(e.getLocalizedMessage(), e);
						Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		                ErrorDialog.openError(shell, "Rule caused error", "The following rule caused an " + e.getClass().getSimpleName() + ": " + cRule.getClass(), status);
				    }
				});
			}
		}
	}
	
	/**
	 * Registers the given {@code rule's class} by instantiating the given rule and storing it.
	 * Each concrete rule class may be present in registry only once.
	 *  
	 * @param rule the class of the rule which will be instantiated
	 */
	@Override
	public final void register(KampRuleStub ruleStub) throws RegistryException {	
		if(ruleStub.hasParent()) {
			// get the dependency first
			IRule parentRule = null;
			for(IRule cRule : this.rules.keySet()) {
				if(cRule.getClass().equals(ruleStub.getParent())) {
					parentRule = cRule;
				}
			}
			
			if(parentRule == null) {
				throw new IllegalStateException("Error, the dependency injection failed. Rule with the following class missing: " + ruleStub.getParent().getSimpleName());
			}
			
			try {
				IRule newRule = ruleStub.getClazz().getConstructor(ruleStub.getParent()).newInstance(parentRule);
				this.rules.put(newRule, ruleStub);
			} catch (InstantiationException e) {
				throw new RegistryException("Could not access a constructor which accepts the parent rule: " + parentRule.getClass().getSimpleName(), e);
			} catch (IllegalAccessException e) {
				throw new RegistryException("IllegalAccess Exception while reflectively trying to create the rule: " + ruleStub.getClazz().getSimpleName(), e);
			} catch (IllegalArgumentException e) {
				throw new RegistryException("A programming error inside the DI logic occured. Please contact the KAMP-DSL developer.", e);
			} catch (InvocationTargetException e) {
				throw new RegistryException("The constructor for the following rule threw an exception:" + ruleStub.getClazz().getSimpleName(), e);
			} catch (NoSuchMethodException e) {
				throw new RegistryException("Could not find a constructor which accepts the parent rule: " + parentRule.getClass().getSimpleName(), e);
			} catch (SecurityException e) {
				throw new RegistryException("Security Exception while reflectively trying to create the rule: " + ruleStub.getClazz().getSimpleName(), e);
			}
		} else {
			try {
				IRule newRule = ruleStub.getClazz().newInstance();
				this.rules.put(newRule, ruleStub);
			} catch (InstantiationException e) {
				throw new RegistryException("Could not find a standard constructor for rule: " + ruleStub.getClazz().getSimpleName(), e);
			} catch (IllegalAccessException e) {
				throw new RegistryException("IllegalAccess Exception while reflectively trying to create the rule: " + ruleStub.getClazz().getSimpleName(), e);
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
	
	// TODO replace this with project scoped preferences in the future: https://help.eclipse.org/mars/topic/org.eclipse.platform.doc.isv/guide/resInt_preferences.htm
	@Override
	public boolean areStandardRulesEnabled() {
		return true;
	}

	@Override
	public long getNumberOfRegisteredRules() {
		return this.rules.size();
	}

	@Override
	public void runEarlyHook(Consumer<Set<IRule>> preHook) {
		this.preHook = preHook;
	}
}