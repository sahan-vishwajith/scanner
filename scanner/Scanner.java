package scanner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The class "Scanner" is a Java class that allows for user input to be read
 * from a file.
 */
public class Scanner {
  private BufferedReader buffer; // The buffer for reading characters from the input file
  private String nextChar; // The next character to be processed
  private int currentLineNum; // The current line number in the input file

  public Scanner(String inputFile) throws IOException {
    currentLineNum = 1;
    buffer = new BufferedReader(new InputStreamReader(new FileInputStream(new File(inputFile))));
  }

  /**
   * The function reads the next character and builds a token from it, returning
   * the token.
   * 
   * @return The method is returning the next token as a Token object.
   */
  public Token getNextToken() {
    Token nextToken = null;
    String currChar;
    if (nextChar != null) {
      currChar = nextChar;
      nextChar = null;
    } else
      currChar = readNextChar();
    if (currChar != null)
      nextToken = buildToken(currChar);
    return nextToken;
  }

  /**
   * The function reads the next character from a buffer and returns it as a
   * string, while also
   * incrementing a current line number if the character is a newline.
   * 
   * @return The method is returning a String value, which is the next character
   *         read from the buffer.
   */
  private String readNextChar() {
    String currChar = null;
    try {
      int c = buffer.read();
      if (c != -1) {
        currChar = Character.toString((char) c);
        if (currChar.equals("\n"))
          currentLineNum++;
      } else
        buffer.close();
    } catch (IOException e) {
    }
    return currChar;
  }

  /**
   * The function `buildToken` takes a character as input and returns a
   * corresponding token based on the
   * type of the character.
   * 
   * @param currChar The current character being processed.
   * @return The method `buildToken` returns the `nextToken` object.
   */
  private Token buildToken(String currChar) {
    Token nextToken = null;
    if (isLetter(currChar)) {
      nextToken = buildIdentifierToken(currChar);
    } else if (isDigit(currChar)) {
      nextToken = buildIntegerToken(currChar);
    } else if (isOperator(currChar)) {
      nextToken = buildOperatorToken(currChar);
    } else if (currChar.equals("\'")) {
      nextToken = buildStringToken(currChar);
    } else if (isSpace(currChar)) {
      nextToken = buildSpaceToken(currChar);
    } else if (isPunctuation(currChar)) {
      nextToken = buildPunctuationPattern(currChar);
    }
    return nextToken;
  }

  private boolean isLetter(String str) {
    return matchPattern(str, LexicalRegexPatterns.LetterPattern);
  }

  private boolean isDigit(String str) {
    return matchPattern(str, LexicalRegexPatterns.DigitPattern);
  }

  private boolean isOperator(String str) {
    return matchPattern(str, LexicalRegexPatterns.OpSymbolPattern);
  }

  private boolean isSpace(String str) {
    return matchPattern(str, LexicalRegexPatterns.SpacePattern);
  }

  private boolean isPunctuation(String str) {
    return matchPattern(str, LexicalRegexPatterns.PunctuationPattern);
  }

  private boolean matchPattern(String str, Pattern pattern) {
    Matcher matcher = pattern.matcher(str);
    return matcher.matches();
  }

  /**
   * The function builds an identifier token by reading characters and determining
   * if they form a valid
   * identifier or a reserved keyword.
   * 
   * @param currChar The parameter `currChar` is a string representing the
   *                 current character being
   *                 processed in the code.
   * @return The method is returning a Token object.
   */
  private Token buildIdentifierToken(String currChar) {
    Token identToken = new Token();
    identToken.setType(TokenType.IDENT);
    identToken.setLineNum(currentLineNum);
    StringBuilder sb = new StringBuilder(currChar);
    List<String> keywords = Arrays.asList(new String[] {
        "let", "in", "within", "fn", "where", "aug", "or",
        "not", "gr", "ge", "ls", "le", "eq", "ne", "true",
        "false", "nil", "dummy", "rec", "and" });

    String nextChar = readNextChar();
    while (nextChar != null) {
      if (isLetter(nextChar)) {
        sb.append(nextChar);
        nextChar = readNextChar();
      } else {
        this.nextChar = nextChar;
        break;
      }
    }

    String value = sb.toString();

    if (keywords.contains(value))
      identToken.setType(TokenType.RESERVED);

    identToken.setValue(value);
    return identToken;
  }

  /**
   * The function builds an integer token by reading consecutive digits from the
   * input string.
   * 
   * @param currChar The parameter "currChar" is a String that represents
   *                 the current character
   *                 being processed.
   * @return The method is returning an instance of the Token class, specifically
   *         an integer token.
   */
  private Token buildIntegerToken(String currChar) {
    Token intToken = new Token();
    intToken.setType(TokenType.NUM);
    intToken.setLineNum(currentLineNum);
    StringBuilder sb = new StringBuilder(currChar);

    String nextChar = readNextChar();
    while (nextChar != null) {
      if (isDigit(nextChar)) {
        sb.append(nextChar);
        nextChar = readNextChar();
      } else {
        this.nextChar = nextChar;
        break;
      }
    }

    intToken.setValue(sb.toString());
    return intToken;
  }

  /**
   * The function builds an operator token by reading characters and appending
   * them to a StringBuilder
   * until a non-operator character is encountered.
   * 
   * @param currChar The current character being processed.
   * @return The method is returning a Token object with the type set to
   *         TokenType.OP, the source
   *         line number set to the current source line number, and the value set
   *         to the concatenated operator
   *         symbols.
   */
  private Token buildOperatorToken(String currChar) {
    Token opToken = new Token();
    opToken.setType(TokenType.OP);
    opToken.setLineNum(currentLineNum);
    StringBuilder sb = new StringBuilder(currChar);
    String nextChar = readNextChar();

    if (currChar.equals("/") && nextChar.equals("/"))
      return buildCommentToken(currChar + nextChar);

    while (nextChar != null) {
      if (isOperator(nextChar)) {
        sb.append(nextChar);
        nextChar = readNextChar();
      } else {
        this.nextChar = nextChar;
        break;
      }
    }

    opToken.setValue(sb.toString());
    return opToken;
  }

  /**
   * The function builds a string token by reading characters between single
   * quotes
   * until a closing single quote is encountered.
   * 
   * @param currentChar The parameter `currentChar` is a string representing the
   *                    current character being
   *                    processed in the code.
   * 
   */
  private Token buildStringToken(String currentChar) {
    Token stringToken = new Token();
    stringToken.setType(TokenType.STR);
    stringToken.setLineNum(currentLineNum);
    StringBuilder sb = new StringBuilder("");

    String nextChar = readNextChar();
    while (nextChar != null) {
      if (nextChar.equals("\'")) {
        stringToken.setValue(sb.toString());
        return stringToken;
      } else if (isLetter(nextChar) || isDigit(nextChar) || isOperator(nextChar)) {
        sb.append(nextChar);
        nextChar = readNextChar();
      }
    }

    return null;
  }

  /**
   * The function builds a space token by reading consecutive space characters and
   * returning a token with
   * the value of the concatenated spaces.
   * 
   * @param currentChar The parameter "currentChar" is a String that represents
   *                    the current character
   *                    being processed in the code.
   * @return The method is returning a Token object.
   */
  private Token buildSpaceToken(String currentChar) {
    Token spaceToken = new Token();
    spaceToken.setType(TokenType.DEL);
    spaceToken.setLineNum(currentLineNum);
    StringBuilder sb = new StringBuilder(currentChar);

    String nextChar = readNextChar();
    while (nextChar != null) {
      if (isSpace(nextChar)) {
        sb.append(nextChar);
        nextChar = readNextChar();
      } else {
        this.nextChar = nextChar;
        break;
      }
    }

    spaceToken.setValue(sb.toString());
    return spaceToken;
  }

  /**
   * The function builds a comment token by reading characters until it encounters
   * a newline character or
   * an operator.
   * 
   * @param currentChar The parameter `currentChar` is a String that represents
   *                    the current character
   *                    being processed in the code.
   * @return The method is returning a Token object.
   */
  private Token buildCommentToken(String currentChar) {
    Token commentToken = new Token();
    commentToken.setType(TokenType.DEL);
    commentToken.setLineNum(currentLineNum);
    StringBuilder sb = new StringBuilder(currentChar);

    String nextChar = readNextChar();
    while (nextChar != null) {
      if (nextChar.equals("\n")) {
        commentToken.setValue(sb.toString());
        return commentToken;
      } else if (isOperator(nextChar)) {
        sb.append(nextChar);
        nextChar = readNextChar();
      } else {
        this.nextChar = nextChar;
        break;
      }
    }

    commentToken.setValue(sb.toString());
    return commentToken;
  }

  /**
   * The `buildPunctuationPattern` method is responsible for building a
   * punctuation token based on the
   * current character being processed.
   * 
   * @param currentChar The parameter `currentChar` is a String that represents
   *                    the current character
   *                    being processed in the code.
   * @return The method is returning a Token object.
   */
  private Token buildPunctuationPattern(String currentChar) {
    Token punctuationToken = new Token();
    punctuationToken.setLineNum(currentLineNum);
    punctuationToken.setValue(currentChar);

    switch (currentChar) {
      case "(":
        punctuationToken.setType(TokenType.L_PAREN);
        break;
      case ")":
        punctuationToken.setType(TokenType.R_PAREN);
        break;
      case ";":
        punctuationToken.setType(TokenType.SEMICOLON);
        break;
      case ",":
        punctuationToken.setType(TokenType.COMMA);
        break;
      // default:
      // punctuationToken.setType(TokenType.PUNCTUATION);
      // break;
    }
    return punctuationToken;
  }
}