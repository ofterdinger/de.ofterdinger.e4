package de.ofterdinger.ide.eclipse.keytool.internal.ui.action;

import static de.ofterdinger.ide.eclipse.keytool.internal.filechanged.FileChangedEvent.FILE_REMOVED;
import static de.ofterdinger.ide.eclipse.keytool.internal.ui.util.ImageKeys.KEY_CLOSE;

import de.ofterdinger.ide.eclipse.keytool.internal.KeystoreFile;
import de.ofterdinger.ide.eclipse.keytool.internal.filechanged.FileChangedEvent;
import de.ofterdinger.ide.eclipse.keytool.internal.ui.KeyStoreView;
import de.ofterdinger.ide.eclipse.keytool.internal.ui.util.ImageKeys;
import de.ofterdinger.ide.eclipse.keytool.internal.ui.util.TreeObject;
import de.ofterdinger.ide.eclipse.keytool.internal.ui.util.TreeParent;
import de.ofterdinger.ide.eclipse.keytool.internal.ui.util.TreeUpdater;
import java.util.List;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;

public class CloseKeystoreAction extends AbstractKeytoolViewAction {
  private final TreeViewer viewer;

  public CloseKeystoreAction(IWorkbenchWindow window, TreeViewer viewer) {
    super(window);
    this.viewer = viewer;
  }

  @Override
  public final void run() {
    TreeObject selectedTreeObject = getSelectedTreeObject();
    if (selectedTreeObject != null) {
      Display display = getWindow().getWorkbench().getDisplay();
      String filename =
          selectedTreeObject instanceof TreeParent
              ? ((TreeParent) selectedTreeObject).getKeystoreFilename()
              : selectedTreeObject.getParent().getKeystoreFilename();
      FileChangedEvent aEvt = new FileChangedEvent(FILE_REMOVED, filename);
      List<KeystoreFile> keystores = KeyStoreView.getKeystores();
      if (keystores != null) {
        int i = 0;
        while (i < keystores.size()) {
          KeystoreFile file = keystores.get(i);
          if (file.getKeystorefilename().equals(filename)) {
            keystores.remove(i);
            --i;
          }
          ++i;
        }
      }
      display.syncExec(new TreeUpdater(aEvt, this.viewer));
    }
  }

  @Override
  protected void actionFiredChild() {
    setEnabled(true);
  }

  @Override
  protected void actionFiredParent() {
    setEnabled(true);
  }

  @Override
  protected ImageDescriptor getActionImage() {
    return ImageKeys.getImageDescriptor(KEY_CLOSE);
  }

  @Override
  protected String getActionText() {
    return "&Close Keystore";
  }

  @Override
  protected String getActionToolTipText() {
    return "Close the current keystore.";
  }
}
