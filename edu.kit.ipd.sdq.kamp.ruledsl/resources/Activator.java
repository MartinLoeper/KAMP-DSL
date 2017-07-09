package gen;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import edu.kit.ipd.sdq.kamp.ruledsl.generator.IRuleProvider;
import edu.kit.ipd.sdq.kamp.ruledsl.generator.IRule;
import src.RuleProviderImpl;

public class Activator implements BundleActivator {

	private IRuleProvider ruleProvider;
	
    public void start(BundleContext context) throws Exception {
        System.out.println("KAMP-RuleDSL bundle successfully activated.");
        
        ruleProvider = new RuleProviderImpl();
        registerRules();
        this.ruleProvider.onRegistryReady();
        
        context.registerService(IRuleProvider.class.getName(), ruleProvider, null);
    }

    public void stop(BundleContext context) throws Exception {
    	 System.out.println("KAMP-RuleDSL bundle successfully shut down.");
    }
    
    private void registerRules() {
    	// register the rules
    	Class<? extends IRule>[] rules = new Class[] { %s };
    	for(Class<? extends IRule> cRule : rules) {
    		try {
				this.ruleProvider.register(cRule.newInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
}