package edu.kit.ipd.sdq.kamp.ruledsl.generator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.swt.internal.ole.win32.CAUUID;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import static edu.kit.ipd.sdq.kamp.ruledsl.generator.KampRuleLanguageUtil.*;

import edu.kit.ipd.sdq.kamp.ruledsl.service.IRuleProvider;

// this is intentionally a Java class NOT xtend because of import problems for plugins calling xtend files
public class KampRuleLanguageFacade {
	private static final BundleContext bundleContext = FrameworkUtil.getBundle(KampRuleLanguageFacade.class).getBundleContext();
	
	public static class KampLanguageService implements AutoCloseable {
		private final ServiceReference<IRuleProvider> serviceReference;
		private final String projectName;
		private final IRuleProvider service;

		public KampLanguageService(String projectName) throws BundleException, CoreException {
			this.projectName = projectName;
			this.serviceReference = getServiceReference(projectName);
			
			if(this.serviceReference == null) {
				throw new IllegalStateException("ServiceReference not found! The bundle for " + projectName + " is probably not registered.");
			}
			
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
		BundleContext bundleContext = FrameworkUtil.getBundle(KampRuleLanguageFacade.class).getBundleContext();
	 	bundleContext.ungetService(serviceReference);
	}
	
	public static ServiceReference<IRuleProvider> getServiceReference(String sourceProjectName) throws BundleException, CoreException {
    	Bundle dslBundle = installIfNecessaryAndGetBundle(sourceProjectName, new NullProgressMonitor());
	    
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

	public static Bundle installIfNecessaryAndGetBundle(String sourceProjectName, IProgressMonitor mon) throws BundleException, CoreException {
		SubMonitor subMonitor = SubMonitor.convert(mon, "Installing Dsl Bundle", 6);
		
		subMonitor.split(1).beginTask("Search for bundle", 3);
		Bundle dslBundle = null;
	    /* lookup the kamp dsl bundle */
	    for(Bundle bundle : bundleContext.getBundles()) {
	    	if(bundle.getSymbolicName() != null && bundle.getSymbolicName().equals(getBundleNameForProjectName(sourceProjectName))) {
	   	  		dslBundle = bundle;
	   	  	}
	    }
	   
	    /* if the bundle is not already loaded, try to load in manually */
	    
	    if(dslBundle == null) {
	    	subMonitor.split(1).beginTask("Install bundle at OSGi layer", 1);
	    	System.out.println("Registering bundle manually...");
	    	Bundle startedBundle = buildProjectAndInstall(sourceProjectName, subMonitor.split(3));
	    	// wait for bundle to start
	    	// TODO is busy waiting ok in KAMP here?
	    	subMonitor.split(1).beginTask("Wait for bundle state ACTIVE", 1);
	    	while(startedBundle != null && startedBundle.getState() != Bundle.ACTIVE) {};
	    	
	    	dslBundle = startedBundle;
	    }
	    subMonitor.done();
	    
	    return dslBundle;
	}

	// basically a convenience method
	public static Bundle buildProjectAndInstall(String sourceProjectName, IProgressMonitor monitor) throws BundleException, CoreException {
		buildProject(getProject(sourceProjectName), null);
    	
		return installAndStartProjectBundle(sourceProjectName, monitor);
	}
	
	// basically a convenience method
	public static Bundle buildProjectAndReInstall(String sourceProjectName, IProgressMonitor monitor) throws CoreException, BundleException {
		buildProject(getProject(sourceProjectName), null);
    	
		return registerProjectBundle(getProject(sourceProjectName), getDslBundle(sourceProjectName), monitor);
	}

	public static KampLanguageService getInstance(String projectName) throws BundleException, CoreException {
		return new KampLanguageService(projectName);
	}
	
	// a modified folder at root indicates that the given project is a kamp project
	public static boolean isKampProjectFolder(IProject project) {
		return project.getFolder("modified").exists();
	}

	public static boolean isKampDslRuleProjectFolder(IProject project) {
		return project.getName().endsWith("-rules");
	}
}
