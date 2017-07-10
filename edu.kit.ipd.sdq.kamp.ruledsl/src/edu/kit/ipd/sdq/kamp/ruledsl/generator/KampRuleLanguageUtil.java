package edu.kit.ipd.sdq.kamp.ruledsl.generator;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import java.util.Base64;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

class KampRuleLanguageUtil {
	public static IWorkspace workspace = ResourcesPlugin.getWorkspace();
    public static IWorkspaceRoot root = workspace.getRoot();
	
	public static final String BUNDLE_NAME = "edu.kit.ipd.sdq.kamp.ruledsl.lookup.bundle";
	
	public static IProject getProject(String causingProjectName) {
		return root.getProject(causingProjectName + "-rules");
	}
	
	public static String getBundleNameForProjectName(String name) {
		return BUNDLE_NAME + "." + Base64.getEncoder().withoutPadding().encodeToString(name.getBytes()).toLowerCase();
	}
	
	public static void buildProject(IProject project, IProgressMonitor monitor) throws CoreException {
		IProgressMonitor mon = monitor;
		if(mon == null) {
			mon = new NullProgressMonitor();
		}
		
		project.build(IncrementalProjectBuilder.AUTO_BUILD, mon);
	}
	
	static Bundle getDslBundle(String projectName) {
		BundleContext bundleContext = FrameworkUtil.getBundle(KampRuleLanguageUtil.class).getBundleContext();
	   	Bundle cBundle = null;
	    for(Bundle bundle : bundleContext.getBundles()) {
	    	if(bundle.getSymbolicName() != null && bundle.getSymbolicName().equals(getBundleNameForProjectName(projectName))) {
	   	  		cBundle = bundle;
	   	  	}
	    }
	    
	    return cBundle;
	}
	
	// unregisteres the given bundle if available and registers it again
	static Bundle registerProjectBundle(IProject project, Bundle bundle, IProgressMonitor monitor) throws BundleException {
	    // if there was a bundle registered, wait for the shutdown
	    if(bundle != null) {
	    	monitor.beginTask("Waiting for bundle to be uninstalled", 1);
	    	System.out.println("DSL Bundle found, uninstalling...");
	    	bundle.uninstall();
	    	// is busy wait ok here as we are in a job?
	    	while(bundle.getState() != Bundle.UNINSTALLED) {};
	    	System.out.println("Bundle is finally uninstalled!");
	    	monitor.worked(1);
	    }
	    
	    System.out.println("Installing new bundle version...");
	    return installAndStartProjectBundle(project, monitor);
	}
	
	// convenice method for external calls
	// returns null if project does not exist, returns installed bundle otherwise
	static Bundle installAndStartProjectBundle(String callerProjectName, IProgressMonitor monitor) throws BundleException {
		 IProject project = getProject(callerProjectName);
		if(!project.exists()) {
			return null;	
		}
		
		return installAndStartProjectBundle(project, monitor);
	}
		
	static Bundle installAndStartProjectBundle(IProject project, IProgressMonitor monitor) throws BundleException {
		// TODO get name by workspace methods
		monitor.beginTask("Starting bundle", 1);
		BundleContext bundleContext = FrameworkUtil.getBundle(KampRuleLanguageUtil.class).getBundleContext();
	    // the bin directory of the plugin
	    IFolder folder = project.getFolder("bin");
	    Bundle b;
		b = bundleContext.installBundle("file:" + folder.getLocation().toString());
	    b.start();
	    monitor.worked(1);
	    
	    return b;
	}
}
