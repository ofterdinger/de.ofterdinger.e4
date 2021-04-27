package de.ofterdinger.e4.googlejavaformat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.text.edits.TextEdit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestsPdeGoogleJavaFormatter {

  private static GoogleJavaFormatter formatter;

  private static int kind = ASTParser.K_COMPILATION_UNIT | CodeFormatter.F_INCLUDE_COMMENTS;

  @BeforeClass
  public static void setUpBeforeClass() {
    formatter = new GoogleJavaFormatter();
  }

  @AfterClass
  public static void tearDownAfterClass() {
    formatter = null;
  }

  @Test
  public void testFormatAlreadyFormatted() throws IOException {
    try (InputStream input = this.getClass().getResourceAsStream("sourceCode.txt")) {
      String srcCode = IOUtils.toString(input, StandardCharsets.UTF_8);
      TextEdit formatted = formatter.format(kind, srcCode, 0, srcCode.length(), 2, "\n");
      assertNull(formatted);
    }
  }

  @Test
  public void testFormatDoFormatting() throws IOException {
    try (InputStream input = this.getClass().getResourceAsStream("sourceCode2.txt")) {
      String srcCode = IOUtils.toString(input, StandardCharsets.UTF_8);
      TextEdit formatted = formatter.format(kind, srcCode, 0, srcCode.length(), 2, "\n");
      assertNotNull(formatted);
    }
  }

  @Test
  public void testCreateIndentation() {
    try {
      formatter.createIndentationString(-1);
      fail();
    } catch (IllegalArgumentException expected) {
      // expected
    }
    assertEquals("", formatter.createIndentationString(0));
    assertEquals("  ", formatter.createIndentationString(1));
    assertEquals("    ", formatter.createIndentationString(2));
  }
}
