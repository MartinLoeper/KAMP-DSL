package edu.kit.ipd.sdq.kamp.ruledsl.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;

import edu.kit.ipd.sdq.kamp.ruledsl.ErrorHandlingUtil;
import edu.kit.ipd.sdq.kamp.ruledsl.generator.KampRuleLanguageGenerator;
import edu.kit.ipd.sdq.kamp.ruledsl.support.KampRuleLanguageFacade;
import edu.kit.ipd.sdq.kamp.ruledsl.support.KampRuleLanguageUtil;
import edu.kit.ipd.sdq.kamp.util.FileAndFolderManagement;

public class RegisterDslBundleAction implements IActionDelegate {

	private ISelection selection;
	private String sourceProjectName;
	private IProject selectedProject;

	@Override
	public void run(IAction action) {
	   IWorkbench wb = PlatformUI.getWorkbench();
	   IProgressService ps = wb.getProgressService();
	   try {
			ps.busyCursorWhile(new IRunnableWithProgress() {
		      public void run(IProgressMonitor monitor) {
		  		try {
					KampRuleLanguageFacade.buildProjectAndReInstall(sourceProjectName, monitor);
				} catch (CoreException | BundleException e) {
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	                ErrorDialog.openError(shell, "Error", "The bundle could not be successfully created and injected.", ErrorHandlingUtil.createMultiStatus(FrameworkUtil.getBundle(KampRuleLanguageUtil.class).getSymbolicName(), e.getLocalizedMessage(), e));
				}
		      }
		   });
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
		IContainer selectedFolder = FileAndFolderManagement.retrieveSelectedFolder(this.getSelection());
		
		if(!(selectedFolder instanceof IProject)) {
			action.setEnabled(false);
		} else {
			this.selectedProject = (IProject) selectedFolder;
			if(KampRuleLanguageFacade.isKampDslRuleProjectFolder(this.selectedProject)) {
				action.setEnabled(true);
				String dslProjectName = this.selectedProject.getName();
				sourceProjectName = dslProjectName.substring(0, dslProjectName.length() - 6);
			} else if(KampRuleLanguageFacade.isKampProjectFolder(this.selectedProject)) {
				if(!KampRuleLanguageUtil.getProject(this.selectedProject.getName()).exists()) {
					action.setEnabled(false);
				} else {
					action.setEnabled(true);
				}
				sourceProjectName = this.selectedProject.getName();
			} else {
				action.setEnabled(false);	
			}
		}
	}
	
	protected ISelection getSelection() {
		return selection;
	}
}
