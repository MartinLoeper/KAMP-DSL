package edu.kit.ipd.sdq.kamp.ruledsl.generator;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import edu.kit.ipd.sdq.kamp.ruledsl.generator.KampRuleLanguageFacade.KampLanguageService;

// this is intentionally a Java class NOT xtend because of import problems for plugins calling xtend files
public class KampRuleLanguageFacade {
	private static final BundleContext bundleContext = FrameworkUtil.getBundle(KampRuleLanguageFacade.class).getBundleContext();
	
	public static class KampLanguageService implements AutoCloseable {
		private final ServiceReference<IRuleProvider> serviceReference;
		private final String projectName;
		private final IRuleProvider service;

		public KampLanguageService(String projectName) {
			this.projectName = projectName;
			this.serviceReference = getServiceReference(projectName);
			
			this.service = bundleContext.getService(serviceReference);
			
			if(this.service == null) {
				throw new IllegalStateException("Service not found! The bundle for " + projectName + " is probably not registered.");
			} else {
				System.out.println("Dsl Service for project obtained: " + projectName);
			}
		}
		
		public IRuleProvider getService() {
			return this.service;
		}

		@Override
		public void close() throws Exception {
			if(this.serviceReference != null)
				unregisterService(serviceReference);
			
			System.out.println("Leaving DSL bundle service lookup.");
		}
		
	}
	
	public static void unregisterService(ServiceReference<IRuleProvider> serviceReference) {	
		BundleContext bundleContext = FrameworkUtil.getBundle(KampRuleLanguageGenerator.class).getBundleContext();
	 	bundleContext.ungetService(serviceReference);
	}
	
	public static ServiceReference<IRuleProvider> getServiceReference(String sourceProjectName) {
    	BundleContext bundleContext = FrameworkUtil.getBundle(KampRuleLanguageGenerator.class).getBundleContext();

		Bundle dslBundle = null;
	    /* lookup the kamp dsl bundle */
	    for(Bundle bundle : bundleContext.getBundles()) {
	    	if(bundle.getSymbolicName().equals(KampRuleLanguageGenerator.getBundleNameForProjectName(sourceProjectName))) {
	   	  		dslBundle = bundle;
	   	  	}
	    }
	   
	    /* if the bundle is not already loaded, try to load in manually */
	    
	    if(dslBundle == null) {
	    	System.out.println("Registering bundle manually...");
	    	Bundle startedBundle = edu.kit.ipd.sdq.kamp.ruledsl.generator.KampRuleLanguageGenerator.installAndStartProjectBundle(sourceProjectName);
	    	// wait for bundle to start
	    	// TODO is busy waiting ok in KAMP here?
	    	while(startedBundle != null && startedBundle.getState() != Bundle.ACTIVE) {};
	    	
	    	dslBundle = startedBundle;
	    }
	    
	    if(dslBundle != null) {
	 		 System.out.println("Found bundle with additional propagation rules. State: " + dslBundle.getState());
	 		 if(dslBundle.getState() == Bundle.ACTIVE) {
	 			@SuppressWarnings("unchecked")
				ServiceReference<IRuleProvider> serviceReference = (ServiceReference<IRuleProvider>) bundleContext.getServiceReference(IRuleProvider.class.getName());
	 			
	 			return serviceReference;
	 		 } else {
	 			 System.err.println("Bundle not activated??! Error.");
	 			 // TODO handle error
	 		 }
	    } else {
	    	System.err.println("Could not start custom DSL bundle.");
	    }
	    
	    return null;
	}

	public static KampLanguageService getService(String projectName) {
		return new KampLanguageService(projectName);
	}
}
