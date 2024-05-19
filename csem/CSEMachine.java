package csem;

import java.util.Stack;

import parser.AST;
import parser.ASTNode;
import parser.ASTNodeType;

/**
 * Represents the Call-by-Name Simple Evaluator Machine (CSEM) for evaluating
 * AST expressions.
 * The CSEM performs call-by-name evaluation, using a control stack and a value
 * stack.
 * The control stack contains AST nodes to be processed, and the value stack
 * stores intermediate results.
 * The CSEM evaluates AST expressions starting from the root delta node and the
 * root environment.
 */
public class CSEMachine {

  private Stack<ASTNode> valueStack;
  private Delta rootDelta;

  /**
   * Creates a CSEM instance with the given Abstract Syntax Tree (AST).
   * 
   * @param ast The AST to be evaluated by the CSEM.
   * @throws RuntimeException If the AST is not standardized, indicating an error
   *                          in the AST structure.
   */
  public CSEMachine(AST ast) {
    if (!ast.isStandardized()) {
      throw new RuntimeException("AST has NOT been standardized!"); // should never happen
    }
    rootDelta = ast.createDeltas();
    rootDelta.setLinkedEnv(new Environment()); // primitive environment
    valueStack = new Stack<ASTNode>();
  }

  /**
   * Evaluates the program starting from the root delta node.
   */
  public void evaluateProgram() {
    processControlStack(rootDelta, rootDelta.getLinkedEnv());
  }

  private void processControlStack(Delta currentDelta, Environment currentEnv) {
    // create a new control stack and add all of the delta's body to it so that the
    // delta's body isn't
    // modified whenever the control stack is popped in all the functions below
    Stack<ASTNode> controlStack = new Stack<ASTNode>();
    controlStack.addAll(currentDelta.getBody());

    while (!controlStack.isEmpty()) {
      processCurrentNode(currentDelta, currentEnv, controlStack);
    }
  }

  private void processCurrentNode(Delta currentDelta, Environment currentEnv, Stack<ASTNode> currentControlStack) {
    ASTNode node = currentControlStack.pop();
    if (applyBinaryOperation(node)) {
      return;
    } else if (applyUnaryOperation(node)) {
      return;
    } else {
      switch (node.getType()) {
        case IDENTIFIER:
          handleIdentifiers(node, currentEnv);
          break;
        case NIL:
        case TAU:
          createTuple(node);
          break;
        case BETA:
          handleBeta((Beta) node, currentControlStack);
          break;
        case GAMMA:
          applyGamma(currentDelta, node, currentEnv, currentControlStack);
          break;
        case DELTA:
          ((Delta) node).setLinkedEnv(currentEnv); // RULE 2
          valueStack.push(node);
          break;
        default:
          // Although we use ASTNodes, a CSEM will only ever see a subset of all possible
          // ASTNodeTypes.
          // These are the types that are NOT standardized away into lambdas and gammas.
          // E.g. types
          // such as LET, WHERE, WITHIN, SIMULTDEF etc will NEVER be encountered by the
          // CSEM
          valueStack.push(node);
          break;
      }
    }
  }

  // RULE 6
  private boolean applyBinaryOperation(ASTNode rator) {
    switch (rator.getType()) {
      case PLUS:
      case MINUS:
      case MULT:
      case DIV:
      case EXP:
      case LS:
      case LE:
      case GR:
      case GE:
        binaryArithmeticOp(rator.getType());
        return true;
      case EQ:
      case NE:
        try {
          binaryLogicalEqNeOp(rator.getType());
        } catch (Exception e) {
          e.printStackTrace();
        }
        return true;
      case OR:
      case AND:
        binaryLogicalOrAndOp(rator.getType());
        return true;
      case AUG:
        augTuples();
        return true;
      default:
        return false;
    }
  }

  /**
   * Performs the specified binary arithmetic operation on the two integer
   * operands
   * popped from the value stack. The result of the operation is then pushed back
   * onto the value stack.
   *
   * @param operationType The type of binary arithmetic operation to perform,
   *                      such as PLUS, MINUS, MULT, DIV, EXP, LS, LE, GR, or GE.
   */
  private void binaryArithmeticOp(ASTNodeType type) {
    ASTNode rand1 = valueStack.pop();
    ASTNode rand2 = valueStack.pop();
    if (rand1.getType() != ASTNodeType.INTEGER || rand2.getType() != ASTNodeType.INTEGER) {
      try {
        throw new Exception();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    ASTNode result = new ASTNode();
    result.setType(ASTNodeType.INTEGER);

    switch (type) {
      case PLUS:
        result.setValue(Integer.toString(Integer.parseInt(rand1.getValue()) + Integer.parseInt(rand2.getValue())));
        break;
      case MINUS:
        result.setValue(Integer.toString(Integer.parseInt(rand1.getValue()) - Integer.parseInt(rand2.getValue())));
        break;
      case MULT:
        result.setValue(Integer.toString(Integer.parseInt(rand1.getValue()) * Integer.parseInt(rand2.getValue())));
        break;
      case DIV:
        result.setValue(Integer.toString(Integer.parseInt(rand1.getValue()) / Integer.parseInt(rand2.getValue())));
        break;
      case EXP:
        result.setValue(
            Integer.toString((int) Math.pow(Integer.parseInt(rand1.getValue()), Integer.parseInt(rand2.getValue()))));
        break;
      case LS:
        if (Integer.parseInt(rand1.getValue()) < Integer.parseInt(rand2.getValue()))
          pushTrueNode();
        else
          pushFalseNode();
        return;
      case LE:
        if (Integer.parseInt(rand1.getValue()) <= Integer.parseInt(rand2.getValue()))
          pushTrueNode();
        else
          pushFalseNode();
        return;
      case GR:
        if (Integer.parseInt(rand1.getValue()) > Integer.parseInt(rand2.getValue()))
          pushTrueNode();
        else
          pushFalseNode();
        return;
      case GE:
        if (Integer.parseInt(rand1.getValue()) >= Integer.parseInt(rand2.getValue()))
          pushTrueNode();
        else
          pushFalseNode();
        return;
      default:
        break;
    }
    valueStack.push(result);
  }

  /**
   * Performs the binary logical equality (EQ) and inequality (NE) operations.
   * 
   * @param type The type of binary operation (EQ or NE).
   * @throws Exception
   */
  private void binaryLogicalEqNeOp(ASTNodeType type) throws Exception {
    ASTNode rand1 = valueStack.pop();
    ASTNode rand2 = valueStack.pop();

    if (rand1.getType() == ASTNodeType.TRUE || rand1.getType() == ASTNodeType.FALSE) {
      if (rand2.getType() != ASTNodeType.TRUE && rand2.getType() != ASTNodeType.FALSE) {
        try {
          throw new Exception();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      compareTruthValues(rand1, rand2, type);
      return;
    }

    if (rand1.getType() != rand2.getType()) {
      try {
        throw new Exception();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    if (rand1.getType() == ASTNodeType.STRING) {
      compareStrings(rand1, rand2, type);
    } else if (rand1.getType() == ASTNodeType.INTEGER) {
      compareIntegers(rand1, rand2, type);
    } else {
      throw new Exception();
    }
  }

  /**
   * Compares two truth values (TRUE or FALSE) based on the specified binary
   * operation (EQ or NE) and pushes the result (TRUE or FALSE) onto the value
   * stack.
   *
   * @param operand1      The first truth value (TRUE or FALSE).
   * @param operand2      The second truth value (TRUE or FALSE).
   * @param operationType The type of binary operation (EQ or NE).
   */
  private void compareTruthValues(ASTNode rand1, ASTNode rand2, ASTNodeType type) {
    if (rand1.getType() == rand2.getType()) {
      if (type == ASTNodeType.EQ) {
        pushTrueNode();
      } else {
        pushFalseNode();
      }
    } else {
      if (type == ASTNodeType.EQ) {
        pushFalseNode();
      } else {
        pushTrueNode();
      }
    }
  }

  /**
   * Compares the two string operands based on the specified binary operation (EQ
   * or NE)
   * and pushes the result (TRUE or FALSE) onto the value stack.
   *
   * @param operand1      The first string operand.
   * @param operand2      The second string operand.
   * @param operationType The type of binary operation (EQ or NE).
   */
  private void compareStrings(ASTNode rand1, ASTNode rand2, ASTNodeType type) {
    if (rand1.getValue().equals(rand2.getValue())) {
      if (type == ASTNodeType.EQ) {
        pushTrueNode();
      } else {
        pushFalseNode();
      }
    } else {
      if (type == ASTNodeType.EQ) {
        pushFalseNode();
      } else {
        pushTrueNode();
      }
    }
  }

  /**
   * Compares the two integer operands based on the specified binary operation (EQ
   * or NE)
   * and pushes the result (TRUE or FALSE) onto the value stack.
   *
   * @param operand1      The first integer operand.
   * @param operand2      The second integer operand.
   * @param operationType The type of binary operation (EQ or NE).
   */
  private void compareIntegers(ASTNode rand1, ASTNode rand2, ASTNodeType type) {
    if (Integer.parseInt(rand1.getValue()) == Integer.parseInt(rand2.getValue())) {
      if (type == ASTNodeType.EQ) {
        pushTrueNode();
      } else {
        pushFalseNode();
      }
    } else {
      if (type == ASTNodeType.EQ) {
        pushFalseNode();
      } else {
        pushTrueNode();
      }
    }
  }

  /**
   * Performs the binary logical OR and AND operations.
   * 
   * @param type The type of binary operation (OR or AND).
   */
  private void binaryLogicalOrAndOp(ASTNodeType type) {
    ASTNode rand1 = valueStack.pop();
    ASTNode rand2 = valueStack.pop();

    if ((rand1.getType() == ASTNodeType.TRUE || rand1.getType() == ASTNodeType.FALSE) &&
        (rand2.getType() == ASTNodeType.TRUE || rand2.getType() == ASTNodeType.FALSE)) {
      orAndTruthValues(rand1, rand2, type);
    } else {
      try {
        throw new Exception();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private void orAndTruthValues(ASTNode rand1, ASTNode rand2, ASTNodeType type) {
    if (type == ASTNodeType.OR) {
      if (rand1.getType() == ASTNodeType.TRUE || rand2.getType() == ASTNodeType.TRUE)
        pushTrueNode();
      else
        pushFalseNode();
    } else {
      if (rand1.getType() == ASTNodeType.TRUE && rand2.getType() == ASTNodeType.TRUE)
        pushTrueNode();
      else
        pushFalseNode();
    }
  }

  private void augTuples() {
    ASTNode rand1 = valueStack.pop();
    ASTNode rand2 = valueStack.pop();

    if (rand1.getType() != ASTNodeType.TUPLE)
      try {
        throw new Exception();
      } catch (Exception e) {
        e.printStackTrace();
      }

    ASTNode childNode = rand1.getChild();
    if (childNode == null)
      rand1.setChild(rand2);
    else {
      while (childNode.getSibling() != null)
        childNode = childNode.getSibling();
      childNode.setSibling(rand2);
    }
    rand2.setSibling(null);

    valueStack.push(rand1);
  }

  // RULE 7
  private boolean applyUnaryOperation(ASTNode rator) {
    switch (rator.getType()) {
      case NOT:
        not();
        return true;
      case NEG:
        neg();
        return true;
      default:
        return false;
    }
  }

  private void not() {
    ASTNode rand = valueStack.pop();
    if (rand.getType() != ASTNodeType.TRUE && rand.getType() != ASTNodeType.FALSE)
      try {
        throw new Exception();
      } catch (Exception e) {
        e.printStackTrace();
      }

    if (rand.getType() == ASTNodeType.TRUE)
      pushFalseNode();
    else
      pushTrueNode();
  }

  private void neg() {
    ASTNode rand = valueStack.pop();
    if (rand.getType() != ASTNodeType.INTEGER)
      try {
        throw new Exception();
      } catch (Exception e) {
        e.printStackTrace();
      }

    ASTNode result = new ASTNode();
    result.setType(ASTNodeType.INTEGER);
    result.setValue(Integer.toString(-1 * Integer.parseInt(rand.getValue())));
    valueStack.push(result);
  }

  // RULE 3
  private void applyGamma(Delta currentDelta, ASTNode node, Environment currentEnv,
      Stack<ASTNode> currentControlStack) {
    ASTNode rator = valueStack.pop();
    ASTNode rand = valueStack.pop();

    if (rator.getType() == ASTNodeType.DELTA) {
      Delta nextDelta = (Delta) rator;

      // RULE 2: Set the linked environment of the current Delta node
      // The Delta node has a link to the environment that was in effect
      // when it was pushed onto the value stack.
      // We construct a new environment here that will contain all the bindings
      // (single or multiple) required by this Delta node. This new environment
      // will link back to the environment carried by the Delta node.
      Environment newEnv = new Environment();
      newEnv.setParent(nextDelta.getLinkedEnv());

      // RULE 4
      if (nextDelta.getBoundVars().size() == 1) {
        newEnv.addMapping(nextDelta.getBoundVars().get(0), rand);
      }
      // RULE 11
      else {
        if (rand.getType() != ASTNodeType.TUPLE)
          try {
            throw new Exception();
          } catch (Exception e) {
            e.printStackTrace();
          }

        for (int i = 0; i < nextDelta.getBoundVars().size(); i++) {
          newEnv.addMapping(nextDelta.getBoundVars().get(i), getNthTupleChild((Tuple) rand, i + 1)); // + 1 coz tuple
                                                                                                     // indexing starts
                                                                                                     // at 1
        }
      }

      processControlStack(nextDelta, newEnv);
      return;
    } else if (rator.getType() == ASTNodeType.YSTAR) {
      // RULE 12
      if (rand.getType() != ASTNodeType.DELTA)
        try {
          throw new Exception();
        } catch (Exception e) {
          e.printStackTrace();
        }

      Eta etaNode = new Eta();
      etaNode.setDelta((Delta) rand);
      valueStack.push(etaNode);
      return;
    } else if (rator.getType() == ASTNodeType.ETA) {
      // RULE 13
      // push back the rand, the eta and then the delta it contains
      valueStack.push(rand);
      valueStack.push(rator);
      valueStack.push(((Eta) rator).getDelta());
      // push back two gammas (one for the eta and one for the delta)
      currentControlStack.push(node);
      currentControlStack.push(node);
      return;
    } else if (rator.getType() == ASTNodeType.TUPLE) {
      tupleSelection((Tuple) rator, rand);
      return;
    } else if (evaluateReservedIdentifiers(rator, rand, currentControlStack))
      return;
    else
      try {
        throw new Exception();
      } catch (Exception e) {
        e.printStackTrace();
      }
  }

  private boolean evaluateReservedIdentifiers(ASTNode rator, ASTNode rand, Stack<ASTNode> currentControlStack) {
    switch (rator.getValue()) {
      case "Isinteger":
        checkTypeAndPushTrueOrFalse(rand, ASTNodeType.INTEGER);
        return true;
      case "Isstring":
        checkTypeAndPushTrueOrFalse(rand, ASTNodeType.STRING);
        return true;
      case "Isdummy":
        checkTypeAndPushTrueOrFalse(rand, ASTNodeType.DUMMY);
        return true;
      case "Isfunction":
        checkTypeAndPushTrueOrFalse(rand, ASTNodeType.DELTA);
        return true;
      case "Istuple":
        checkTypeAndPushTrueOrFalse(rand, ASTNodeType.TUPLE);
        return true;
      case "Istruthvalue":
        if (rand.getType() == ASTNodeType.TRUE || rand.getType() == ASTNodeType.FALSE)
          pushTrueNode();
        else
          pushFalseNode();
        return true;
      case "Stem":
        stem(rand);
        return true;
      case "Stern":
        stern(rand);
        return true;
      case "Conc":
      case "conc": // typos
        conc(rand, currentControlStack);
        return true;
      case "Print":
      case "print": // typos
        printNodeValue(rand);
        pushDummyNode();
        return true;
      case "ItoS":
        itos(rand);
        return true;
      case "Order":
        order(rand);
        return true;
      case "Null":
        isNullTuple(rand);
        return true;
      default:
        return false;
    }
  }

  /**
   * Checks the type of the given node and pushes either True or False node based
   * on the comparison result.
   * 
   * @param rand The node to check the type of.
   * @param type The expected type for the comparison.
   */
  private void checkTypeAndPushTrueOrFalse(ASTNode rand, ASTNodeType type) {
    if (rand.getType() == type)
      pushTrueNode();
    else
      pushFalseNode();
  }

  /**
   * Pushes a True node onto the value stack.
   */
  private void pushTrueNode() {
    ASTNode trueNode = new ASTNode();
    trueNode.setType(ASTNodeType.TRUE);
    trueNode.setValue("true");
    valueStack.push(trueNode);
  }

  /**
   * Pushes a False node onto the value stack.
   */
  private void pushFalseNode() {
    ASTNode falseNode = new ASTNode();
    falseNode.setType(ASTNodeType.FALSE);
    falseNode.setValue("false");
    valueStack.push(falseNode);
  }

  /**
   * Pushes a Dummy node onto the value stack.
   */
  private void pushDummyNode() {
    ASTNode falseNode = new ASTNode();
    falseNode.setType(ASTNodeType.DUMMY);
    valueStack.push(falseNode);
  }

  /**
   * Extracts the first character from the string node and pushes it back to the
   * value stack.
   * 
   * @param rand The string node.
   */
  private void stem(ASTNode rand) {
    if (rand.getType() != ASTNodeType.STRING)
      try {
        throw new Exception();
      } catch (Exception e) {
        e.printStackTrace();
      }

    if (rand.getValue().isEmpty())
      rand.setValue("");
    else
      rand.setValue(rand.getValue().substring(0, 1));

    valueStack.push(rand);
  }

  /**
   * Removes the first character from the string node and pushes it back to the
   * value stack.
   * 
   * @param rand The string node.
   */
  private void stern(ASTNode rand) {
    if (rand.getType() != ASTNodeType.STRING)
      try {
        throw new Exception();
      } catch (Exception e) {
        e.printStackTrace();
      }

    if (rand.getValue().isEmpty() || rand.getValue().length() == 1)
      rand.setValue("");
    else
      rand.setValue(rand.getValue().substring(1));

    valueStack.push(rand);
  }

  /**
   * Concatenates two string nodes and pushes the result onto the value stack.
   * 
   * @param rand1               The first string node.
   * @param currentControlStack The current control stack.
   */
  private void conc(ASTNode rand1, Stack<ASTNode> currentControlStack) {
    currentControlStack.pop();
    ASTNode rand2 = valueStack.pop();
    if (rand1.getType() != ASTNodeType.STRING || rand2.getType() != ASTNodeType.STRING)
      try {
        throw new Exception();
      } catch (Exception e) {
        e.printStackTrace();
      }

    ASTNode result = new ASTNode();
    result.setType(ASTNodeType.STRING);
    result.setValue(rand1.getValue() + rand2.getValue());

    valueStack.push(result);
  }

  /**
   * Converts the given integer node to a string node.
   * 
   * @param rand The integer node to be converted.
   */
  private void itos(ASTNode rand) {
    if (rand.getType() != ASTNodeType.INTEGER)
      try {
        throw new Exception();
      } catch (Exception e) {
        e.printStackTrace();
      }

    rand.setType(ASTNodeType.STRING); // All values are stored internally as strings, so nothing else to do
    valueStack.push(rand);
  }

  /**
   * Retrieves the number of elements in the tuple node and pushes it as an
   * integer node onto the value stack.
   * 
   * @param rand The tuple node.
   */
  private void order(ASTNode rand) {
    if (rand.getType() != ASTNodeType.TUPLE)
      try {
        throw new Exception();
      } catch (Exception e) {
        e.printStackTrace();
      }

    ASTNode result = new ASTNode();
    result.setType(ASTNodeType.INTEGER);
    result.setValue(Integer.toString(getNumChildren(rand)));

    valueStack.push(result);
  }

  /**
   * Checks if the tuple node is empty and pushes the appropriate truth value
   * (True/False) onto the value stack.
   * 
   * @param rand The tuple node.
   */
  private void isNullTuple(ASTNode rand) {
    if (rand.getType() != ASTNodeType.TUPLE)
      try {
        throw new Exception();
      } catch (Exception e) {
        e.printStackTrace();
      }

    if (getNumChildren(rand) == 0)
      pushTrueNode();
    else
      pushFalseNode();
  }

  // RULE 10
  /**
   * Selects the element at the specified index from the tuple node and pushes it
   * onto the value stack.
   *
   * @param tupleNode The tuple node from which to select the element.
   * @param indexNode The integer node specifying the index of the element to
   *                  select (indices start from 1).
   */
  private void tupleSelection(Tuple rator, ASTNode rand) {
    if (rand.getType() != ASTNodeType.INTEGER)
      try {
        throw new Exception();
      } catch (Exception e) {
        e.printStackTrace();
      }

    ASTNode result = getNthTupleChild(rator, Integer.parseInt(rand.getValue()));
    if (result == null)
      try {
        throw new Exception();
      } catch (Exception e) {
        e.printStackTrace();
      }

    valueStack.push(result);
  }

  /**
   * Retrieves the nth element of the given tuple node.
   * The index of the element to retrieve starts from 1 (not 0).
   * If the index is out of bounds, this method returns null.
   *
   * @param tupleNode The tuple node from which to retrieve the element.
   * @param index     The index of the element to retrieve (starts from 1).
   * @return The nth element of the tuple, or null if the index is out of bounds.
   */
  private ASTNode getNthTupleChild(Tuple tupleNode, int n) {
    ASTNode childNode = tupleNode.getChild();
    for (int i = 1; i < n; ++i) { // Tuple selection index starts at 1
      if (childNode == null)
        break;
      childNode = childNode.getSibling();
    }
    return childNode;
  }

  /**
   * Handles identifier nodes by looking up their corresponding values in the
   * current environment.
   * If the identifier is found in the current environment, its associated value
   * is pushed onto the value stack. If the identifier is a reserved identifier,
   * the identifier node itself is pushed onto the value stack. If the identifier
   * is not found in the current environment, an exception is thrown.
   *
   * @param identifierNode The identifier node to be handled.
   * @param currentEnv     The current environment to look up the identifier.
   */
  private void handleIdentifiers(ASTNode node, Environment currentEnv) {
    if (currentEnv.lookup(node.getValue()) != null) // RULE 1
      valueStack.push(currentEnv.lookup(node.getValue()));
    else if (isReservedIdentifier(node.getValue()))
      valueStack.push(node);
    else
      try {
        throw new Exception();
      } catch (Exception e) {
        e.printStackTrace();
      }
  }

  // RULE 9
  /**
   * Creates a new tuple node and pushes it onto the value stack.
   * The tuple node is constructed by popping the required number of elements
   * from the value stack and linking them together as the children of the tuple.
   *
   * @param node The node from which to create the tuple.
   */
  private void createTuple(ASTNode node) {
    int numChildren = getNumChildren(node);
    Tuple tupleNode = new Tuple();

    if (numChildren == 0) {
      valueStack.push(tupleNode);
      return;
    }

    ASTNode childNode = null, tempNode = null;
    for (int i = 0; i < numChildren; ++i) {
      if (childNode == null)
        childNode = valueStack.pop();
      else if (tempNode == null) {
        tempNode = valueStack.pop();
        childNode.setSibling(tempNode);
      } else {
        tempNode.setSibling(valueStack.pop());
        tempNode = tempNode.getSibling();
      }
    }

    tempNode.setSibling(null);
    tupleNode.setChild(childNode);
    valueStack.push(tupleNode);
  }

  // RULE 8
  private void handleBeta(Beta node, Stack<ASTNode> currentControlStack) {
    ASTNode conditionResultNode = valueStack.pop();

    if (conditionResultNode.getType() != ASTNodeType.TRUE && conditionResultNode.getType() != ASTNodeType.FALSE)
      try {
        throw new Exception();
      } catch (Exception e) {
        e.printStackTrace();
      }

    if (conditionResultNode.getType() == ASTNodeType.TRUE)
      currentControlStack.addAll(node.getThenBody());
    else
      currentControlStack.addAll(node.getElseBody());
  }

  /**
   * Calculates the number of children of a given node.
   * 
   * @param node The node to count the children of.
   * @return The number of children of the given node.
   */
  private int getNumChildren(ASTNode node) {
    int numChildren = 0;
    ASTNode childNode = node.getChild();
    while (childNode != null) {
      numChildren++;
      childNode = childNode.getSibling();
    }
    return numChildren;
  }

  /**
   * Prints the value of the given node.
   * 
   * @param rand The node whose value to print.
   */
  private void printNodeValue(ASTNode rand) {
    String evaluationResult = rand.getValue();
    evaluationResult = evaluationResult.replace("\\t", "\t");
    evaluationResult = evaluationResult.replace("\\n", "\n");
    System.out.print(evaluationResult);
  }

  /**
   * Checks if the given identifier value is a reserved identifier.
   * Reserved identifiers are pre-defined names in the language that have special
   * meanings and functionalities.
   *
   * @param identifierValue The value of the identifier to be checked.
   * @return True if the identifier value is a reserved identifier, false
   *         otherwise.
   */
  private boolean isReservedIdentifier(String value) {
    switch (value) {
      case "Isinteger":
      case "Isstring":
      case "Istuple":
      case "Isdummy":
      case "Istruthvalue":
      case "Isfunction":
      case "ItoS":
      case "Order":
      case "Conc":
      case "conc":
      case "Stern":
      case "Stem":
      case "Null":
      case "Print":
      case "print":
      case "neg":
        return true;
    }
    return false;
  }

}
