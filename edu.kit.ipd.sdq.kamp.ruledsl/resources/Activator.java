package gen;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import edu.kit.ipd.sdq.kamp.ruledsl.runtime.KampConfiguration;
import edu.kit.ipd.sdq.kamp.ruledsl.runtime.KampGraph;
import edu.kit.ipd.sdq.kamp.ruledsl.runtime.KampRule;
import edu.kit.ipd.sdq.kamp.ruledsl.runtime.RuleProviderBase;
import edu.kit.ipd.sdq.kamp.ruledsl.runtime.graph.GraphException;
import edu.kit.ipd.sdq.kamp.ruledsl.runtime.graph.KampRuleGraph;
import edu.kit.ipd.sdq.kamp.ruledsl.support.KampRuleStub;
import edu.kit.ipd.sdq.kamp.ruledsl.support.RegistryException;
import edu.kit.ipd.sdq.kamp.ruledsl.runtime.graph.KampRuleVertex;
import edu.kit.ipd.sdq.kamp.ruledsl.support.IConfiguration;
import edu.kit.ipd.sdq.kamp.ruledsl.support.IRule;
import edu.kit.ipd.sdq.kamp.ruledsl.support.IRuleProvider;
import edu.kit.ipd.sdq.kamp.ruledsl.util.RollbarExceptionReporting;
import gen.rule.*;


public class Activator extends AbstractUIPlugin implements BundleActivator {

	private IRuleProvider ruleProvider;
	private final KampRuleGraph ruleGraph = new KampRuleGraph();
	private static final RollbarExceptionReporting REPORTING = RollbarExceptionReporting.INSTANCE;
	private static Reflections reflections;
	
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
            
            // convert rule graph into RuleProvider registry instructions (topological sort)
            List<KampRuleStub> rules = this.ruleGraph.topologicalSort();
            for(KampRuleStub cRuleStub : rules) {
            	System.out.println(cRuleStub.getClazz().getSimpleName());
            	try {
            		this.ruleProvider.register(cRuleStub);
            	} catch(RegistryException e) {
            		// we have to abort the registration because the DI is not guaranteed to work from now on...
            		Display.getDefault().syncExec(new Runnable() {
        			    public void run() {
        			    	MultiStatus status = RuleProviderBase.createMultiStatus(null, e);
        					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        	                ErrorDialog.openError(shell, "Rule Registration Error", "The rules could not be instantiated and registered correctly. They were registered until the exception occured. Total rules registered so far: " + ruleProvider.getNumberOfRegisteredRules() + ". Rule causing exception: " + cRuleStub.getClazz().getSimpleName(), status);
        			    }
        			});
            		break;
            	}
            }
        } catch(GraphException e) {
        	Display.getDefault().syncExec(new Runnable() {
			    public void run() {
			    	MultiStatus status = RuleProviderBase.createMultiStatus("You created a cycle in you rule hierarchy.", e);
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	                ErrorDialog.openError(shell, "Dependency Injection Error", null, status);
			    }
			});
        }
        
        // scan for configuration classes
        Set<Class<?>> annotatedClasses = getReflectionsForSrcPackage().getTypesAnnotatedWith(KampConfiguration.class);
	 	if(annotatedClasses.size() > 0) {
	 		if(annotatedClasses.size() == 1) {
	 			Class<?> c = annotatedClasses.stream().findFirst().get();
		 		if(IConfiguration.class.isAssignableFrom(c)) {
		 			Class<? extends IConfiguration> cIConfig = (Class<? extends IConfiguration>) c;
		 			try {
						this.ruleProvider.setConfiguration(cIConfig.newInstance());
					} catch (InstantiationException | IllegalAccessException e) {
						Display.getDefault().syncExec(new Runnable() {
						    public void run() {
						    	MultiStatus status = RuleProviderBase.createMultiStatus(null, e);
								Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				                ErrorDialog.openError(shell, "Dependency Injection Error", "Could not instantiate the Configuration class. Did you forget or override the standard constructor?", status);
						    }
						});
					}
		 		} else {
		 			System.err.println("[CONFIG-REGISTRY] The user defined and annotated type does not implement the IConfiguration interface and is thus ignored.");
		 		}
	 		} else {
	 			System.err.println("[CONFIG-REGISTRY] More than one class is annotated with @KampConfiguration. None of them is honored until only one KampConfiguration annotation is present.");
	 		}
	 	}
        
        this.ruleProvider.runEarlyHook(rules -> {
	        // 1. Inject the graph we created for DI in every method which is annotated with @KampGraph
        	//    The method must have the following parameter types: KampRuleGraph
        	
	        Set<Method> annotatedMethods = getReflectionsForSrcPackage().getMethodsAnnotatedWith(KampGraph.class);
	        annotatedMethods.stream().forEach(m -> {
		 		// find the corresponding instance
		 		for(IRule cRule : rules) {
		 			if(m.getDeclaringClass().equals(cRule.getClass())) {
		 				// try to inject graph
		 				try {
							m.invoke(cRule, this.ruleGraph);
						} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
							// TODO this might cause too much dialogs... use a newer batch-like api
							Display.getDefault().syncExec(new Runnable() {
							    public void run() {
							    	MultiStatus status = RuleProviderBase.createMultiStatus(null, e);
									Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					                ErrorDialog.openError(shell, "Dependency Injection Error", "Could not inject the KampRuleGraph into method \"" + m.getName() + "\" of class \"" + m.getDeclaringClass().getSimpleName() + "\"." + ((e instanceof IllegalArgumentException) ? "Expecting the following signature: " + m.getName() + "(KampRuleGraph)!" : ""), status);
							    }
							});
						}
		 			}
		 		}
		 	});
        });

        System.out.println("KAMP-RuleDSL bundle successfully activated.");
        context.registerService(IRuleProvider.class.getName(), ruleProvider, null);
    }
    
    private Reflections getReflectionsForSrcPackage() {
    	if(reflections == null) {
    		reflections = new Reflections(new ConfigurationBuilder()
	 			 .addClassLoaders(new ClassLoader[] { getClass().getClassLoader() })
	 		     .setUrls(ClasspathHelper.forPackage("src", getClass().getClassLoader()))
	 		     .setScanners(new SubTypesScanner(false), new TypeAnnotationsScanner(), new MethodAnnotationsScanner())
	 		     .filterInputsBy(new FilterBuilder().includePackage("src"))); 
    	}
    	
    	return reflections;
    }
    
    public void registerUsersRules() {	 	
	 	Set<Class<?>> annotatedClasses = getReflectionsForSrcPackage().getTypesAnnotatedWith(KampRule.class);
	 	
	 	annotatedClasses.stream().forEach(c -> {
	 		if(IRule.class.isAssignableFrom(c)) {
	 			Class<? extends IRule> cIRule = (Class<? extends IRule>) c;
	 			KampRuleVertex cVertex = this.ruleGraph.getVertex(cIRule);
	 			if(cVertex == null)
	 				cVertex = new KampRuleVertex(cIRule);
	 			
	 			this.ruleGraph.addVertex(cVertex);
	 			
	 			KampRule[] kampRuleAnnotations = c.getDeclaredAnnotationsByType(KampRule.class);
	 			if(kampRuleAnnotations.length > 0) {
	 				if(kampRuleAnnotations.length > 1) {
	 					System.err.println("[RULE-REGISTRY] The user defined multiple KampRule annotations. Only the first one is used. The rest is ignored.");
	 				}
	 				
	 				KampRule kampRuleAnnotation = kampRuleAnnotations[0];

		 			if(!kampRuleAnnotation.parent().getClass().equals(IRule.class)) {
		 				KampRuleVertex parentVertex = this.ruleGraph.getVertex(kampRuleAnnotation.parent());
		 				if(parentVertex == null) {
		 					parentVertex = new KampRuleVertex(kampRuleAnnotation.parent());
		 					this.ruleGraph.addVertex(parentVertex);
		 				}
		 				
		 				cVertex.setParent(parentVertex);
		 				parentVertex.addChild(cVertex);
		 				
		 				// only take the disableParent option into account if a parent rule is set
		 				if(kampRuleAnnotation.disableParent()) {
			 				cVertex.disableAllParents();
		 				} 
		 			}
		 			
		 			if(!kampRuleAnnotation.enabled()) {
		 				cVertex.setActive(false);
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
    		KampRuleVertex v = new KampRuleVertex(cRule);
    		v.setUserDefined(false);
    		this.ruleGraph.addVertex(v);
    	}
    }
}