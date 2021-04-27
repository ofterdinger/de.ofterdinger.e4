package de.ofterdinger.e4.keytool.internal;

import static org.apache.commons.lang3.ArrayUtils.EMPTY_OBJECT_ARRAY;
import static org.eclipse.ui.plugin.AbstractUIPlugin.imageDescriptorFromPlugin;

import de.ofterdinger.e4.keytool.internal.ui.util.ImageKeys;
import de.ofterdinger.e4.keytool.internal.ui.util.TreeChainObject;
import de.ofterdinger.e4.keytool.internal.ui.util.TreeObject;
import de.ofterdinger.e4.keytool.internal.ui.util.TreeParent;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class KeystoreAdapterFactory implements IAdapterFactory {
  private IWorkbenchAdapter treeChainObjectAdapter;
  private IWorkbenchAdapter treeObjectAdapter;
  private IWorkbenchAdapter treeParentAdapter;

  public KeystoreAdapterFactory() {
    this.treeObjectAdapter =
        new IWorkbenchAdapter() {

          @Override
          public Object[] getChildren(Object o) {
            if (o instanceof TreeObject) {
              return new Object[0];
            }
            return EMPTY_OBJECT_ARRAY;
          }

          @Override
          public ImageDescriptor getImageDescriptor(Object object) {
            if (object instanceof TreeObject) {
              TreeObject treeObj = (TreeObject) object;
              if (treeObj.isPrivateKey()) {
                return imageDescriptorFromPlugin(KeytoolPlugin.PLUGIN_ID, ImageKeys.LOCK_CLOSED);
              }
              return imageDescriptorFromPlugin(KeytoolPlugin.PLUGIN_ID, ImageKeys.LOCK_OPEN);
            }
            return null;
          }

          @Override
          public String getLabel(Object o) {
            return ((TreeObject) o).getName();
          }

          @Override
          public Object getParent(Object o) {
            return ((TreeObject) o).getParent();
          }
        };
    this.treeParentAdapter =
        new IWorkbenchAdapter() {

          @Override
          public Object[] getChildren(Object o) {
            if (o instanceof TreeObject) {
              return new Object[0];
            }
            return EMPTY_OBJECT_ARRAY;
          }

          @Override
          public ImageDescriptor getImageDescriptor(Object object) {
            if (object instanceof TreeObject) {
              return imageDescriptorFromPlugin(KeytoolPlugin.PLUGIN_ID, ImageKeys.ICON_KEY);
            }
            return null;
          }

          @Override
          public String getLabel(Object o) {
            return ((TreeObject) o).getName();
          }

          @Override
          public Object getParent(Object o) {
            return ((TreeObject) o).getParent();
          }
        };
    this.treeChainObjectAdapter =
        new IWorkbenchAdapter() {

          @Override
          public Object[] getChildren(Object o) {
            if (o instanceof TreeChainObject) {
              return new Object[0];
            }
            return EMPTY_OBJECT_ARRAY;
          }

          @Override
          public ImageDescriptor getImageDescriptor(Object object) {
            if (object instanceof TreeChainObject) {
              return imageDescriptorFromPlugin(KeytoolPlugin.PLUGIN_ID, ImageKeys.CERTIFICATE);
            }
            return null;
          }

          @Override
          public String getLabel(Object o) {
            return ((TreeChainObject) o).getName();
          }

          @Override
          public Object getParent(Object o) {
            return ((TreeChainObject) o).getParent();
          }
        };
  }

  @Override
  public Object getAdapter(Object adaptableObject, Class adapterType) {
    if (adapterType == IWorkbenchAdapter.class && adaptableObject instanceof TreeParent) {
      return this.treeParentAdapter;
    }
    if (adapterType == IWorkbenchAdapter.class && adaptableObject instanceof TreeObject) {
      return this.treeObjectAdapter;
    }
    if (adapterType == IWorkbenchAdapter.class && adaptableObject instanceof TreeChainObject) {
      return this.treeChainObjectAdapter;
    }
    return null;
  }

  @Override
  public final Class<?>[] getAdapterList() {
    return new Class[] {IWorkbenchAdapter.class};
  }
}
