package edu.kit.ipd.sdq.kamp.ruledsl.generator

import edu.kit.ipd.sdq.kamp.ruledsl.util.ErrorContext
import edu.kit.ipd.sdq.kamp.ruledsl.util.RollbarExceptionReporting
import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.Status
import org.eclipse.core.runtime.SubMonitor
import org.osgi.framework.Bundle

import static edu.kit.ipd.sdq.kamp.ruledsl.support.KampRuleLanguageUtil.*

public class KarlProjectUpdateJob extends KarlJobBase {			
    
	new(Configuration config) {
		super(config, "Update rules for " + config.assignedProjectName)
	}
	
	override protected run(IProgressMonitor mon) {
		val SubMonitor subMonitor = SubMonitor.convert(mon, name, 18);
			
		try {
		   	var IProject project = getProject(config.assignedProjectName)
		   	removeGeneratedFolderContents(project, subMonitor.split(1));
		   		
		   	createManifest(getBundleNameForProjectName(config.assignedProjectName), project, config.packageUris, subMonitor.split(1), config.resource.contents)	
		   	syncManifests(project, config.assignedProjectName);
		    createActivator(project, subMonitor.split(1), config.getRootRuleFile)
			//createServiceBase(project, subMonitor.split(1));
		   	moveRuleSourceFiles(subMonitor.split(1), project, config.sourceFileUris, config.sourceFileNames);
		    
		   	buildProject(project, subMonitor.split(1));
		   	project.refreshLocal(IProject.DEPTH_INFINITE, subMonitor.split(1))
		   	
		   	waitForBuild(subMonitor.split(1));
		   	
		   	val Bundle dslBundle = getDslBundle(config.assignedProjectName);
		   	if(dslBundle !== null) {
		   		registerProjectBundle(project, dslBundle, subMonitor.split(2))
		   	} else {
		   		installAndStartProjectBundle(project, subMonitor.split(1))
		   	}
		   	
		   	println("DONE")
		   	mon.done
		    Status.OK_STATUS
		} catch(Exception e) {
			RollbarExceptionReporting.INSTANCE.log(e, ErrorContext.PROJECT_BUILD, null);
			
			return new Status(Status.ERROR, BUNDLE_NAME, "Die Regeln konnten nicht eingefügt werden.", e);
		}
	}	
}
