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

  public static PostfixExpression create(PostfixExpression exp1, PostfixExpression exp2, char op) {
    PostfixExpression exp = new AutoValue_PostfixExpression(exp1.expressionString() + exp2.expressionString() + op);
    exp.expressionResult = switch (op) {
      case Operators.ADD -> exp1.expressionResult + exp2.expressionResult;
      case Operators.SUB -> exp1.expressionResult - exp2.expressionResult;
      case Operators.MULT -> exp1.expressionResult * exp2.expressionResult;
      case Operators.DIV -> {
        if (exp2.expressionResult == 0 || exp1.expressionResult % exp2.expressionResult != 0) {
          throw new IllegalStateException(
              String.format("%s divided by %s does not result in an integer", exp1, exp2));
        }
        yield exp1.expressionResult / exp2.expressionResult;
      }

      default -> throw new IllegalArgumentException(op + " is not a valid operator");
    };
    return exp;
  }

  private static long evaluate(String expression) {
    Stack<Long> stack = new Stack<>();
    for (char c : expression.toCharArray()) {
      switch (c) {
        case '1', '2', '3', '4', '5', '6', '7', '8' -> stack.push((long) c - '0');
        default -> {
          Long op2 = stack.pop();
          Long op1 = stack.pop();
          switch (c) {
            case Operators.ADD -> stack.push(op1 + op2);
            case Operators.SUB -> stack.push(op1 - op2);
            case Operators.MULT -> stack.push(op1 * op2);
            case Operators.DIV -> {
              if (op2 == 0 || op1 % op2 != 0) {
                throw new IllegalStateException(
                    String.format("%s divided by %s does not result in an integer", op1, op2));
              }
              stack.push(op1 / op2);
            }
            default -> throw new IllegalArgumentException(
                c + " is not a valid dice roll or operator");
          }
        }
      }
    }
    Long result = stack.pop();
    if (!stack.empty()) {
      throw new IllegalArgumentException(expression + " is not a complete postfix expression");
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