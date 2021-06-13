package de.ofterdinger.ide.eclipse.keytool.internal.ui.util;

import static de.ofterdinger.ide.eclipse.keytool.internal.KeytoolPlugin.PLUGIN_ID;
import static org.eclipse.core.runtime.IStatus.ERROR;

import de.ofterdinger.ide.eclipse.keytool.internal.KeystoreFile;
import de.ofterdinger.ide.eclipse.keytool.internal.KeytoolPlugin;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import org.eclipse.core.runtime.Status;

public class TreeParent extends TreeObject {
  private final List<TreeObject> children = new ArrayList<>();
  private final KeystoreFile keystoreFile;

  public TreeParent(KeystoreFile keystoreFile, String name) {
    super(name, false);
    this.keystoreFile = keystoreFile;
  }

  public void addChild(TreeObject child) {
    this.children.add(child);
    child.setParent(this);
  }

  public TreeObject[] getChildren() {
    return this.children.toArray(new TreeObject[this.children.size()]);
  }

  public KeyStore getKeyStore() {
    if (this.keystoreFile != null) {
      return this.keystoreFile.getKeystore();
    }
    return null;
  }

  public KeystoreFile getKeystoreFile() {
    return this.keystoreFile;
  }

  public String getKeystoreFilename() {
    if (this.keystoreFile != null) {
      return this.keystoreFile.getKeystorefilename();
    }
    return null;
  }

  public boolean hasChildren() {
    return !this.children.isEmpty();
  }

  public void populateKeystoreToNode(KeystoreFile keystoreFile) {
    KeyStore keystore = keystoreFile.getKeystore();
    try {
      Enumeration<String> enumeration = keystore.aliases();
      while (enumeration.hasMoreElements()) {
        String alias = enumeration.nextElement();
        boolean hasPrivateKey = keystore.isKeyEntry(alias);
        addChild(new TreeObject(alias, hasPrivateKey));
      }
    } catch (Exception e) {
      KeytoolPlugin.getDefault().getLog().log(new Status(ERROR, PLUGIN_ID, e.getMessage(), e));
    }
  }

  public void removeAllChildren() {
    this.children.clear();
  }

  public void removeChild(TreeObject child) {
    this.children.remove(child);
    child.setParent(null);
  }
}
