package de.ofterdinger.e4.keytool.internal.ui.handler;

import de.ofterdinger.e4.keytool.internal.ui.KeyStoreView;
import org.eclipse.ui.IWorkbenchWindow;

public class OpenKeystoreHandler extends AbstractKeytoolHandler {

  @Override
  public void execute(IWorkbenchWindow window) {
    KeyStoreView.getInstance().openKeystore();
  }
}
