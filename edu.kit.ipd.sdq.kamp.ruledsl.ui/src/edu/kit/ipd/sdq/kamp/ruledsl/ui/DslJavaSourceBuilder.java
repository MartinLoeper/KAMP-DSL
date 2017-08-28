package edu.kit.ipd.sdq.kamp.ruledsl.ui;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;

import edu.kit.ipd.sdq.kamp.ruledsl.generator.KampRuleLanguageGenerator;
import edu.kit.ipd.sdq.kamp.ruledsl.generator.KarlJobBase;
import edu.kit.ipd.sdq.kamp.ruledsl.support.KampRuleLanguageFacade;
import edu.kit.ipd.sdq.kamp.ruledsl.support.KampRuleLanguageUtil;
import edu.kit.ipd.sdq.kamp.ruledsl.util.ErrorHandlingUtil;

public class DslJavaSourceBuilder extends IncrementalProjectBuilder {
	 
	 public static final String BUILDER_ID = "edu.kit.ipd.sdq.kamp.ruledsl.ui.sourceBuilder";
	 
	 @Override
	 protected IProject[] build(final int kind, final Map<String, String> args, final IProgressMonitor monitor) throws CoreException {
	   if(KarlJobBase.isKarlJobRunning()) {
		   return new IProject[] { getProject() };
	   }
		 
	  SubMonitor subMonitor = SubMonitor.convert(monitor, "Build and register project", 10);
		 
	  System.out.println("A file was saved. Trigger the custom builder.");
	  
	  if (kind == IncrementalProjectBuilder.AUTO_BUILD || kind == IncrementalProjectBuilder.INCREMENTAL_BUILD) {
		 IResourceDelta delta = getDelta(getProject());
	  
	      if(delta != null) {
	    	  IResourceDelta[] projDeltas = delta.getAffectedChildren(
	  				IResourceDelta.CHANGED|
	  				IResourceDelta.ADDED|
	  				IResourceDelta.REMOVED
	  		);
	  		for (int i = 0; i < projDeltas.length; ++i) {
	  			IResource resource = projDeltas[i].getResource();
	  			if(resource.getName().equals("src")) {
	  				String projectName = getProject().getName();
	  				try {
						KampRuleLanguageFacade.buildProjectAndReInstall(projectName.substring(0, projectName.length() - 6), subMonitor.split(10));
					} catch (OperationCanceledException | BundleException e) {
						PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
							@Override
							public void run() {
								Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
								ErrorDialog.openError(shell, "Error", "The bundle could not be successfully created and injected.", ErrorHandlingUtil.createMultiStatus(FrameworkUtil.getBundle(KampRuleLanguageUtil.class).getSymbolicName(), e.getLocalizedMessage(), e));
								Throwable t = ((BundleException) e).getNestedException();
								if(t != null) {
									t.printStackTrace();
								}
							}
						});
					}
	  			} else if(resource.getName().equals("gen")) {
	  				// we have to check if the build was triggered by a user change... how to do that?
//	  				PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
//
//	  		            @Override
//	  		            public void run() {
//	  		            	MessageBox dialog = new MessageBox(PlatformUI.getWorkbench().getDisplay().getActiveShell(), SWT.ICON_WARNING | SWT.OK);
//	  						dialog.setText("Warning");
//	  						dialog.setMessage("Making changes to the gen package is strongly discouraged.");
//	  			
//	  						dialog.open();
//	  		            }
//	  		        });
	  			}
	  		}
	      }
	  }

	  return new IProject[] { getProject() };
	 }
	}