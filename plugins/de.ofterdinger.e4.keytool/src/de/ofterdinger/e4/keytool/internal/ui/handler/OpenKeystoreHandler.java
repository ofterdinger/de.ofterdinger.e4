package de.ofterdinger.e4.keytool.internal.ui.handler;

import org.eclipse.ui.IWorkbenchWindow;

import de.ofterdinger.e4.keytool.internal.ui.KeyStoreView;

public class OpenKeystoreHandler extends AbstractKeytoolHandler {
	@Override
	public void execute(IWorkbenchWindow window) {
		KeyStoreView.getInstance().openKeystore();
	}
}
