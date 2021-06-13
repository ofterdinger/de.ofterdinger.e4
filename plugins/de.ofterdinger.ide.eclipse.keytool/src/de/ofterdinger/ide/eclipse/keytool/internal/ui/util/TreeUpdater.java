package de.ofterdinger.ide.eclipse.keytool.internal.ui.util;

import static de.ofterdinger.ide.eclipse.keytool.internal.KeytoolPlugin.PLUGIN_ID;
import static org.eclipse.core.runtime.IStatus.ERROR;

import de.ofterdinger.ide.eclipse.keytool.internal.KeystoreFile;
import de.ofterdinger.ide.eclipse.keytool.internal.KeytoolPlugin;
import de.ofterdinger.ide.eclipse.keytool.internal.certificate.CertTools;
import de.ofterdinger.ide.eclipse.keytool.internal.certificate.KeystoreType;
import de.ofterdinger.ide.eclipse.keytool.internal.filechanged.FileChangedEvent;
import de.ofterdinger.ide.eclipse.keytool.internal.ui.KeyStoreView;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.TreeViewer;

public class TreeUpdater implements Runnable {
  private final FileChangedEvent fileChangedEvent;
  private final TreeViewer viewer;

  public TreeUpdater(FileChangedEvent fileChangedEvent, TreeViewer viewer) {
    this.fileChangedEvent = fileChangedEvent;
    this.viewer = viewer;
  }

  @Override
  public void run() {
    TreeParent data = (TreeParent) this.viewer.getInput();
    if (data != null) {
      if (this.fileChangedEvent.getOperationType() == FileChangedEvent.FILE_ADDED) {
        String keystoreFilename = this.fileChangedEvent.getFileName();
        String keystorePassword = this.fileChangedEvent.getPassword();
        if (KeyStoreView.getKeystoreFile(keystoreFilename) != null) {
          this.viewer.refresh();
          return;
        }
        KeystoreType keystoreType = this.fileChangedEvent.getKeystoreType();
        try {
          KeystoreFile keystoreFile =
              CertTools.loadKeystoreFile(keystoreFilename, keystoreType, keystorePassword);
          TreeParent treeParent = new TreeParent(keystoreFile, keystoreFilename);
          treeParent.populateKeystoreToNode(keystoreFile);
          data.addChild(treeParent);
          KeyStoreView.getKeystores().add(keystoreFile);
        } catch (Exception e) {
          KeytoolPlugin.getDefault().getLog().log(new Status(ERROR, PLUGIN_ID, e.getMessage(), e));
        }
      } else {
        TreeObject[] children = data.getChildren();
        int i = 0;
        while (i < children.length) {
          if (children[i] instanceof TreeParent) {
            TreeParent parent = (TreeParent) children[i];
            if (this.fileChangedEvent.getFileName().equals(parent.getKeystoreFilename())) {
              switch (this.fileChangedEvent.getOperationType()) {
                case FileChangedEvent.FILE_REMOVED:
                  data.removeChild(parent);
                  break;
                case FileChangedEvent.FILE_UPDATED:
                  updateFile(parent);
                  break;
                default:
                  break;
              }
            }
          }
          ++i;
        }
      }
      this.viewer.refresh();
    }
  }

  private static void updateFile(TreeParent parent) {
    try {
      CertTools.loadKeystoreFile(parent.getKeystoreFile());
      parent.removeAllChildren();
      parent.populateKeystoreToNode(parent.getKeystoreFile());
    } catch (IOException | NoSuchAlgorithmException | CertificateException e) {
      KeytoolPlugin.getDefault().getLog().log(new Status(ERROR, PLUGIN_ID, e.getMessage(), e));
    }
  }
}
