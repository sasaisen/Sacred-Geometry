package sg.common;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMultiset;
import com.google.common.collect.Multiset;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SacredGeometryUtil {
  private static final String diceExpression = "(\\d+)d([68])";
  private static final Pattern dicePattern = Pattern.compile(diceExpression);

  public static void sacredGeometry(List<String> args, Consumer<String> output) {
    if (args.size() != 2) {
      output.accept("Exactly 2 arguments are required for Sacred Geometry:\n");
      output.accept("1) A dice expression (#d6 or #d8) OR string of dice rolls (from 1 to 8), between 2 and 20 dice;\n");
      output.accept("2) A target spell level (from 1 to 9).\n");
      return;
    }

    Multiset<Integer> rollSet = parseRollSet(args.get(0), output, true);

    Set<Long> targetSet = parseTargetSet(args.get(1), output);

    String result = SacredGeometryCalculator.calculate(rollSet, targetSet);
    if (!Strings.isNullOrEmpty(result)) {
      output.accept("Result: ");
      output.accept(postfixToInfix(result));
      output.accept(" = ");
      output.accept(String.valueOf(PostfixExpression.create(result).expressionResult()));
    } else {
      output.accept("No result could be found.");
    }
  }

  static Multiset<Integer> parseRollSet(String rollArg, Consumer<String> output, boolean capRolls) {
    ImmutableSortedMultiset.Builder<Integer> builder = ImmutableSortedMultiset.naturalOrder();

    Matcher diceMatcher = dicePattern.matcher(rollArg);
    if (diceMatcher.matches()) {
      int diceNumber = Integer.parseInt(diceMatcher.group(1));
      if (capRolls && (diceNumber > 20 || diceNumber < 2)) {
        throw new IllegalArgumentException("The number of rolls must be between 2 and 20 (inclusive).");
      }

      int diceSize = Integer.parseInt(diceMatcher.group(2));
      output.accept("Rolling ");
      output.accept(rollArg);
      output.accept("\n");
      for (int i = 0; i < diceNumber; i++) {
        builder.add(ThreadLocalRandom.current().nextInt(0, diceSize)+1);
      }
    } else {
      if (capRolls && (rollArg.length() > 20 || rollArg.length() < 2)) {
        throw new IllegalArgumentException("The number of rolls must be between 2 and 20 (inclusive).");
      }

      for (char c : rollArg.toCharArray()) {
        switch (c) {
          case '1', '2', '3', '4', '5', '6', '7', '8' -> builder.add(Character.getNumericValue(c));
          default -> throw new IllegalArgumentException(
              rollArg + " is not a valid dice expression (#d6 or #d8) or set of dice rolls (from 1 to 8).");
        }
      }
    }

    ImmutableSortedMultiset<Integer> rollSet = builder.build();
    output.accept("Dice rolls are: ");
    output.accept(rollSet.toString());
    output.accept("\n");
    return rollSet;
  }

  static Set<Long> parseTargetSet(String targetArg, Consumer<String> output) {
    Set<Long> targetSet = switch (targetArg) {
      case "1" -> ImmutableSet.of(3L, 5L, 7L);
      case "2" -> ImmutableSet.of(11L, 13L, 17L);
      case "3" -> ImmutableSet.of(19L, 23L, 29L);
      case "4" -> ImmutableSet.of(31L, 37L, 41L);
      case "5" -> ImmutableSet.of(43L, 47L, 53L);
      case "6" -> ImmutableSet.of(59L, 61L, 67L);
      case "7" -> ImmutableSet.of(71L, 73L, 79L);
      case "8" -> ImmutableSet.of(83L, 89L, 97L);
      case "9" -> ImmutableSet.of(101L, 103L, 107L);
      default -> throw new IllegalArgumentException(
          targetArg + " is not a valid spell level (from 1 to 9).");
    };
    output.accept("Target numbers are: ");
    output.accept(targetSet.toString());
    output.accept("\n");
    return targetSet;
  }

  static String postfixToInfix(String postfix) {
    return buildInOrder(buildTree(postfix)).toString();
  }

  static TreeNode<Character> buildTree(String postfix) {
    Character value = postfix.charAt(postfix.length() - 1);
    TreeNode<Character> left = null;
    TreeNode<Character> right = null;

    if (Operators.OPS.contains(value)) {
      right = buildTree(postfix.substring(0, postfix.length() - 1));
      left = buildTree(postfix.substring(0, postfix.length() - 1 - right.size()));
    }
    return new TreeNode<>(value, left, right);
  }

  static StringBuilder buildInOrder(TreeNode<Character> root) {
    StringBuilder stringBuilder = new StringBuilder();

    if (root.left() != null) {
      boolean parentheses = Operators.MULT_DIV.contains(root.value()) && Operators.ADD_SUB.contains(root.left().value());
      stringBuilder
          .append(parentheses ? "(" : "")
          .append(buildInOrder(root.left()))
          .append(parentheses ? ")" : "");
    }

    boolean space = Operators.OPS.contains(root.value());
    stringBuilder.append(space ? " " : "").append(root.value()).append(space ? " " : "");

    if (root.right() != null) {
      boolean parentheses =
          (Operators.MULT_DIV.contains(root.value()) && Operators.ADD_SUB.contains(root.right().value()))
              || (root.value() == Operators.DIV && root.right().value() == Operators.MULT)
              || (root.value() == Operators.SUB && root.right().value() == Operators.ADD);
      stringBuilder
          .append(parentheses ? "(" : "")
          .append(buildInOrder(root.right()))
          .append(parentheses ? ")" : "");
    }

    return stringBuilder;
  }
}
