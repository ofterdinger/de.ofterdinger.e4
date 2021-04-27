package de.ofterdinger.e4.keytool.internal.ui.action;

import de.ofterdinger.e4.keytool.internal.ui.util.TextConstants;
import de.ofterdinger.e4.keytool.internal.ui.util.TreeObject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;

abstract class AbstractKeytoolViewAction extends Action
    implements ISelectionListener, ISelectionChangedListener, ActionFactory.IWorkbenchAction {
  private TreeObject selectedTreeObject;
  private IStructuredSelection selection;
  private final IWorkbenchWindow window;

  protected AbstractKeytoolViewAction(IWorkbenchWindow window) {
    setEnabled(false);
    this.window = window;
    setText(getActionText());
    setToolTipText(getActionToolTipText());
    setImageDescriptor(getActionImage());
    window.getSelectionService().addSelectionListener(this);
    window.getWorkbench().getHelpSystem().setHelp(this, TextConstants.KEYSTORE_VIEW_HELP_ID);
  }

  public final void checkSelection(ISelection incoming) {
    if (incoming instanceof IStructuredSelection) {
      this.selection = (IStructuredSelection) incoming;
      if (this.selection.size() == 1 && this.selection.getFirstElement() instanceof TreeObject) {
        this.selectedTreeObject = (TreeObject) this.selection.getFirstElement();
        if (this.selectedTreeObject.getParent().getKeystoreFile() != null) {
          actionFiredChild();
        } else {
          actionFiredParent();
        }
      } else {
        setEnabled(false);
      }
    }
  }

  @Override
  public final void dispose() {
    this.window.getSelectionService().removeSelectionListener(this);
  }

  public TreeObject getSelectedTreeObject() {
    return this.selectedTreeObject;
  }

  public IStructuredSelection getSelection() {
    return this.selection;
  }

  public IWorkbenchWindow getWindow() {
    return this.window;
  }

  @Override
  public final void selectionChanged(IWorkbenchPart part, ISelection incoming) {
    if (part == null) {
      throw new IllegalStateException("Part is null!"); // $NON-NLS-1$
    }
    checkSelection(incoming);
  }

  @Override
  public final void selectionChanged(SelectionChangedEvent event) {
    checkSelection(event.getSelection());
  }

  protected abstract void actionFiredChild();

  protected abstract void actionFiredParent();

  protected abstract ImageDescriptor getActionImage();

  protected abstract String getActionText();

  protected abstract String getActionToolTipText();
}
