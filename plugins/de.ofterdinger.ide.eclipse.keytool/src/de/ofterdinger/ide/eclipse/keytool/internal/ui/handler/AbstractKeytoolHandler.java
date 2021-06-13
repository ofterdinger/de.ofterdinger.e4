package de.ofterdinger.ide.eclipse.keytool.internal.ui.handler;

import static de.ofterdinger.ide.eclipse.keytool.internal.KeytoolPlugin.PLUGIN_ID;
import static org.eclipse.core.runtime.IStatus.ERROR;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import de.ofterdinger.ide.eclipse.keytool.internal.KeytoolPlugin;
import de.ofterdinger.ide.eclipse.keytool.internal.ui.util.TextConstants;

abstract class AbstractKeytoolHandler extends AbstractHandler {

  @Override
  public final Object execute(ExecutionEvent event) throws ExecutionException {
    try {
      IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
      window.getActivePage().showView(TextConstants.KEYSTORE_VIEW_ID);
      execute(window);
    } catch (PartInitException e) {
      KeytoolPlugin.getDefault().getLog().log(new Status(ERROR, PLUGIN_ID, e.getMessage(), e));
    }
    return null;
  }

  protected abstract void execute(IWorkbenchWindow var1) throws ExecutionException;
}
