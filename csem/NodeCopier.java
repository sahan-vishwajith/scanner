package csem;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import parser.ASTNode;

public class NodeCopier {
    public ASTNode copy(ASTNode astNode) {
        ASTNode copy = new ASTNode();

        if (astNode.getChild() != null)
            copy.setChild(copy(astNode.getChild()));

        if (astNode.getSibling() != null)
            copy.setSibling(copy(astNode.getSibling()));

        copy.setType(astNode.getType());
        copy.setValue(astNode.getValue());
        copy.setSourceLineNumber(astNode.getSourceLineNumber());

        return copy;
    }

    public Beta copy(Beta beta) {
        Beta copy = new Beta();

        if (beta.getChild() != null)
            copy.setChild(copy(beta.getChild()));

        if (beta.getSibling() != null)
            copy.setSibling(copy(beta.getSibling()));

        copy.setType(beta.getType());
        copy.setValue(beta.getValue());
        copy.setSourceLineNumber(beta.getSourceLineNumber());

        Stack<ASTNode> thenBodyCopy = new Stack<>();
        for (ASTNode thenBodyElement : beta.getThenBody()) {
            thenBodyCopy.add(thenBodyElement.accept(this));
        }
        copy.setThenBody(thenBodyCopy);

        Stack<ASTNode> elseBodyCopy = new Stack<>();
        for (ASTNode elseBodyElement : beta.getElseBody()) {
            elseBodyCopy.add(elseBodyElement.accept(this));
        }
        copy.setElseBody(elseBodyCopy);

        return copy;
    }

    public Delta copy(Delta delta) {
        Delta copy = new Delta();

        if (delta.getChild() != null)
            copy.setChild(copy(delta.getChild()));

        if (delta.getSibling() != null)
            copy.setSibling(copy(delta.getSibling()));

        copy.setType(delta.getType());
        copy.setValue(delta.getValue());
        copy.setIndex(delta.getIndex());
        copy.setSourceLineNumber(delta.getSourceLineNumber());

        Stack<ASTNode> bodyCopy = new Stack<ASTNode>();
        for (ASTNode bodyElement : delta.getBody()) {
            bodyCopy.add(bodyElement.accept(this));
        }
        copy.setBody(bodyCopy);

        List<String> boundVarsCopy = new ArrayList<>(delta.getBoundVars());
        copy.setBoundVars(boundVarsCopy);

        copy.setLinkedEnv(delta.getLinkedEnv());

        return copy;
    }

    public Eta copy(Eta eta) {
        Eta copy = new Eta();

        if (eta.getChild() != null)
            copy.setChild(copy(eta.getChild()));

        if (eta.getSibling() != null)
            copy.setSibling(copy(eta.getSibling()));

        copy.setType(eta.getType());
        copy.setValue(eta.getValue());
        copy.setSourceLineNumber(eta.getSourceLineNumber());

        copy.setDelta(eta.getDelta().accept(this));

        return copy;
    }

    public Tuple copy(Tuple tuple) {
        Tuple copy = new Tuple();

        if (tuple.getChild() != null)
            copy.setChild(copy(tuple.getChild()));

        if (tuple.getSibling() != null)
            copy.setSibling(copy(tuple.getSibling()));

        copy.setType(tuple.getType());
        copy.setValue(tuple.getValue());
        copy.setSourceLineNumber(tuple.getSourceLineNumber());

        return copy;
    }
}
