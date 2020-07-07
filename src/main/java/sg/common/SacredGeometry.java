package sg.common;

import static sg.common.ExpressionUtil.postfixToInfix;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSortedMultiset;
import com.google.common.collect.Multiset;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SacredGeometry {
  private static final String diceExpression = "(\\d+)d([68])";
  private static final Pattern dicePattern = Pattern.compile(diceExpression);

  public static void sacredGeometry(List<String> args, Consumer<String> output) {
    if (args.size() != 2) {
      output.accept("Exactly 2 arguments are required for Sacred Geometry:\n");
      output.accept("1) A dice expression (#d6 or #d8) OR string of dice rolls (from 1 to 8), between 2 and 20 dice;\n");
      output.accept("2) A target spell level (from 1 to 9).\n");
      return;
    }

    Multiset<Integer> rollSet = parseRollArg(args.get(0), output, true);

    Targets target = parseTargetArg(args.get(1), output);

    String result = SacredGeometryCalculator.calculate(rollSet, target);
    if (!Strings.isNullOrEmpty(result)) {
      output.accept("Result: ");
      output.accept(postfixToInfix(result));
      output.accept(" = ");
      output.accept(String.valueOf(PostfixExpression.create(result).expressionResult()));
    } else {
      output.accept("No result could be found.");
    }
  }

  static Multiset<Integer> parseRollArg(String rollArg, Consumer<String> output, boolean capRolls) {
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

  static Targets parseTargetArg(String targetArg, Consumer<String> output) {
    Targets target = switch (targetArg) {
      case "1" -> Targets.ONE;
      case "2" -> Targets.TWO;
      case "3" -> Targets.THREE;
      case "4" -> Targets.FOUR;
      case "5" -> Targets.FIVE;
      case "6" -> Targets.SIX;
      case "7" -> Targets.SEVEN;
      case "8" -> Targets.EIGHT;
      case "9" -> Targets.NINE;
      default -> throw new IllegalArgumentException(
          targetArg + " is not a valid spell level (from 1 to 9).");
    };
    output.accept("Target numbers are: ");
    output.accept(target.targetValues().toString());
    output.accept("\n");
    return target;
  }

}
