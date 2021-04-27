package de.ofterdinger.e4.keytool.internal.ui;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class KeystoreViewLabelProvider extends LabelProvider {
  private static final int INITIAL_NO_OF_IMAGES = 7;

  private Map<ImageDescriptor, Image> imageTable = new HashMap<>(INITIAL_NO_OF_IMAGES);

  @Override
  public final void dispose() {
    if (this.imageTable != null) {
      Iterator<Image> i = this.imageTable.values().iterator();
      while (i.hasNext()) {
        i.next().dispose();
      }
      this.imageTable = null;
    }
  }

  @Override
  public final Image getImage(Object element) {
    IWorkbenchAdapter adapter = getAdapter(element);
    if (adapter == null) {
      return null;
    }
    ImageDescriptor descriptor = adapter.getImageDescriptor(element);
    if (descriptor == null) {
      return null;
    }
    return this.imageTable.computeIfAbsent(descriptor, key -> descriptor.createImage());
  }

  @Override
  public final String getText(Object element) {
    IWorkbenchAdapter adapter = getAdapter(element);
    if (adapter == null) {
      return EMPTY;
    }
    return adapter.getLabel(element);
  }

  private static IWorkbenchAdapter getAdapter(Object element) {
    IWorkbenchAdapter adapter = null;
    if (element instanceof IAdaptable) {
      adapter = ((IAdaptable) element).getAdapter(IWorkbenchAdapter.class);
    }
    if (adapter == null) {
      adapter =
          (IWorkbenchAdapter)
              Platform.getAdapterManager().loadAdapter(element, IWorkbenchAdapter.class.getName());
    }
    return adapter;
  }
}
