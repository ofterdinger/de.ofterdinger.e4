package de.ofterdinger.ide.eclipse.keytool.internal.ui.util;

import org.eclipse.core.runtime.PlatformObject;

public class TreeObject extends PlatformObject {
  private final String name;
  private TreeParent parent;
  private boolean privateKey;

  public TreeObject(String name, boolean hasPrivateKey) {
    this.name = name;
    this.privateKey = hasPrivateKey;
  }

  public String getName() {
    return this.name;
  }

  public TreeParent getParent() {
    return this.parent;
  }

  public boolean hasParentKeystore() {
    return (this.parent != null && this.parent.getKeystoreFile() != null);
  }

  public boolean isPrivateKey() {
    return this.privateKey;
  }

  public void setParent(TreeParent parent) {
    this.parent = parent;
  }

  public void setPrivateKey(boolean privateKey) {
    this.privateKey = privateKey;
  }

  @Override
  public String toString() {
    return getName();
  }
}
