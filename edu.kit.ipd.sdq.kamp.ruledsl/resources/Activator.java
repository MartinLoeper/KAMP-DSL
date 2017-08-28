package gen;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import edu.kit.ipd.sdq.kamp.ruledsl.runtime.KampRule;
import edu.kit.ipd.sdq.kamp.ruledsl.runtime.RuleProviderBase;
import edu.kit.ipd.sdq.kamp.ruledsl.runtime.graph.GraphException;
import edu.kit.ipd.sdq.kamp.ruledsl.runtime.graph.KampRuleGraph;
import edu.kit.ipd.sdq.kamp.ruledsl.runtime.graph.KampRuleVertex;
import edu.kit.ipd.sdq.kamp.ruledsl.support.IRule;
import edu.kit.ipd.sdq.kamp.ruledsl.support.IRuleProvider;
import edu.kit.ipd.sdq.kamp.ruledsl.util.RollbarExceptionReporting;
import gen.rule.TestRule;

public class Activator extends AbstractUIPlugin implements BundleActivator {

	private IRuleProvider ruleProvider;
	private final KampRuleGraph ruleGraph = new KampRuleGraph();
	private static final RollbarExceptionReporting REPORTING = RollbarExceptionReporting.INSTANCE;
	
    public void start(BundleContext context) throws Exception {
    	super.start(context);
        
    	// build the rule provider, which contains all rules which will be examined in list form (instead of graph)
        ruleProvider = new RuleProviderBase();
        
        // build the rule graph
        registerRules();
        registerUsersRules();
        
        // validate the rule graph
        try {
        	this.ruleGraph.validate();
        	
        	// run exclusion algorithms (such as disable all parents)
            this.ruleGraph.runExclusionAlgorithms();
            
            // TODO for debug purposes: show the graph
            this.ruleGraph.show();
            
            // convert rule graph into RuleProvider registry instructions (topological sort)
//            List<Class<? extends IRule>> rules = this.ruleGraph.topologicalSort();
//            for(Class<? extends IRule> cRule : rules) {
//            	System.out.println(cRule.getSimpleName());
//            }
        } catch(GraphException e) {
        	Display.getDefault().syncExec(new Runnable() {
			    public void run() {
			    	MultiStatus status = RuleProviderBase.createMultiStatus("You created a cycle in you rule hierarchy.", e);
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	                ErrorDialog.openError(shell, "You caused an error", null, status);
			    }
			});
        }
        
        System.out.println("KAMP-RuleDSL bundle successfully activated.");
        context.registerService(IRuleProvider.class.getName(), ruleProvider, null);
    }
    
    public void registerUsersRules() {
    	Reflections reflections = new Reflections(new ConfigurationBuilder()
	 			 .addClassLoaders(new ClassLoader[] { getClass().getClassLoader() })
	 		     .setUrls(ClasspathHelper.forPackage("src", getClass().getClassLoader()))
	 		     .setScanners(new SubTypesScanner(false), new TypeAnnotationsScanner())
	 		     .filterInputsBy(new FilterBuilder().includePackage("src"))); 
	 	
	 	Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(KampRule.class);
	 	
	 	annotatedClasses.stream().forEach(c -> {
	 		if(IRule.class.isAssignableFrom(c)) {
	 			Class<? extends IRule> cIRule = (Class<? extends IRule>) c;
	 			KampRuleVertex cVertex = new KampRuleVertex(cIRule);
	 			this.ruleGraph.addVertex(cVertex);
	 			
	 			KampRule[] kampRuleAnnotations = c.getDeclaredAnnotationsByType(KampRule.class);
	 			if(kampRuleAnnotations.length > 0) {
	 				if(kampRuleAnnotations.length > 1) {
	 					System.err.println("[RULE-REGISTRY] The user defined multiple KampRule annotations. Only the first one is used. The rest is ignored.");
	 				}
	 				
	 				KampRule kampRuleAnnotation = kampRuleAnnotations[0];
	 				IRule ruleInstance = null;
		 			if(!kampRuleAnnotation.parent().getClass().equals(IRule.class)) {
		 				KampRuleVertex parentVertex = this.ruleGraph.getVertex(kampRuleAnnotation.parent());
		 				if(parentVertex == null) {
		 					parentVertex = new KampRuleVertex(kampRuleAnnotation.parent());
		 				}
		 				
		 				cVertex.setParent(parentVertex);
		 				parentVertex.addChild(cVertex);
//		 				try {
//							// TODO implement some kind of DI
//		 					//ruleInstance = (IRule) c.getConstructor(IRule.class).newInstance(null);
//		 					//ruleInstance = (IRule) c.newInstance();
//							//this.ruleProvider.register(ruleInstance);
//						} catch (InstantiationException | IllegalAccessException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
		 				
		 				if(kampRuleAnnotation.disableParent()) {
			 				cVertex.disableAllParents();
		 				} 
		 			} else {
		 				try {
							ruleInstance = (IRule) c.newInstance();
							//this.ruleProvider.register(ruleInstance);
						} catch (InstantiationException | IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		 			}
	 			}
	 		} else {
	 			System.err.println("[RULE-REGISTRY] The user defined and annotated type does not implement the IRule interface and is thus excluded from rule registration: " + c.getName());
	 		}
	 	});
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
    		this.ruleGraph.addVertex(new KampRuleVertex(cRule));
    	}
    }
}