package de.ofterdinger.ide.eclipse.keytool.internal.ui.handler;

import org.eclipse.ui.IWorkbenchWindow;

import de.ofterdinger.ide.eclipse.keytool.internal.ui.KeyStoreView;

public class OpenKeystoreHandler extends AbstractKeytoolHandler {

  @Override
  public void execute(IWorkbenchWindow window) {
    KeyStoreView.getInstance().openKeystore();
  }
}
