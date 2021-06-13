package de.ofterdinger.ide.eclipse.keytool.internal.ui.action;

import de.ofterdinger.ide.eclipse.keytool.internal.ui.wizard.ImportCertificateWizard;
import java.io.File;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;

public class ImportCertificateAction implements IObjectActionDelegate {
  private File selectedFile;
  private IWorkbenchPartSite site;

  @Override
  public void run(IAction action) {
    if (this.selectedFile != null && this.site != null) {
      ImportCertificateWizard wizard = new ImportCertificateWizard();
      wizard.setFileToImport(this.selectedFile);
      WizardDialog dialog = new WizardDialog(this.site.getShell(), wizard);
      dialog.create();
      dialog.open();
    }
  }

  @Override
  public void selectionChanged(IAction action, ISelection selection) {
    if (selection instanceof TreeSelection) {
      TreeSelection treeSelection = (TreeSelection) selection;
      Object[] selections = treeSelection.toArray();
      int i = 0;
      while (i < selections.length) {
        if (selections[i] instanceof IFile) {
          IFile file = (IFile) selections[i];
          if (file.exists()) {
            this.selectedFile = file.getLocation().toFile();
          }
        }
        ++i;
      }
    }
  }

  @Override
  public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    this.site = targetPart.getSite();
  }
}
