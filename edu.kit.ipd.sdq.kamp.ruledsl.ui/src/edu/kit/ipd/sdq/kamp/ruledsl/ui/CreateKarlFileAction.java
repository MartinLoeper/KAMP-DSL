package edu.kit.ipd.sdq.kamp.ruledsl.ui;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.PlatformUI;

import edu.kit.ipd.sdq.kamp.ruledsl.support.KampRuleLanguageFacade;
import edu.kit.ipd.sdq.kamp.util.FileAndFolderManagement;

public class CreateKarlFileAction implements IActionDelegate {

	private ISelection selection;
	private IProject selectedProject;

	@Override
	public void run(IAction action) {
		if(action.isEnabled()) {
			if(this.selectedProject != null) {
				IFile file = this.selectedProject.getFile("rules.karl");
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				if(file.exists()) {
					PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

	  		            @Override
	  		            public void run() {
	  		            	MessageBox dialog = new MessageBox(PlatformUI.getWorkbench().getDisplay().getActiveShell(), SWT.ICON_ERROR | SWT.ABORT);
	  						dialog.setText("Fehler");
	  						dialog.setMessage("There is already a rule definition file in this project.");

	  						dialog.open();
	  		            }
	  		        });
				} else {
					try {
						file.create(new ByteArrayInputStream( new byte[0] ), false, new NullProgressMonitor());
					} catch (CoreException e) {
						MessageDialog.openError(shell, "Error", e.getMessage());
					}
				}
			}
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
			if(KampRuleLanguageFacade.isKampProjectFolder(this.selectedProject)) {
				action.setEnabled(true);
			} else {
				action.setEnabled(false);	
			}
		}
	}
	
	protected ISelection getSelection() {
		return selection;
	}

}
