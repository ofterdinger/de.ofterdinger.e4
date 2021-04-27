package de.ofterdinger.e4.googlejavaformat;

import java.io.IOException;
import org.junit.Test;

public class TestsPdeActivator {
  private static final String ERROR =
      "A test message for PDE tests and supposed to be shown in log.";

  @Test
  public void testTrace() {
    try {
      throw new IOException(ERROR);
    } catch (IOException e) {
      Activator.logError(e);
    }
  }
}
