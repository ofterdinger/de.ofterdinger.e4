package de.ofterdinger.ide.e4.googlejavaformat;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import com.google.googlejavaformat.java.FormatterException;
import com.google.googlejavaformat.java.Replacement;
import com.google.googlejavaformat.java.SnippetFormatter;
import com.google.googlejavaformat.java.SnippetFormatter.SnippetKind;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

/** Runs the Google Java formatter on the given code. */
public class GoogleJavaFormatter extends CodeFormatter {

  private static final int INDENTATION_SIZE = 2;

  @Override
  public TextEdit format(
      int kind, String source, int offset, int length, int indentationLevel, String lineSeparator) {
    IRegion[] regions = new IRegion[] {new Region(offset, length)};
    return formatInternal(kind, source, regions, indentationLevel);
  }

  @Override
  public TextEdit format(
      int kind, String source, IRegion[] regions, int indentationLevel, String lineSeparator) {
    return formatInternal(kind, source, regions, indentationLevel);
  }

  @Override
  public String createIndentationString(int indentationLevel) {
    Preconditions.checkArgument(
        indentationLevel >= 0,
        "Indentation level cannot be less than zero. Given: %s",
        indentationLevel);
    int spaces = indentationLevel * INDENTATION_SIZE;
    StringBuilder buf = new StringBuilder(spaces);
    for (int i = 0; i < spaces; i++) {
      buf.append(' ');
    }
    return buf.toString();
  }

  /** Runs the Google Java formatter on the given source, with only the given ranges specified. */
  private static TextEdit formatInternal(
      int kind, String source, IRegion[] regions, int initialIndent) {
    try {
      boolean includeComments =
          (kind & CodeFormatter.F_INCLUDE_COMMENTS) == CodeFormatter.F_INCLUDE_COMMENTS;
      kind &= ~CodeFormatter.F_INCLUDE_COMMENTS;
      SnippetKind snippetKind;
      switch (kind) {
        case ASTParser.K_EXPRESSION:
          snippetKind = SnippetKind.EXPRESSION;
          break;
        case ASTParser.K_STATEMENTS:
          snippetKind = SnippetKind.STATEMENTS;
          break;
        case ASTParser.K_CLASS_BODY_DECLARATIONS:
          snippetKind = SnippetKind.CLASS_BODY_DECLARATIONS;
          break;
        case ASTParser.K_COMPILATION_UNIT:
          snippetKind = SnippetKind.COMPILATION_UNIT;
          break;
        default:
          throw new IllegalArgumentException(
              String.format("Unknown snippet kind: %d", Integer.valueOf(kind)));
      }
      List<Replacement> replacements =
          new SnippetFormatter()
              .format(
                  snippetKind, source, rangesFromRegions(regions), initialIndent, includeComments);
      if (idempotent(source, regions, replacements)) {
        // Do not create edits if there's no diff.
        return null;
      }
      // Convert replacements to text edits.
      return editFromReplacements(replacements);
    } catch (IllegalArgumentException | FormatterException exception) {
      Activator.logError(exception);
      // Do not format on errors.
      return null;
    }
  }

  private static List<Range<Integer>> rangesFromRegions(IRegion[] regions) {
    List<Range<Integer>> ranges = new ArrayList<>();
    for (IRegion region : regions) {
      ranges.add(
          Range.closedOpen(
              Integer.valueOf(region.getOffset()),
              Integer.valueOf(region.getOffset() + region.getLength())));
    }
    return ranges;
  }

  /** @return {@code true} if input and output texts are equal, else {@code false}. */
  private static boolean idempotent(
      String source, IRegion[] regions, List<Replacement> replacements) {
    // This implementation only checks for single replacement.
    if (replacements.size() == 1) {
      Replacement replacement = replacements.get(0);
      String output = replacement.getReplacementString();
      // Entire source case: input = output, nothing changed.
      if (output.equals(source)) {
        return true;
      }
      // Single region and single replacement case: if they are equal, nothing changed.
      if (regions.length == 1) {
        Range<Integer> range = replacement.getReplaceRange();
        String snippet =
            source.substring(range.lowerEndpoint().intValue(), range.upperEndpoint().intValue());
        if (output.equals(snippet)) {
          return true;
        }
      }
    }
    return false;
  }

  private static TextEdit editFromReplacements(List<Replacement> replacements) {
    // Split the replacements that cross line boundaries.
    TextEdit edit = new MultiTextEdit();
    for (Replacement replacement : replacements) {
      Range<Integer> replaceRange = replacement.getReplaceRange();
      edit.addChild(
          new ReplaceEdit(
              replaceRange.lowerEndpoint().intValue(),
              replaceRange.upperEndpoint().intValue() - replaceRange.lowerEndpoint().intValue(),
              replacement.getReplacementString()));
    }
    return edit;
  }
}
