package de.ofterdinger.ide.eclipse.googlejavaformat.testsuite;

import de.ofterdinger.ide.eclipse.googlejavaformat.TestsPdeActivator;
import de.ofterdinger.ide.eclipse.googlejavaformat.TestsPdeGoogleJavaFormatter;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/** Test suite for all PDE tests. */
@RunWith(Suite.class)
@SuiteClasses({ //
  TestsPdeActivator.class,
  TestsPdeGoogleJavaFormatter.class
})
public class AllPdeTests {

  @BeforeClass
  public static void setUpClass() {
    // nothing to prepare yet
  }

  @AfterClass
  public static void tearDownClass() {
    // nothing to cleanup yet
  }
}
