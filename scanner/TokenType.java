package scanner;

public enum TokenType {
  IDENT, // Identifier token
  NUM, // Integer token
  STR, // String token
  OP, // Operator token
  DEL, // Delete (whitespace) token
  L_PAREN, // Left parenthesis token
  R_PAREN, // Right parenthesis token
  SEMICOLON, // Semicolon token
  COMMA, // Comma token
  RESERVED // Reserved keyword token
}
