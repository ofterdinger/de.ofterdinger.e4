package de.ofterdinger.e4.keytool.internal.ui.handler;

import de.ofterdinger.e4.keytool.internal.ui.preference.OpenPreferencesAction;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;

public class PreferencesHandler extends AbstractKeytoolHandler {

  @Override
  public void execute(IWorkbenchWindow window) throws ExecutionException {
    OpenPreferencesAction.openPreferencesPage(window.getWorkbench(), window.getShell());
  }
}
