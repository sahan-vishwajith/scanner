package parser;

import java.util.ArrayDeque;
import java.util.Stack;

import csem.Beta;
import csem.Delta;

/**
 * The AST class represents an Abstract Syntax Tree and provides methods for
 * standardizing the tree and
 * creating delta objects.
 */
public class AST {
    private ASTNode root;
    private ArrayDeque<PendingDeltaBody> pendingDeltaBodyQueue;
    private boolean standardized;
    private Delta currentDelta;
    private Delta rootDelta;
    private int deltaIndex;

    public AST(ASTNode node) {
        this.root = node;
    }

    public void standardize() {
        standardize(root);
        standardized = true;
    }

    /**
     * The function standardizes an Abstract Syntax Tree (AST) by applying a series
     * of transformations to
     * specific node types.
     * 
     * @param node The parameter `node` is an instance of the `ASTNode` class, which
     *             represents a node in
     *             an abstract syntax tree (AST). The `standardize` method is a
     *             recursive method that traverses the AST
     *             and performs certain transformations on the nodes based on their
     *             type.
     */
    private void standardize(ASTNode node) {
        if (node.getChild() != null) {
            ASTNode childNode = node.getChild();
            while (childNode != null) {
                standardize(childNode);
                childNode = childNode.getSibling();
            }
        }

        if (node.getType() == ASTNodeType.LET) {
            ASTNode equalNode = node.getChild();
            if (equalNode.getType() != ASTNodeType.EQUAL) {
                try {
                    throw new Exception();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            ASTNode e = equalNode.getChild().getSibling();
            equalNode.getChild().setSibling(equalNode.getSibling());
            equalNode.setSibling(e);
            equalNode.setType(ASTNodeType.LAMBDA);
            node.setType(ASTNodeType.GAMMA);
        } else if (node.getType() == ASTNodeType.WHERE) {
            ASTNode equalNode = node.getChild().getSibling();
            node.getChild().setSibling(null);
            equalNode.setSibling(node.getChild());
            node.setChild(equalNode);
            node.setType(ASTNodeType.LET);
            standardize(node);
        } else if (node.getType() == ASTNodeType.FCNFORM) {
            ASTNode childSibling = node.getChild().getSibling();
            node.getChild().setSibling(constructLambdaChain(childSibling));
            node.setType(ASTNodeType.EQUAL);
        } else if (node.getType() == ASTNodeType.AT) {
            ASTNode e1 = node.getChild();
            ASTNode n = e1.getSibling();
            ASTNode e2 = n.getSibling();
            ASTNode gammaNode = new ASTNode();
            gammaNode.setType(ASTNodeType.GAMMA);
            gammaNode.setChild(n);
            n.setSibling(e1);
            e1.setSibling(null);
            gammaNode.setSibling(e2);
            node.setChild(gammaNode);
            node.setType(ASTNodeType.GAMMA);
        } else if (node.getType() == ASTNodeType.WITHIN) {
            if (node.getChild().getType() != ASTNodeType.EQUAL
                    || node.getChild().getSibling().getType() != ASTNodeType.EQUAL) {
                try {
                    throw new Exception();
                } catch (Exception e3) {
                    e3.printStackTrace();
                }
            }
            ASTNode x1 = node.getChild().getChild();
            ASTNode e1 = x1.getSibling();
            ASTNode x2 = node.getChild().getSibling().getChild();
            ASTNode e2 = x2.getSibling();
            ASTNode lambdaNode = new ASTNode();
            lambdaNode.setType(ASTNodeType.LAMBDA);
            x1.setSibling(e2);
            lambdaNode.setChild(x1);
            lambdaNode.setSibling(e1);
            ASTNode gammaNode = new ASTNode();
            gammaNode.setType(ASTNodeType.GAMMA);
            gammaNode.setChild(lambdaNode);
            x2.setSibling(gammaNode);
            node.setChild(x2);
            node.setType(ASTNodeType.EQUAL);
        } else if (node.getType() == ASTNodeType.SIMULTDEF) {
            ASTNode commaNode = new ASTNode();
            commaNode.setType(ASTNodeType.COMMA);
            ASTNode tauNode = new ASTNode();
            tauNode.setType(ASTNodeType.TAU);
            ASTNode childNode = node.getChild();
            while (childNode != null) {
                try {
                    populateCommaAndTauNode(childNode, commaNode, tauNode);
                } catch (Exception e3) {
                    e3.printStackTrace();
                }
                childNode = childNode.getSibling();
            }
            commaNode.setSibling(tauNode);
            node.setChild(commaNode);
            node.setType(ASTNodeType.EQUAL);
        } else if (node.getType() == ASTNodeType.REC) {
            ASTNode childNode = node.getChild();
            if (childNode.getType() != ASTNodeType.EQUAL) {
                try {
                    throw new Exception();
                } catch (Exception e3) {
                    e3.printStackTrace();
                }
            }
            ASTNode x = childNode.getChild();
            ASTNode lambdaNode = new ASTNode();
            lambdaNode.setType(ASTNodeType.LAMBDA);
            lambdaNode.setChild(x);
            ASTNode yStarNode = new ASTNode();
            yStarNode.setType(ASTNodeType.YSTAR);
            yStarNode.setSibling(lambdaNode);
            ASTNode gammaNode = new ASTNode();
            gammaNode.setType(ASTNodeType.GAMMA);
            gammaNode.setChild(yStarNode);
            ASTNode xWithSiblingGamma = new ASTNode();
            xWithSiblingGamma.setChild(x.getChild());
            xWithSiblingGamma.setSibling(gammaNode);
            xWithSiblingGamma.setType(x.getType());
            xWithSiblingGamma.setValue(x.getValue());
            node.setChild(xWithSiblingGamma);
            node.setType(ASTNodeType.EQUAL);
        } else if (node.getType() == ASTNodeType.LAMBDA) {
            ASTNode childSibling = node.getChild().getSibling();
            node.getChild().setSibling(constructLambdaChain(childSibling));
        }
    }

    /**
     * The function `populateCommaAndTauNode` takes an equalNode and assigns its
     * child to commaNode and its
     * sibling to tauNode.
     * 
     * @param equalNode The equalNode parameter is an ASTNode representing an equal
     *                  sign (=) in an abstract
     *                  syntax tree.
     * @param commaNode The `commaNode` parameter is an ASTNode object that
     *                  represents a comma in an
     *                  abstract syntax tree.
     * @param tauNode   The `tauNode` parameter is an ASTNode object that represents
     *                  a node in an Abstract
     *                  Syntax Tree (AST).
     */
    private void populateCommaAndTauNode(ASTNode equalNode, ASTNode commaNode, ASTNode tauNode) throws Exception {
        if (equalNode.getType() != ASTNodeType.EQUAL) {
            throw new Exception();
        }

        ASTNode x = equalNode.getChild();
        ASTNode e = x.getSibling();

        commaNode.setChild(x);
        tauNode.setChild(e);
    }

    /**
     * The function constructs a chain of lambda nodes from a given ASTNode.
     * 
     * @param node The `node` parameter is an ASTNode object representing a node in
     *             an abstract syntax
     *             tree.
     * @return The method is returning the last node in the lambda chain.
     */
    private ASTNode constructLambdaChain(ASTNode node) {
        if (node.getSibling() == null) {
            return node;
        }

        ASTNode lambdaNode = new ASTNode();
        lambdaNode.setType(ASTNodeType.LAMBDA);
        lambdaNode.setChild(node);

        ASTNode nextNode = node.getSibling();
        while (nextNode.getSibling() != null) {
            ASTNode tempNode = new ASTNode();
            tempNode.setType(ASTNodeType.LAMBDA);
            tempNode.setChild(nextNode);
            lambdaNode.setSibling(tempNode);
            lambdaNode = tempNode;
            nextNode = nextNode.getSibling();
        }
        lambdaNode.setSibling(nextNode);

        return lambdaNode;
    }

    /**
     * The function creates and returns a delta object by processing a queue of
     * pending delta bodies.
     * 
     * @return The method `createDeltas()` returns a `Delta` object.
     */
    public Delta createDeltas() {
        pendingDeltaBodyQueue = new ArrayDeque<PendingDeltaBody>();
        deltaIndex = 0;
        currentDelta = createDelta(root);
        processPendingDeltaStack();
        return rootDelta;
    }

    /**
     * The function creates a new Delta object and adds it to a queue, setting the
     * start node, body stack,
     * and index values.
     * 
     * @param startBodyNode The startBodyNode parameter is an ASTNode object that
     *                      represents the starting
     *                      node of the body for which a Delta object is being
     *                      created.
     * @return The method is returning an object of type Delta.
     */
    private Delta createDelta(ASTNode startBodyNode) {
        PendingDeltaBody pendingDelta = new PendingDeltaBody();
        pendingDelta.startNode = startBodyNode;
        pendingDelta.body = new Stack<ASTNode>();
        pendingDeltaBodyQueue.add(pendingDelta);

        Delta d = new Delta();
        d.setBody(pendingDelta.body);
        d.setIndex(deltaIndex++);
        currentDelta = d;

        if (startBodyNode == root)
            rootDelta = currentDelta;

        return d;
    }

    /**
     * The function processes a queue of pending delta bodies by popping each body
     * and its start node, and
     * then calling a method to build the delta body.
     */
    private void processPendingDeltaStack() {
        while (!pendingDeltaBodyQueue.isEmpty()) {
            PendingDeltaBody pendingDeltaBody = pendingDeltaBodyQueue.pop();
            Stack<ASTNode> body = pendingDeltaBody.body;
            ASTNode startNode = pendingDeltaBody.startNode;

            buildDeltaBody(startNode, body);
        }
    }

    /**
     * The function `buildDeltaBody` recursively builds a body of nodes for a given
     * ASTNode, based on its
     * type.
     * 
     * @param node The `node` parameter is an instance of the `ASTNode` class, which
     *             represents a node in
     *             an abstract syntax tree. It is used to traverse the tree and
     *             perform operations on the nodes.
     * @param body The `body` parameter is a `Stack` of `ASTNode` objects. It is
     *             used to store the nodes
     *             that make up the body of a delta or beta expression.
     */
    private void buildDeltaBody(ASTNode node, Stack<ASTNode> body) {
        switch (node.getType()) {
            case LAMBDA:
                Delta d = createDelta(node.getChild().getSibling());
                if (node.getChild().getType() == ASTNodeType.COMMA) {
                    ASTNode commaNode = node.getChild();
                    ASTNode childNode = commaNode.getChild();
                    while (childNode != null) {
                        d.addBoundVars(childNode.getValue());
                        childNode = childNode.getSibling();
                    }
                } else {
                    d.addBoundVars(node.getChild().getValue());
                }
                body.push(d);
                return;
            case CONDITIONAL:
                ASTNode conditionNode = node.getChild();
                ASTNode thenNode = conditionNode.getSibling();
                ASTNode elseNode = thenNode.getSibling();

                Beta betaNode = new Beta();

                buildDeltaBody(thenNode, betaNode.getThenBody());
                buildDeltaBody(elseNode, betaNode.getElseBody());

                body.push(betaNode);

                buildDeltaBody(conditionNode, body);
                return;
            default:
                body.push(node);
                ASTNode childNode = node.getChild();
                while (childNode != null) {
                    buildDeltaBody(childNode, body);
                    childNode = childNode.getSibling();
                }
                return;
        }
    }

    private class PendingDeltaBody {
        Stack<ASTNode> body;
        ASTNode startNode;
    }

    public boolean isStandardized() {
        return standardized;
    }
}
