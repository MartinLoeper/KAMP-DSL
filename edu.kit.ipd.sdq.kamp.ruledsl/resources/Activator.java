import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import edu.kit.ipd.sdq.kamp.ruledsl.generator.IRuleProvider;

public class Activator implements BundleActivator {

    public void start(BundleContext context) throws Exception {
        System.out.println("KAMP-RuleDSL bundle successfully activated.");
        context.registerService(IRuleProvider.class.getName(), new RuleProviderImpl(), null);
    }

    public void stop(BundleContext context) throws Exception {
    	 System.out.println("KAMP-RuleDSL bundle successfully shut down.");
    }
}