package csem;

import parser.ASTNode;
import parser.ASTNodeType;

/**
 * Represents the fixed-point resulting from the application (Y F). We never
 * actually evaluate the fixed-point. The hope is that the program will (in the
 * recursion's base case) choose the option that doesn't have the fixed point
 * (and
 * hence will not lead to our evaluating the fixed point again (what happens
 * when
 * we replace YF with F (YF) i.e., Eta with Delta Eta)). If the source code
 * creates
 * an infinite recursion, none of these tricks will save us.
 */
public class Eta extends ASTNode {
    private Delta delta;

    public Eta() {
        setType(ASTNodeType.ETA);
    }

    public Eta accept(NodeCopier nodeCopier) {
        return nodeCopier.copy(this);
    }

    @Override
    public String getValue() {
        return "[eta closure: " + delta.getBoundVars().get(0) + ": " + delta.getIndex() + "]";
    }

    public Delta getDelta() {
        return delta;
    }

    public void setDelta(Delta delta) {
        this.delta = delta;
    }
}
