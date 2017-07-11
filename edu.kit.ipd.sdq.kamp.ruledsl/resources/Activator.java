package gen;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import edu.kit.ipd.sdq.kamp.ruledsl.support.KampRuleLanguageUtil;
import edu.kit.ipd.sdq.kamp.ruledsl.support.IRule;
import edu.kit.ipd.sdq.kamp.ruledsl.support.IRuleProvider;
import src.RuleProviderImpl;
import gen.rule.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class Activator extends AbstractUIPlugin implements BundleActivator {

	private IRuleProvider ruleProvider;
	
    public void start(BundleContext context) throws Exception {
    	super.start(context);
        
        ruleProvider = new RuleProviderImpl();
        registerRules();
        try {
        	this.ruleProvider.onRegistryReady();
            System.out.println("KAMP-RuleDSL bundle successfully activated.");
        } catch(Exception e) {
        	e.printStackTrace();
			
			Display.getDefault().asyncExec(new Runnable() {
			    public void run() {
			    	MultiStatus status = createMultiStatus(e.getLocalizedMessage(), e);
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	                ErrorDialog.openError(shell, "You caused an error", "A user defined method caused a framework exception.", status);
			    }
			});
        }
        
        context.registerService(IRuleProvider.class.getName(), ruleProvider, null);
    }

    public void stop(BundleContext context) throws Exception {
    	super.stop(context);
    	 System.out.println("KAMP-RuleDSL bundle successfully shut down.");
    }
    
    private void registerRules() {
    	// register the rules
    	@SuppressWarnings("unchecked")
		Class<? extends IRule>[] rules = new Class[] { %s };
    	for(Class<? extends IRule> cRule : rules) {
    		try {
				this.ruleProvider.register(cRule.newInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				// this should never happen
				e.printStackTrace();
				
				// make the bundle registration fail as something has gone horribly wrong
				throw new RuntimeException("Did you forget the public default constructor for rule: " + cRule.getClass() + "?", e);
			}
    	}
    }

    static MultiStatus createMultiStatus(String msg, Throwable t) {
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
}