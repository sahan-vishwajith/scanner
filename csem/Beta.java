package csem;

import java.util.Stack;

import parser.ASTNode;
import parser.ASTNodeType;

/**
 * The Beta class represents a node in an abstract syntax tree (AST) and
 * contains stacks for the "then"
 * and "else" bodies.
 */
public class Beta extends ASTNode {
    private Stack<ASTNode> thenBody;
    private Stack<ASTNode> elseBody;

    public Beta() {
        setType(ASTNodeType.BETA);
        thenBody = new Stack<>();
        elseBody = new Stack<>();
    }

    public Stack<ASTNode> getThenBody() {
        return thenBody;
    }

    public Stack<ASTNode> getElseBody() {
        return elseBody;
    }

    public void setThenBody(Stack<ASTNode> thenBody) {
        this.thenBody = thenBody;
    }

    public void setElseBody(Stack<ASTNode> elseBody) {
        this.elseBody = elseBody;
    }

    public Beta accept(NodeCopier nodeCopier) {
        return nodeCopier.copy(this);
    }

}