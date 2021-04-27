package de.ofterdinger.e4.googlejavaformat.testsuite;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.ofterdinger.e4.googlejavaformat.TestsPdeActivator;
import de.ofterdinger.e4.googlejavaformat.TestsPdeGoogleJavaFormatter;

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
