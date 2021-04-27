package de.ofterdinger.e4.keytool.internal.ui.util;

import de.ofterdinger.e4.keytool.internal.KeytoolPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public final class ImageKeys {
  public static final String CERTIFICATE = "icons/cert.gif"; // $NON-NLS-1$
  public static final String DELETE_CERTIFICATE = "icons/deletecert.gif"; // $NON-NLS-1$
  public static final String EXPORT_CERTIFICATE = "icons/certexport.gif"; // $NON-NLS-1$
  public static final String IMPORT_CERTIFICATE = "icons/certimport.gif"; // $NON-NLS-1$
  public static final String ICON_KEY = "icons/key.gif"; // $NON-NLS-1$
  public static final String KEY_CLOSE = "icons/key_close.gif"; // $NON-NLS-1$
  public static final String LOCK_CLOSED = "icons/lock_closed.gif"; // $NON-NLS-1$
  public static final String LOCK_OPEN = "icons/lock_open.gif"; // $NON-NLS-1$
  public static final String NEW_CERTIFICATE = "icons/certnew.gif"; // $NON-NLS-1$
  private static ImageRegistry registry = null;

  private ImageKeys() {}

  public static Image getImage(String imageKey) {
    if (registry == null) {
      registry = ImageKeys.initializeRegistry();
    }
    return registry.get(imageKey);
  }

  public static ImageDescriptor getImageDescriptor(String imageKey) {
    return AbstractUIPlugin.imageDescriptorFromPlugin(KeytoolPlugin.PLUGIN_ID, imageKey);
  }

  private static ImageRegistry initializeRegistry() {
    ImageRegistry newRegistry = new ImageRegistry(Display.getCurrent());
    newRegistry.put(CERTIFICATE, ImageKeys.getImageDescriptor(CERTIFICATE));
    newRegistry.put(EXPORT_CERTIFICATE, ImageKeys.getImageDescriptor(EXPORT_CERTIFICATE));
    newRegistry.put(IMPORT_CERTIFICATE, ImageKeys.getImageDescriptor(IMPORT_CERTIFICATE));
    newRegistry.put(NEW_CERTIFICATE, ImageKeys.getImageDescriptor(NEW_CERTIFICATE));
    newRegistry.put(ICON_KEY, ImageKeys.getImageDescriptor(ICON_KEY));
    newRegistry.put(KEY_CLOSE, ImageKeys.getImageDescriptor(KEY_CLOSE));
    newRegistry.put(LOCK_CLOSED, ImageKeys.getImageDescriptor(LOCK_CLOSED));
    newRegistry.put(LOCK_OPEN, ImageKeys.getImageDescriptor(LOCK_OPEN));
    return newRegistry;
  }
}
