package sg.common;

import com.google.auto.value.AutoValue;
import java.util.Stack;

@AutoValue
public abstract class PostfixExpression {
  private long expressionResult;

  public static PostfixExpression create(int n) {
    PostfixExpression exp = new AutoValue_PostfixExpression(String.valueOf(n));
    exp.expressionResult = n;
    return exp;
  }

  public static PostfixExpression create(String s) {
    PostfixExpression exp = new AutoValue_PostfixExpression(s);
    exp.expressionResult = evaluate(s);
    return exp;
  }

  public static PostfixExpression create(PostfixExpression exp1, PostfixExpression exp2, Operators op) {
    PostfixExpression exp = new AutoValue_PostfixExpression(exp1.expressionString() + exp2.expressionString() + op.character());
    exp.expressionResult = switch (op) {
      case ADD -> exp1.expressionResult + exp2.expressionResult;
      case SUB -> exp1.expressionResult - exp2.expressionResult;
      case MULT -> exp1.expressionResult * exp2.expressionResult;
      case DIV -> {
        if (exp2.expressionResult == 0 || exp1.expressionResult % exp2.expressionResult != 0) {
          throw new IllegalStateException(
              String.format("%s divided by %s does not result in an integer", exp1, exp2));
        }
        yield exp1.expressionResult / exp2.expressionResult;
      }
    };
    return exp;
  }

  private static long evaluate(String expression) {
    Stack<Long> stack = new Stack<>();
    for (char c : expression.toCharArray()) {
      switch (c) {
        case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> stack.push((long) c - '0');
        default -> {
          Operators operator = Operators.fromCharacter(c);
          if (operator != null) {
            Long operand2 = stack.pop();
            Long operand1 = stack.pop();
            switch (Operators.fromCharacter(c)) {
              case ADD -> stack.push(operand1 + operand2);
              case SUB -> stack.push(operand1 - operand2);
              case MULT -> stack.push(operand1 * operand2);
              case DIV -> {
                if (operand2 == 0 || operand1 % operand2 != 0) {
                  throw new IllegalStateException(
                      String.format("%s divided by %s does not result in an integer", operand1, operand2));
                }
                stack.push(operand1 / operand2);
              }
            }
          }
          else {
            throw new IllegalStateException(c + " is not a valid dice roll or operator");
          }
        }

      }
    }
    Long result = stack.pop();
    if (!stack.empty()) {
      throw new IllegalStateException(expression + " is not a complete postfix expression");
    }
    return result;
  }

  public abstract String expressionString();

  public long expressionResult() {
    return expressionResult;
  }

  @Override
  public String toString() {
    return expressionString();
  }
}