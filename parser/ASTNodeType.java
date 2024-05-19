package parser;

// The code defines an enumeration called `ASTNodeType` which represents different types of nodes in an
// Abstract Syntax Tree (AST). Each enum constant represents a specific type of node and has a
// corresponding string representation used for printing the AST.
public enum ASTNodeType {
    // General
    IDENTIFIER("<ID:%s>"),
    STRING("<STR:'%s'>"),
    INTEGER("<INT:%s>"),

    // Expressions
    LET("let"),
    LAMBDA("lambda"),
    WHERE("where"),

    // Tuple expressions
    TAU("tau"),
    AUG("aug"),
    CONDITIONAL("->"),

    // Boolean Expressions
    OR("or"),
    AND("&"),
    NOT("not"),
    GR("gr"),
    GE("ge"),
    LS("ls"),
    LE("le"),
    EQ("eq"),
    NE("ne"),

    // Arithmetic Expressions
    PLUS("+"),
    MINUS("-"),
    NEG("neg"),
    MULT("*"),
    DIV("/"),
    EXP("**"),
    AT("@"),

    // Rators and Rands
    GAMMA("gamma"),
    TRUE("<true>"),
    FALSE("<false>"),
    NIL("<nil>"),
    DUMMY("<dummy>"),

    // Definitions
    WITHIN("within"),
    SIMULTDEF("and"),
    REC("rec"),
    EQUAL("="),
    FCNFORM("function_form"),

    // Variables
    PAREN("<()>"),
    COMMA(","),

    // Post-standardize
    YSTAR("<Y*>"),

    BETA(""),
    DELTA(""),
    ETA(""),
    TUPLE("");

    private final String printName;

    private ASTNodeType(String printName) {
        this.printName = printName;
    }

    public String getPrintName() {
        return printName;
    }
}
