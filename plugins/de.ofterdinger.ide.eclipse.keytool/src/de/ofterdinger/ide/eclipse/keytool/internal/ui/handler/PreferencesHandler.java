package de.ofterdinger.ide.eclipse.keytool.internal.ui.handler;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;

import de.ofterdinger.ide.eclipse.keytool.internal.ui.preference.OpenPreferencesAction;

public class PreferencesHandler extends AbstractKeytoolHandler {

  @Override
  public void execute(IWorkbenchWindow window) throws ExecutionException {
    OpenPreferencesAction.openPreferencesPage(window.getWorkbench(), window.getShell());
  }
}
