package scanner;

import java.util.regex.Pattern;

public class LexicalRegexPatterns {
  private static final String letterRegexString = "a-zA-Z";
  private static final String digitRegexString = "\\d";
  private static final String spaceRegexString = "[\\s\\t\\n]";
  private static final String punctuationRegexString = "();,";
  private static final String opSymbolRegexString = "+-/~:=|!#%_{}\"*<>.&$^\\[\\]?@";
  private static final String opSymbolToEscapeString = "([*<>.&$^?])";

  public static final Pattern LetterPattern = Pattern.compile("[a-zA-Z]");

  public static final Pattern IdentifierPattern = Pattern.compile("[a-zA-Z\\d_]");

  public static final Pattern DigitPattern = Pattern.compile("\\d");

  public static final Pattern PunctuationPattern = Pattern.compile("[();,]");

  public static final String opSymbolRegex = "[" + escapeMetaChars("+-/~:=|!#%_{}\"*<>.&$^\\[\\]?@", "([*<>.&$^?])")
      + "]";
  public static final Pattern OpSymbolPattern = Pattern.compile(opSymbolRegex);

  public static final Pattern StringPattern = Pattern.compile("[ \\t\\n\\\\" + punctuationRegexString
      + letterRegexString + digitRegexString + escapeMetaChars(opSymbolRegexString, opSymbolToEscapeString) + "]");

  public static final Pattern SpacePattern = Pattern.compile(spaceRegexString);

  public static final Pattern CommentPattern = Pattern.compile("[ \\t\\'\\\\ \\r" + punctuationRegexString
      + letterRegexString + digitRegexString + escapeMetaChars(opSymbolRegexString, opSymbolToEscapeString) + "]"); // the
                                                                                                                    // \\r
                                                                                                                    // is
                                                                                                                    // for
                                                                                                                    // Windows
                                                                                                                    // LF;
                                                                                                                    // not
                                                                                                                    // really
                                                                                                                    // required
                                                                                                                    // since
                                                                                                                    // we're
                                                                                                                    // targeting
                                                                                                                    // *nix
                                                                                                                    // systems

  private static String escapeMetaChars(String inputString, String charsToEscape) {
    return inputString.replaceAll(charsToEscape, "\\\\\\\\$1");
  }
}