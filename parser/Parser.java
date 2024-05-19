package parser;

import java.util.Stack;

import scanner.Scanner;
import scanner.Token;
import scanner.TokenType;

public class Parser {
  private Scanner s;

  private Token currentToken;

  Stack<ASTNode> stack;

  // The below code is defining a constructor for a class called Parser. The
  // constructor takes a
  // parameter of type Scanner and assigns it to a variable called s. It also
  // initializes a new Stack
  // object called stack.
  public Parser(Scanner s) {
    this.s = s;
    stack = new Stack<ASTNode>();
  }

  /**
   * The function builds an Abstract Syntax Tree (AST) by parsing a given input
   * and returning the root
   * node of the AST.
   * 
   * @return The method is returning an instance of the AST (Abstract Syntax Tree)
   *         class.
   */
  public AST buildAST() {
    startParse();
    return new AST(stack.pop());
  }

  /**
   * The startParse function reads non-terminals, processes the E production, and
   * throws an exception
   * if there is an unexpected token.
   */
  public void startParse() {
    readNonTerminal();
    readE();
    if (currentToken != null)
      try {
        throw new Exception();
      } catch (Exception e) {
        e.printStackTrace();
      }
  }

  /**
   * The function reads tokens until it encounters a non-DELETE token, and then
   * creates a terminal AST
   * node based on the type of the current token.
   */
  private void readNonTerminal() {
    do {
      currentToken = s.getNextToken();
    } while (isCurrentTokenType(TokenType.DEL));
    if (null != currentToken) {
      if (currentToken.getType() == TokenType.IDENT) {
        createTerminalASTNode(ASTNodeType.IDENTIFIER, currentToken.getValue());
      } else if (currentToken.getType() == TokenType.NUM) {
        createTerminalASTNode(ASTNodeType.INTEGER, currentToken.getValue());
      } else if (currentToken.getType() == TokenType.STR) {
        createTerminalASTNode(ASTNodeType.STRING, currentToken.getValue());
      }
    }
  }

  /**
   * The function checks if the current token's type and value match the given
   * parameters.
   * 
   * @param type  The type parameter is of type TokenType, which is an enumeration
   *              representing different
   *              types of tokens. It is used to check if the type of the current
   *              token matches the specified type.
   * @param value The `value` parameter is a `String` that represents the expected
   *              value of the current
   *              token.
   * @return The method is returning a boolean value.
   */
  private boolean isCurrentToken(TokenType type, String value) {
    return currentToken != null &&
        currentToken.getType() == type &&
        currentToken.getValue().equals(value);
  }

  /**
   * The function checks if the current token's type matches the given type.
   * 
   * @param type The "type" parameter is of type TokenType.
   * @return The method is returning a boolean value.
   */
  private boolean isCurrentTokenType(TokenType type) {
    return currentToken != null && currentToken.getType() == type;
  }

  /**
   * The function builds an N-ary AST (Abstract Syntax Tree) node by popping the
   * specified number of
   * child nodes from a stack and setting them as children of the new node.
   * 
   * @param type      The type of the AST node being built. It specifies the
   *                  category or purpose of the node,
   *                  such as "expression" or "statement".
   * @param ary_value The `ary_value` parameter represents the number of children
   *                  that the current node
   *                  should have in the N-ary AST (Abstract Syntax Tree).
   */
  private void buildNAryASTNode(ASTNodeType type, int ary_value) {
    ASTNode node = new ASTNode();
    node.setType(type);
    while (ary_value > 0) {
      ASTNode child = stack.pop();
      if (node.getChild() != null)
        child.setSibling(node.getChild());
      node.setChild(child);
      node.setSourceLineNumber(child.getSourceLineNumber());
      ary_value--;
    }
    stack.push(node);
  }

  /**
   * The function creates a new ASTNode with a given type, value, and source line
   * number, and pushes it
   * onto a stack.
   * 
   * @param terminal_type  The `terminal_type` parameter is the type of the AST
   *                       node being created. It
   *                       specifies the category or classification of the node,
   *                       such as "identifier", "number", "OP",
   *                       etc.
   * @param terminal_value The `terminal_value` parameter is a string that
   *                       represents the value of the
   *                       terminal node in the abstract syntax tree (AST). It
   *                       could be any value that is associated with the
   *                       terminal node, such as a variable name, a literal
   *                       value, or an OP symbol.
   */
  private void createTerminalASTNode(ASTNodeType terminal_type, String terminal_value) {
    ASTNode node = new ASTNode();
    node.setType(terminal_type);
    node.setValue(terminal_value);
    node.setSourceLineNumber(currentToken.getLineNum());
    stack.push(node);
  }

  /* EXPRESSIONS */

  // NON TERMINAL -> E

  private void readE() {
    if (isCurrentToken(TokenType.RESERVED, "let")) { // E -> 'let' D 'in' E => 'let'
      readNonTerminal();
      readD();
      if (!isCurrentToken(TokenType.RESERVED, "in"))
        try {
          throw new Exception();
        } catch (Exception e) {
          e.printStackTrace();
        }
      readNonTerminal();
      readE();
      buildNAryASTNode(ASTNodeType.LET, 2);
    } else if (isCurrentToken(TokenType.RESERVED, "fn")) { // E -> 'fn' Vb+ '.' E => 'lambda'
      int treesToPop = 0;

      readNonTerminal();
      while (isCurrentTokenType(TokenType.IDENT) || isCurrentTokenType(TokenType.L_PAREN)) {
        readVB();
        treesToPop++;
      }

      if (treesToPop == 0)
        try {
          throw new Exception();
        } catch (Exception e) {
          e.printStackTrace();
        }

      if (!isCurrentToken(TokenType.OP, "."))
        try {
          throw new Exception();
        } catch (Exception e) {
          e.printStackTrace();
        }

      readNonTerminal();
      readE();

      buildNAryASTNode(ASTNodeType.LAMBDA, treesToPop + 1);
    } else // E -> Ew
      readEW();
  }

  // NON TERMINAL -> EW

  private void readEW() {
    readT(); // Ew -> T
    if (isCurrentToken(TokenType.RESERVED, "where")) { // Ew -> T 'where' Dr => 'where'
      readNonTerminal();
      readDR();
      buildNAryASTNode(ASTNodeType.WHERE, 2);
    }
  }

  /* Tuple Expressions */

  // NON TERMINAL -> T

  private void readT() {
    readTA(); // T -> Ta
    int treesToPop = 0;
    while (isCurrentToken(TokenType.OP, ",")) { // T -> Ta (',' Ta )+ => 'tau'
      readNonTerminal();
      readTA();
      treesToPop++;
    }
    if (treesToPop > 0)
      buildNAryASTNode(ASTNodeType.TAU, treesToPop + 1);
  }

  // NON TERMINAL -> TA

  private void readTA() {
    readTC(); // Ta -> Tc
    while (isCurrentToken(TokenType.RESERVED, "aug")) { // Ta -> Ta 'aug' Tc => 'aug'
      readNonTerminal();
      readTC();
      buildNAryASTNode(ASTNodeType.AUG, 2);
    }
  }

  // NON TERMINAL -> TC

  private void readTC() {
    readB(); // Tc -> B

    if (isCurrentToken(TokenType.OP, "->")) { // Tc -> B '->' Tc '|' Tc => '->'
      readNonTerminal();
      readTC();
      if (!isCurrentToken(TokenType.OP, "|"))
        try {
          throw new Exception();
        } catch (Exception e) {
          e.printStackTrace();
        }
      readNonTerminal();
      readTC();
      buildNAryASTNode(ASTNodeType.CONDITIONAL, 3);
    }
  }

  /* Boolean Expressions */

  // NON TERMINAL -> B

  private void readB() {
    readBT(); // B -> Bt

    while (isCurrentToken(TokenType.RESERVED, "or")) { // B -> B 'or' Bt => 'or'
      readNonTerminal();
      readBT();
      buildNAryASTNode(ASTNodeType.OR, 2);
    }
  }

  // NON TERMINAL -> BT

  private void readBT() {
    readBS(); // Bt -> Bs;

    while (isCurrentToken(TokenType.OP, "&")) { // Bt -> Bt '&' Bs => '&'
      readNonTerminal();
      readBS();
      buildNAryASTNode(ASTNodeType.AND, 2);
    }
  }

  // NON TERMINAL -> BS

  private void readBS() {
    if (isCurrentToken(TokenType.RESERVED, "not")) { // Bs -> 'not' Bp => 'not'
      readNonTerminal();
      readBP();
      buildNAryASTNode(ASTNodeType.NOT, 1);
    } else
      readBP(); // Bs -> Bp

  }

  // NON TERMINAL -> BP

  private void readBP() {
    readA(); // Bp -> A
    if (isCurrentToken(TokenType.RESERVED, "gr") || isCurrentToken(TokenType.OP, ">")) { // Bp -> A('gr' | '>' ) A
                                                                                         // => 'gr'
      readNonTerminal();
      readA();
      buildNAryASTNode(ASTNodeType.GR, 2);
    } else if (isCurrentToken(TokenType.RESERVED, "ge") || isCurrentToken(TokenType.OP, ">=")) { // Bp -> A ('ge'
                                                                                                 // | '>=') A =>
                                                                                                 // 'ge'
      readNonTerminal();
      readA();
      buildNAryASTNode(ASTNodeType.GE, 2);
    } else if (isCurrentToken(TokenType.RESERVED, "ls") || isCurrentToken(TokenType.OP, "<")) { // Bp -> A ('ls' |
                                                                                                // '<' ) A => 'ls'
      readNonTerminal();
      readA();
      buildNAryASTNode(ASTNodeType.LS, 2);
    } else if (isCurrentToken(TokenType.RESERVED, "le") || isCurrentToken(TokenType.OP, "<=")) { // Bp -> A ('le'
                                                                                                 // | '<=') A =>
                                                                                                 // 'le'
      readNonTerminal();
      readA();
      buildNAryASTNode(ASTNodeType.LE, 2);
    } else if (isCurrentToken(TokenType.RESERVED, "eq")) { // Bp -> A 'eq' A => 'eq'
      readNonTerminal();
      readA();
      buildNAryASTNode(ASTNodeType.EQ, 2);
    } else if (isCurrentToken(TokenType.RESERVED, "ne")) { // Bp -> A 'ne' A => 'ne'
      readNonTerminal();
      readA();
      buildNAryASTNode(ASTNodeType.NE, 2);
    }
  }

  /* Arithmetic Expressions */

  // NON TERMINAL -> A

  private void readA() {
    if (isCurrentToken(TokenType.OP, "+")) { // A -> '+' At
      readNonTerminal();
      readAT();
    } else if (isCurrentToken(TokenType.OP, "-")) { // A -> '-' At => 'neg'
      readNonTerminal();
      readAT();
      buildNAryASTNode(ASTNodeType.NEG, 1);
    } else
      readAT();

    boolean plus = true;
    while (isCurrentToken(TokenType.OP, "+") || isCurrentToken(TokenType.OP, "-")) {
      if (currentToken.getValue().equals("+"))
        plus = true;
      else if (currentToken.getValue().equals("-"))
        plus = false;
      readNonTerminal();
      readAT();
      if (plus) // A -> A '+' At => '+'
        buildNAryASTNode(ASTNodeType.PLUS, 2);
      else // A -> A '-' At => '-'
        buildNAryASTNode(ASTNodeType.MINUS, 2);
    }
  }

  // NON TERMINAL -> AT

  private void readAT() {
    readAF(); // At -> Af;

    boolean mult = true;
    while (isCurrentToken(TokenType.OP, "*") || isCurrentToken(TokenType.OP, "/")) {
      if (currentToken.getValue().equals("*"))
        mult = true;
      else if (currentToken.getValue().equals("/"))
        mult = false;
      readNonTerminal();
      readAF();
      if (mult) // At -> At '*' Af => '*'
        buildNAryASTNode(ASTNodeType.MULT, 2);
      else // At -> At '/' Af => '/'
        buildNAryASTNode(ASTNodeType.DIV, 2);
    }
  }

  // NON TERMINAL -> AF

  private void readAF() {
    try {
      readAP();
    } catch (Exception e) {
      e.printStackTrace();
    } // Af -> Ap;
    if (isCurrentToken(TokenType.OP, "**")) { // Af -> Ap '**' Af => '**'
      readNonTerminal();
      readAF();
      buildNAryASTNode(ASTNodeType.EXP, 2);
    }
  }

  // NON TERMINAL -> AP

  private void readAP() throws Exception {
    readR(); // Ap -> R;
    while (isCurrentToken(TokenType.OP, "@")) { // Ap -> Ap '@' '<IDENTIFIER>' R => '@'
      readNonTerminal();
      if (!isCurrentTokenType(TokenType.IDENT))
        throw new Exception();
      readNonTerminal();
      readR();
      buildNAryASTNode(ASTNodeType.AT, 3);
    }
  }

  /* Rators and Rands */

  // NON TERMINAL -> R

  private void readR() {
    readRN();
    readNonTerminal();
    while (isCurrentTokenType(TokenType.NUM) ||
        isCurrentTokenType(TokenType.STR) ||
        isCurrentTokenType(TokenType.IDENT) ||
        isCurrentToken(TokenType.RESERVED, "true") ||
        isCurrentToken(TokenType.RESERVED, "false") ||
        isCurrentToken(TokenType.RESERVED, "nil") ||
        isCurrentToken(TokenType.RESERVED, "dummy") ||
        isCurrentTokenType(TokenType.L_PAREN)) { // R -> R Rn => 'gamma'
      readRN();
      buildNAryASTNode(ASTNodeType.GAMMA, 2);
      readNonTerminal();
    }
  }

  // NON TERMINAL -> RN

  private void readRN() {
    if (isCurrentTokenType(TokenType.IDENT) || // R -> '<IDENTIFIER>'
        isCurrentTokenType(TokenType.NUM) || // R -> '<INTEGER>'
        isCurrentTokenType(TokenType.STR)) { // R-> '<STRING>'
    } else if (isCurrentToken(TokenType.RESERVED, "true")) { // R -> 'true' => 'true'
      createTerminalASTNode(ASTNodeType.TRUE, "true");
    } else if (isCurrentToken(TokenType.RESERVED, "false")) { // R -> 'false' => 'false'
      createTerminalASTNode(ASTNodeType.FALSE, "false");
    } else if (isCurrentToken(TokenType.RESERVED, "nil")) { // R -> 'nil' => 'nil'
      createTerminalASTNode(ASTNodeType.NIL, "nil");
    } else if (isCurrentTokenType(TokenType.L_PAREN)) {
      readNonTerminal();
      readE(); // extra readNonTerminal in readE()
      if (!isCurrentTokenType(TokenType.R_PAREN))
        try {
          throw new Exception();
        } catch (Exception e) {
          e.printStackTrace();
        }
    } else if (isCurrentToken(TokenType.RESERVED, "dummy")) { // R -> 'dummy' => 'dummy'
      createTerminalASTNode(ASTNodeType.DUMMY, "dummy");
    }
  }

  /* Definitions */

  // NON TERMINAL -> D

  private void readD() {
    readDA(); // D -> Da
    if (isCurrentToken(TokenType.RESERVED, "within")) { // D -> Da 'within' D => 'within'
      readNonTerminal();
      readD();
      buildNAryASTNode(ASTNodeType.WITHIN, 2);
    }
  }

  // NON TERMINAL -> DA

  private void readDA() {
    readDR(); // Da -> Dr
    int treesToPop = 0;
    while (isCurrentToken(TokenType.RESERVED, "and")) { // Da -> Dr ( 'and' Dr )+ => 'and'
      readNonTerminal();
      readDR();
      treesToPop++;
    }
    if (treesToPop > 0)
      buildNAryASTNode(ASTNodeType.SIMULTDEF, treesToPop + 1);
  }

  // NON TERMINAL -> DR

  private void readDR() {
    if (isCurrentToken(TokenType.RESERVED, "rec")) { // Dr -> 'rec' Db => 'rec'
      readNonTerminal();
      readDB();
      buildNAryASTNode(ASTNodeType.REC, 1);
    } else { // Dr -> Db
      readDB();
    }
  }

  // NON TERMINAL -> DB

  private void readDB() {
    if (isCurrentTokenType(TokenType.L_PAREN)) { // Db -> '(' D ')'
      readD();
      readNonTerminal();
      if (!isCurrentTokenType(TokenType.R_PAREN))
        try {
          throw new Exception();
        } catch (Exception e) {
          e.printStackTrace();
        }
      readNonTerminal();
    } else if (isCurrentTokenType(TokenType.IDENT)) {
      readNonTerminal();
      if (isCurrentToken(TokenType.OP, ",")) { // Db -> Vl '=' E => '='
        readNonTerminal();
        readVL();
        if (!isCurrentToken(TokenType.OP, "="))
          try {
            throw new Exception();
          } catch (Exception e) {
            e.printStackTrace();
          }
        buildNAryASTNode(ASTNodeType.COMMA, 2);
        readNonTerminal();
        readE();
        buildNAryASTNode(ASTNodeType.EQUAL, 2);
      } else { // Db -> '<IDENTIFIER>' Vb+ '=' E => 'fcn_form'
        if (isCurrentToken(TokenType.OP, "=")) { // Db -> Vl '=' E => '='; if Vl had only one IDENTIFIER (no
                                                 // commas)
          readNonTerminal();
          readE();
          buildNAryASTNode(ASTNodeType.EQUAL, 2);
        } else { // Db -> '<IDENTIFIER>' Vb+ '=' E => 'fcn_form'
          int treesToPop = 0;

          while (isCurrentTokenType(TokenType.IDENT) || isCurrentTokenType(TokenType.L_PAREN)) {
            readVB();
            treesToPop++;
          }

          if (treesToPop == 0)
            try {
              throw new Exception();
            } catch (Exception e) {
              e.printStackTrace();
            }

          if (!isCurrentToken(TokenType.OP, "="))
            try {
              throw new Exception();
            } catch (Exception e) {
              e.printStackTrace();
            }

          readNonTerminal();
          readE();

          buildNAryASTNode(ASTNodeType.FCNFORM, treesToPop + 2);
        }
      }
    }
  }

  /* Variables */

  // NON TERMINAL -> VB

  private void readVB() {
    if (isCurrentTokenType(TokenType.IDENT)) { // Vb -> '<IDENTIFIER>'
      readNonTerminal();
    } else if (isCurrentTokenType(TokenType.L_PAREN)) {
      readNonTerminal();
      if (isCurrentTokenType(TokenType.R_PAREN)) { // Vb -> '(' ')' => '()'
        createTerminalASTNode(ASTNodeType.PAREN, "");
        readNonTerminal();
      } else { // Vb -> '(' Vl ')'
        readVL();
        if (!isCurrentTokenType(TokenType.R_PAREN))
          try {
            throw new Exception();
          } catch (Exception e) {
            e.printStackTrace();
          }
        readNonTerminal();
      }
    }
  }

  // NON TERMINAL -> VL

  private void readVL() {
    if (!isCurrentTokenType(TokenType.IDENT))
      try {
        throw new Exception();
      } catch (Exception e) {
        e.printStackTrace();
      }
    else {
      readNonTerminal();
      int treesToPop = 0;
      while (isCurrentToken(TokenType.OP, ",")) { // Vl -> '<IDENTIFIER>' list ',' => ','?;
        readNonTerminal();
        if (!isCurrentTokenType(TokenType.IDENT))
          try {
            throw new Exception();
          } catch (Exception e) {
            e.printStackTrace();
          }
        readNonTerminal();
        treesToPop++;
      }
      if (treesToPop > 0)
        buildNAryASTNode(ASTNodeType.COMMA, treesToPop + 1); // +1 for the first identifier
    }
  }

}
