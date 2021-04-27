package de.ofterdinger.e4.keytool.internal.ui.editor;

import de.ofterdinger.e4.keytool.internal.certificate.CompleteCertificate;
import de.ofterdinger.e4.keytool.internal.ui.util.ImageKeys;
import de.ofterdinger.e4.keytool.internal.ui.util.TextConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.SubMenuManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.part.MultiPageEditorActionBarContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

public class CertificateEditorContributor extends MultiPageEditorActionBarContributor {
  private IEditorPart activeEditorPart;
  private ExportCertificateAction exportAction;

  public CertificateEditorContributor() {
    createActions();
  }

  @Override
  public final void contributeToMenu(IMenuManager manager) {
    IContributionItem item = manager.find("keytool.menus.keytool"); // $NON-NLS-1$
    if (item instanceof SubMenuManager) {
      SubMenuManager subManager = (SubMenuManager) item;
      subManager.add(this.exportAction);
    }
  }

  @Override
  public final void contributeToToolBar(IToolBarManager manager) {
    manager.add(new Separator());
    manager.add(this.exportAction);
  }

  @Override
  public void setActiveEditor(IEditorPart activeEditorPart) {
    if (activeEditorPart != null) {
      CompleteCertificate completeCertificate =
          ((CertificateEditor) activeEditorPart).getCompleteCertificate();
      this.exportAction.setCompleteCertificate(completeCertificate);
    }
    super.setActiveEditor(activeEditorPart);
  }

  @Override
  public final void setActivePage(IEditorPart part) {
    if (this.activeEditorPart == part) {
      return;
    }
    this.activeEditorPart = part;
    this.exportAction.setShell(part.getSite().getShell());
    IActionBars actionBars = getActionBars();
    if (actionBars != null) {
      ITextEditor editor = part instanceof ITextEditor ? (ITextEditor) part : null;
      actionBars.setGlobalActionHandler(
          ActionFactory.DELETE.getId(), getAction(editor, ITextEditorActionConstants.DELETE));
      actionBars.setGlobalActionHandler(
          ActionFactory.UNDO.getId(), getAction(editor, ITextEditorActionConstants.UNDO));
      actionBars.setGlobalActionHandler(
          ActionFactory.REDO.getId(), getAction(editor, ITextEditorActionConstants.REDO));
      actionBars.setGlobalActionHandler(
          ActionFactory.CUT.getId(), getAction(editor, ITextEditorActionConstants.CUT));
      actionBars.setGlobalActionHandler(
          ActionFactory.COPY.getId(), getAction(editor, ITextEditorActionConstants.COPY));
      actionBars.setGlobalActionHandler(
          ActionFactory.PASTE.getId(), getAction(editor, ITextEditorActionConstants.PASTE));
      actionBars.setGlobalActionHandler(
          ActionFactory.SELECT_ALL.getId(),
          getAction(editor, ITextEditorActionConstants.SELECT_ALL));
      actionBars.setGlobalActionHandler(
          ActionFactory.FIND.getId(), getAction(editor, ITextEditorActionConstants.FIND));
      actionBars.setGlobalActionHandler(
          IDEActionFactory.BOOKMARK.getId(), getAction(editor, IDEActionFactory.BOOKMARK.getId()));
      actionBars.updateActionBars();
    }
    CompleteCertificate completeCertificate =
        ((CertificateEditor) this.activeEditorPart).getCompleteCertificate();
    this.exportAction.setCompleteCertificate(completeCertificate);
  }

  private void createActions() {
    this.exportAction = new ExportCertificateAction(null);
    this.exportAction.setImageDescriptor(
        ImageKeys.getImageDescriptor(ImageKeys.EXPORT_CERTIFICATE));
    this.exportAction.setId("Keytool.actions.exportAction"); // $NON-NLS-1$
    PlatformUI.getWorkbench()
        .getHelpSystem()
        .setHelp(this.exportAction, TextConstants.KEYSTORE_VIEW_HELP_ID);
  }

  static final IAction getAction(ITextEditor editor, String actionID) {
    return editor == null ? null : editor.getAction(actionID);
  }
}
