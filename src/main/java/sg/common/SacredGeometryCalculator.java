package sg.common;

import static sg.common.SetUtil.difference;
import static sg.common.SetUtil.isSupersetOf;
import static sg.common.SetUtil.newSet;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public final class SacredGeometryCalculator {
  private static final Map<Targets, Map<Multiset<Integer>, PostfixExpression>> sgExpressionSets;

  static {
    try {
      sgExpressionSets =
          ImmutableMap.<Targets, Map<Multiset<Integer>, PostfixExpression>>builder()
              .put(Targets.ZERO, FileUtil.readFile(FileUtil.SG_0))
              .put(Targets.ONE, FileUtil.readFile(FileUtil.SG_1))
              .put(Targets.TWO, FileUtil.readFile(FileUtil.SG_2))
              .put(Targets.THREE, FileUtil.readFile(FileUtil.SG_3))
              .put(Targets.FOUR, FileUtil.readFile(FileUtil.SG_4))
              .put(Targets.FIVE, FileUtil.readFile(FileUtil.SG_5))
              .put(Targets.SIX, FileUtil.readFile(FileUtil.SG_6))
              .put(Targets.SEVEN, FileUtil.readFile(FileUtil.SG_7))
              .put(Targets.EIGHT, FileUtil.readFile(FileUtil.SG_8))
              .put(Targets.NINE, FileUtil.readFile(FileUtil.SG_9))
              .build();
    } catch (IOException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  public static String calculate(Multiset<Integer> rollSet, Targets target) {
    Map<Multiset<Integer>, PostfixExpression> sgExpressionSet = sgExpressionSets.get(target);

    for (Entry<Multiset<Integer>, PostfixExpression> e : sgExpressionSet.entrySet()) {
      if (!isSupersetOf(rollSet, e.getKey())) {
        continue;
      }

      if (rollSet.size() == e.getKey().size()) {
        return e.getValue().expressionString();
      }

      Multiset<Integer> remainderSet = difference(rollSet, e.getKey());

      for (Entry<Multiset<Integer>, PostfixExpression> z :
          sgExpressionSets.get(Targets.ZERO).entrySet()) {
        if (!isSupersetOf(remainderSet, z.getKey())) {
          continue;
        }

        StringBuilder result = new StringBuilder().append(e.getValue()).append(z.getValue());

        for (Integer i : difference(remainderSet, z.getKey())) {
          result.append(i).append(Operators.MULT.character());
        }
        return result.append(Operators.ADD.character()).toString();
      }
    }

    return calculateWithoutLookup(rollSet, target.targetValues());
  }

  static String calculateWithoutLookup(Multiset<Integer> rollSet, Set<Long> targetSet) {
    ImmutableMultiset.Builder<PostfixExpression> exps = ImmutableMultiset.builder();

    for (Multiset.Entry<Integer> e : rollSet.entrySet()) {
      exps.addCopies(PostfixExpression.create(e.getElement()), e.getCount());
    }

    return calculateHelper(new HashSet<>(), exps.build(), targetSet);
  }

  static String calculateHelper(
      Set<Multiset<Long>> valueSets, Multiset<PostfixExpression> exps, Set<Long> targetSet) {
    Set<Long> iSet;
    Set<Long> jSet;
    String result;

    // No point evaluating this set if we've already encountered a similar set; we know it'll fail.
    ImmutableMultiset<Long> currentValueSet =
        ImmutableMultiset.<Long>builder()
            .addAll(exps.stream().map(PostfixExpression::expressionResult).iterator())
            .build();
    if (valueSets.contains(currentValueSet)) {
      return "";
    }
    valueSets.add(currentValueSet);

    // We've condensed down to one expression; it may be a solution.
    if (exps.size() <= 1) {
      for (PostfixExpression expression : exps) {
        return targetSet.contains(expression.expressionResult())
            ? expression.expressionString()
            : "";
      }
      return "";
    }

    iSet = new HashSet<>();
    for (PostfixExpression exp1 : exps.elementSet()) {
      if (iSet.contains(exp1.expressionResult())) {
        continue;
      }
      iSet.add(exp1.expressionResult());
      jSet = new HashSet<>();
      for (PostfixExpression exp2 : exps.elementSet()) {
        if (exp1 == exp2 && exps.count(exp1) == 1) {
          continue; // Don't operate an expression against itself, unless present more than once
        }
        if (jSet.contains(exp2.expressionResult())) {
          continue;
        }
        jSet.add(exp2.expressionResult());

        for (Operators operator : Operators.ADD_SUB_MULT) {
          result =
              calculateHelper(
                  valueSets,
                  newSet(PostfixExpression.create(exp1, exp2, operator), exps, exp1, exp2),
                  targetSet);
          if (!Strings.isNullOrEmpty(result)) {
            return result;
          }
        }

        if (exp2.expressionResult() == 0
            || exp1.expressionResult() % exp2.expressionResult() != 0) {
          continue;
        }
        result =
            calculateHelper(
                valueSets,
                newSet(PostfixExpression.create(exp1, exp2, Operators.DIV), exps, exp1, exp2),
                targetSet);
        if (!Strings.isNullOrEmpty(result)) {
          return result;
        }
      }
    }

    return "";
  }
}
