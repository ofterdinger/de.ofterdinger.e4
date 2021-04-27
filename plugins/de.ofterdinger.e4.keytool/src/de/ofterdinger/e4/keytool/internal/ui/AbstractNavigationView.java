package de.ofterdinger.e4.keytool.internal.ui;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import de.ofterdinger.e4.keytool.internal.ui.util.TreeObject;
import de.ofterdinger.e4.keytool.internal.ui.util.TreeParent;

abstract class AbstractNavigationView extends ViewPart {
	private TreeViewer viewer;

	public final void createTreeViewer(Composite parent) {
		this.viewer = new TreeViewer(parent, 2818);
		this.viewer.setContentProvider(new ViewContentProvider());
		this.viewer.setLabelProvider(new ViewLabelProvider());
		this.viewer.setInput(createModel());
	}

	public TreeViewer getViewer() {
		return this.viewer;
	}

	@Override
	public void setFocus() {
		this.viewer.getControl().setFocus();
	}

	protected abstract Object createModel();

	public class ViewContentProvider implements ITreeContentProvider {
		@Override
		public void dispose() {
			// nothing to do yet
		}

		@Override
		public Object[] getChildren(Object parent) {
			if (parent instanceof TreeParent) {
				return ((TreeParent) parent).getChildren();
			}
			return new Object[0];
		}

		@Override
		public Object[] getElements(Object parent) {
			return getChildren(parent);
		}

		@Override
		public final Object getParent(Object child) {
			if (child instanceof TreeObject) {
				return ((TreeObject) child).getParent();
			}
			return null;
		}

		@Override
		public boolean hasChildren(Object parent) {
			if (parent instanceof TreeParent) {
				return ((TreeParent) parent).hasChildren();
			}
			return false;
		}

		@Override
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
			if (v == null) {
				throw new IllegalStateException("Viewer is null!"); //$NON-NLS-1$
			}
		}
	}

	public class ViewLabelProvider extends LabelProvider {
		@Override
		public final Image getImage(Object obj) {
			String imageKey = "IMG_OBJ_ELEMENTS"; //$NON-NLS-1$
			if (obj instanceof TreeParent) {
				imageKey = "IMG_OBJ_FOLDER"; //$NON-NLS-1$
			}
			return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
		}

		@Override
		public final String getText(Object obj) {
			return obj.toString();
		}
	}
}
