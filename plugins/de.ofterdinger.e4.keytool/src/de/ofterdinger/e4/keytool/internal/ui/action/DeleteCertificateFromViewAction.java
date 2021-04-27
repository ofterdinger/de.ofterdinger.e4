package de.ofterdinger.e4.keytool.internal.ui.action;

import static de.ofterdinger.e4.keytool.internal.KeytoolPlugin.PLUGIN_ID;
import static org.eclipse.core.runtime.IStatus.ERROR;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;

import de.ofterdinger.e4.keytool.internal.KeystoreFile;
import de.ofterdinger.e4.keytool.internal.KeytoolPlugin;
import de.ofterdinger.e4.keytool.internal.ui.KeyStoreView;
import de.ofterdinger.e4.keytool.internal.ui.util.ImageKeys;
import de.ofterdinger.e4.keytool.internal.ui.util.TreeObject;

public class DeleteCertificateFromViewAction extends AbstractKeytoolViewAction {
	private Shell shell;

	public DeleteCertificateFromViewAction(IWorkbenchWindow window) {
		super(window);
		this.shell = window.getShell();
	}

	public Shell getShell() {
		return this.shell;
	}

	@Override
	public void run() {
		TreeObject selectedTreeObject;
		if (KeytoolPlugin.openConfirm("Sure?", "Are you sure you want to delete the certificate?", this.shell) //$NON-NLS-1$ //$NON-NLS-2$
				&& (selectedTreeObject = getSelectedTreeObject()) != null && selectedTreeObject.hasParentKeystore()) {
			KeystoreFile keystoreFile = selectedTreeObject.getParent().getKeystoreFile();
			try (FileOutputStream out = new FileOutputStream(keystoreFile.getKeystorefilename())) {
				KeyStore keystore = keystoreFile.getKeystore();
				keystore.deleteEntry(selectedTreeObject.getName());
				keystore.store(out, keystoreFile.getPassword().toCharArray());
				KeyStoreView.fireFileChanged(keystoreFile.getKeystorefilename(), keystoreFile.getKeystoreType(),
						keystoreFile.getPassword());
			} catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
				KeytoolPlugin.getDefault().getLog().log(new Status(ERROR, PLUGIN_ID, e.getMessage(), e));
				KeytoolPlugin.showError("Error deleting and saving keystore: " + e.getMessage(), this.shell); //$NON-NLS-1$
			}
		}
	}

	@Override
	protected void actionFiredChild() {
		setEnabled(true);
	}

	@Override
	protected void actionFiredParent() {
		setEnabled(false);
	}

	@Override
	protected ImageDescriptor getActionImage() {
		return ImageKeys.getImageDescriptor(ImageKeys.DELETE_CERTIFICATE);
	}

	@Override
	protected String getActionText() {
		return "Delete certificate"; //$NON-NLS-1$
	}

	@Override
	protected String getActionToolTipText() {
		return "Delete the certificate"; //$NON-NLS-1$
	}
}
