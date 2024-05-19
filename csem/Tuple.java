package csem;

import parser.ASTNode;
import parser.ASTNodeType;

/**
 * The Tuple class represents a tuple in an abstract syntax tree (AST) and
 * provides methods for getting
 * the value of the tuple and accepting a node copier.
 */
public class Tuple extends ASTNode {

    public Tuple() {
        setType(ASTNodeType.TUPLE);
    }

    @Override
    public String getValue() {
        ASTNode childNode = getChild();
        if (childNode == null)
            return "nil";

        StringBuilder printValue = new StringBuilder("(");
        while (childNode.getSibling() != null) {
            printValue.append(childNode.getValue()).append(", ");
            childNode = childNode.getSibling();
        }
        printValue.append(childNode.getValue()).append(")");
        return printValue.toString();
    }

    public Tuple accept(NodeCopier nodeCopier) {
        return nodeCopier.copy(this);
    }
}