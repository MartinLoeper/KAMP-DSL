package edu.kit.ipd.sdq.kamp.ruledsl.generator;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

// this is intentionally a Java class NOT xtend because of import problems for plugins calling xtend files
public class KampRuleLanguageFacade {
	public static void unregisterService(ServiceReference<IRuleProvider> serviceReference) {	
		BundleContext bundleContext = FrameworkUtil.getBundle(KampRuleLanguageGenerator.class).getBundleContext();
	 	bundleContext.ungetService(serviceReference);
	}
	
	public static ServiceReference<IRuleProvider> getServiceReference() {
    	BundleContext bundleContext = FrameworkUtil.getBundle(KampRuleLanguageGenerator.class).getBundleContext();

		Bundle dslBundle = null;
	    /* lookup the kamp dsl bundle */
	    for(Bundle bundle : bundleContext.getBundles()) {
	    	if(bundle.getSymbolicName().equals(edu.kit.ipd.sdq.kamp.ruledsl.generator.KampRuleLanguageGenerator.BUNDLE_NAME)) {
	   	  		dslBundle = bundle;
	   	  	}
	    }
	   
	    /* if the bundle is not already loaded, try to load in manually */
	    // TODO find the name of the calling project dynamically
	    String callerProjectName = "MartinTest1";
	    
	    if(dslBundle == null) {
	    	System.out.println("Registering bundle manually...");
	    	Bundle startedBundle = edu.kit.ipd.sdq.kamp.ruledsl.generator.KampRuleLanguageGenerator.installAndStartProjectBundle(callerProjectName);
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
}
