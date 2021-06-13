package de.ofterdinger.ide.eclipse.googlejavaformat;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/** Activates this bundle. */
public class Activator implements BundleActivator {

  private static ILog logger;

  @Override
  public void start(BundleContext bundleContext) {
    setContext(bundleContext);
  }

  @Override
  public void stop(BundleContext bundleContext) {
    setContext(null);
  }

  /**
   * Writes the given error to the log.
   *
   * @param error the error to log
   */
  public static synchronized void logError(Throwable error) {
    if (error != null && logger != null) {
      logger.error(error.getMessage(), error);
    }
  }

  private static synchronized void setContext(BundleContext bundleContext) {
    if (bundleContext != null) {
      logger = Platform.getLog(bundleContext.getBundle());
    } else {
      logger = null;
    }
  }
}
