package de.ofterdinger.ide.e4.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/** Test suite for all PDE tests. */
@RunWith(Suite.class)
@SuiteClasses({ //
		de.ofterdinger.ide.e4.googlejavaformat.testsuite.AllPdeTests.class //
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
