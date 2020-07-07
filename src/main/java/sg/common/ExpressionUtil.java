package sg.common;

import static sg.common.Operators.ADD;
import static sg.common.Operators.ADD_SUB;
import static sg.common.Operators.DIV;
import static sg.common.Operators.MULT;
import static sg.common.Operators.MULT_DIV;
import static sg.common.Operators.OPS;
import static sg.common.Operators.SUB;

public class ExpressionUtil {
  static String postfixToInfix(String postfix) {
    return buildInOrder(buildTree(postfix), false).toString();
  }

  static TreeNode<Character> buildTree(String postfix) {
    char value = postfix.charAt(postfix.length() - 1);
    TreeNode<Character> left = null;
    TreeNode<Character> right = null;

    if (OPS.contains(Operators.fromCharacter(value))) {
      right = buildTree(postfix.substring(0, postfix.length() - 1));
      left = buildTree(postfix.substring(0, postfix.length() - 1 - right.size()));
    }
    return new TreeNode<>(value, left, right);
  }

  static StringBuilder buildInOrder(TreeNode<Character> root, boolean invertOp) {
    StringBuilder stringBuilder = new StringBuilder();

    Operators valueOp = Operators.fromCharacter(root.value());
    Operators leftOp = root.left() != null ? Operators.fromCharacter(root.left().value()) : null;
    Operators rightOp = root.right() != null ? Operators.fromCharacter(root.right().value()) : null;

    if (root.left() != null) {
      boolean parentheses = MULT_DIV.contains(valueOp) && ADD_SUB.contains(leftOp);
      stringBuilder
          .append(parentheses ? "(" : "")
          .append(buildInOrder(root.left(), false))
          .append(parentheses ? ")" : "");
    }

    boolean space = (valueOp != null);
    stringBuilder.append(space ? " " : "");
    if (invertOp && valueOp != null) {
      switch (valueOp) {
        case ADD -> stringBuilder.append(SUB.character());
        case SUB -> stringBuilder.append(ADD.character());
        case MULT -> stringBuilder.append(DIV.character());
        case DIV -> stringBuilder.append(MULT.character());
      }
    } else {
      stringBuilder.append(root.value());
    }
    stringBuilder.append(space ? " " : "");

    if (root.right() != null) {
      boolean parentheses = MULT_DIV.contains(valueOp) && ADD_SUB.contains(rightOp);
      boolean invertChildOp =
          (valueOp == DIV && MULT_DIV.contains(rightOp))
              || (valueOp == SUB && ADD_SUB.contains(rightOp));
      stringBuilder
          .append(parentheses ? "(" : "")
          .append(buildInOrder(root.right(), invertChildOp))
          .append(parentheses ? ")" : "");
    }

    return stringBuilder;
  }
}
