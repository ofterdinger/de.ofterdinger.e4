package de.ofterdinger.e4.keytool.internal.ui.preference;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;

import de.ofterdinger.e4.keytool.internal.KeytoolPlugin;
import de.ofterdinger.e4.keytool.internal.ui.util.ImageKeys;

public class OpenPreferencesAction extends Action {
	private final Shell shell;
	private final IWorkbench workbench;

	public OpenPreferencesAction(IWorkbenchWindow window) {
		this.shell = window.getShell();
		this.workbench = window.getWorkbench();
	}

	@Override
	public final void run() {
		super.run();
		OpenPreferencesAction.openPreferencesPage(this.workbench, this.shell);
	}

	public static void openPreferencesPage(IWorkbench workbench, Shell shell) {
		GeneralPreferencePage page = new GeneralPreferencePage();
		ImageDescriptor imageDescriptor = ImageKeys.getImageDescriptor(ImageKeys.ICON_KEY);
		page.setImageDescriptor(imageDescriptor);
		page.setTitle("Keytool"); //$NON-NLS-1$
		page.setDescription("Keytool preferences"); //$NON-NLS-1$
		page.init(workbench);
		PreferenceManager mgr = new PreferenceManager();
		PreferenceNode node = new PreferenceNode(KeytoolPlugin.PLUGIN_ID, page);
		mgr.addToRoot(node);
		PreferenceDialog dialog = new PreferenceDialog(shell, mgr);
		dialog.create();
		dialog.setMessage(page.getTitle());
		dialog.open();
	}
}
