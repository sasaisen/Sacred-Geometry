package sg.common;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import java.util.HashSet;
import java.util.Set;

public final class SacredGeometryCalculator {
  /**
   * Workhorse function. General algorithm is: <LIST>
   * <LI>Insert each roll as a stand-alone postfix expression into the set of either "odd" or "even"
   *     expressions.
   * <LI>Recursively condense these expressions into a single expression and check if it evaluates
   *     to a target number. See {@link SacredGeometryCalculator#calculateHelper(Set, Multiset,
   *     Multiset, Set)}
   * <LI>Return either a completed expression if found, or empty. </LIST>
   *
   * @param rollSet the initial set of dice rolls, usually 2-20 values from 1 to 6 or 8
   * @param targetSet the set of target numbers, one of which needs to be reached
   * @return a postfix expression that evaluates to one of the target numbers and uses all rolls
   *     from the original set, or an empty string if no such expression could be found
   */
  public static String calculate(Multiset<Integer> rollSet, Set<Long> targetSet) {
    ImmutableMultiset.Builder<PostfixExpression> odds = ImmutableMultiset.builder();
    ImmutableMultiset.Builder<PostfixExpression> evens = ImmutableMultiset.builder();

    for (Entry<Integer> e : rollSet.entrySet()) {
      if (e.getElement() % 2 == 0) {
        evens.addCopies(PostfixExpression.create(e.getElement()), e.getCount());
      } else {
        odds.addCopies(PostfixExpression.create(e.getElement()), e.getCount());
      }
    }

    return calculateHelper(new HashSet<>(), odds.build(), evens.build(), targetSet);
  }

  /**
   * Combines two postfix expressions from the passed sets with an operation (+-*\/) into a new
   * expression and recurses. Once only a single expression remains, compares it to our set of
   * target numbers, and returns either the expression string all the way up the chain (if
   * successful), or empty (if not). If "every" combination of expressions and operators has been
   * evaluated to no avail, return empty. Actually evaluating every permutation would be insanely
   * slow and expensive; fortunately, there's tricks to significantly speed things along with little
   * or no compromise to accuracy: <LIST>
   * <LI>Each time an expression set is iterated through, the evaluated result of each expression is
   *     stored for the scope of that iteration. This allows us to skip repeated or "identical"
   *     (evaluates to the same result) expressions later in the iteration.
   * <LI>Similarly, every expression set encountered is stored in evaluated form as it is
   *     encountered. At the start of each branch, if an identical set of (evaluated) expressions is
   *     already present, we know can skip this branch entirely. This eliminates huge swathes of
   *     otherwise-repeated computation.
   * <LI>Since target numbers are prime, only some "final" expression combinations can generate a
   *     target number, specifically: <LIST>
   * <LI>Odd + Even
   * <LI>Odd - Even
   * <LI>Even - Odd
   * <LI>Odd / Odd
   * <LI>Even / Even </LIST>These operations are given precedence over the rest, so that a target
   *     number will be found "sooner" in a branch.
   * <LI>Intermediate expressions that don't evaluate to an integer are disregarded (i.e.
   *     multiplication is required before division), which is greatly simplifying in that only
   *     integer handling is required. This is the only optimization that could harm accuracy, as it
   *     skips cases where a solution divides by 2 or 3 and then later multiplies by 6 (e.g. "1 2 /
   *     3 + 6 * 2 +" for result 23); however, I have yet to encounter an instance of this where the
   *     algorithm does not successfully find an alternate solution. Cases that divide by 2 and
   *     multiply by 4 would similarly be affected, but this will never cause issues because doing
   *     so is equivalent to multiplying by 2. (e.g. "1 2 / 3 + 4 * 5 +" and "1 2 * 3 4 * + 5 +" are
   *     both valid for result 19) </LIST>
   *
   * @param valueSets set of already encountered expression sets, stored in evaluated form
   * @param odds set of postfix expressions that evaluate to odd integers
   * @param evens set of postfix expressions that evaluate to even integers
   * @param targetSet the set of target numbers, one of which needs to be reached
   * @return a postfix expression that evaluates to one of the target numbers and uses all rolls
   *     from the original set, or an empty string if no such expression could be found
   */
  static String calculateHelper(
      Set<Multiset<Long>> valueSets,
      Multiset<PostfixExpression> odds,
      Multiset<PostfixExpression> evens,
      Set<Long> targetSet) {
    Set<Long> iSet;
    Set<Long> jSet;
    String result;

    // No point evaluating this set if we've already encountered a similar set; we know it'll fail.
    ImmutableMultiset<Long> currentValueSet =
        ImmutableMultiset.<Long>builder()
            .addAll(odds.stream().map(PostfixExpression::expressionResult).iterator())
            .addAll(evens.stream().map(PostfixExpression::expressionResult).iterator())
            .build();
    if (valueSets.contains(currentValueSet)) {
      return "";
    }
    valueSets.add(currentValueSet);

    // We've condensed down to one expression; if it's odd it may be a solution.
    if (odds.size() + evens.size() <= 1) {
      for (PostfixExpression expression : odds) {
        return targetSet.contains(expression.expressionResult())
            ? expression.expressionString()
            : "";
      }
      return "";
    }

    // Combine two expressions from either set and recurse
    // O -> odd, E -> even
    // Blocks are split so that operations which can generate a target number are evaluated first
    // (Specifically, additions/subtractions/divisions that produce an odd result)

    // The following ops may generate a target number
    // O x E or E x O
    iSet = new HashSet<>();
    for (PostfixExpression exp1 : odds.elementSet()) {
      if (iSet.contains(exp1.expressionResult())) {
        continue;
      }
      iSet.add(exp1.expressionResult());
      jSet = new HashSet<>();
      for (PostfixExpression exp2 : evens.elementSet()) {
        if (jSet.contains(exp2.expressionResult())) {
          continue;
        }
        jSet.add(exp2.expressionResult());

        // O + E => O, equivalently E + O => O
        // O - E => O
        for (Operator operator : Operator.ADD_SUB) {
          result =
              calculateHelper(
                  valueSets,
                  newSet(PostfixExpression.create(exp1, exp2, operator), odds, exp1),
                  newSet(evens, exp2),
                  targetSet);
          if (!Strings.isNullOrEmpty(result)) {
            return result;
          }
        }

        // E - O => O
        result =
            calculateHelper(
                valueSets,
                newSet(PostfixExpression.create(exp2, exp1, Operator.SUB), odds, exp1),
                newSet(evens, exp2),
                targetSet);
        if (!Strings.isNullOrEmpty(result)) {
          return result;
        }
      }
    }

    // O x O
    iSet = new HashSet<>();
    for (PostfixExpression exp1 : odds.elementSet()) {
      if (iSet.contains(exp1.expressionResult())) {
        continue;
      }
      iSet.add(exp1.expressionResult());
      jSet = new HashSet<>();
      for (PostfixExpression exp2 : odds.elementSet()) {
        if (exp1 == exp2 && odds.count(exp1) == 1) {
          continue; // Don't operate an expression against itself, unless present more than once
        }
        if (jSet.contains(exp2.expressionResult())) {
          continue;
        }
        jSet.add(exp2.expressionResult());

        // O / O => O, skip if not an integer
        if (exp2.expressionResult() == 0
            || exp1.expressionResult() % exp2.expressionResult() != 0) {
          continue;
        }
        result =
            calculateHelper(
                valueSets,
                newSet(PostfixExpression.create(exp1, exp2, Operator.DIV), odds, exp1, exp2),
                evens,
                targetSet);
        if (!Strings.isNullOrEmpty(result)) {
          return result;
        }
      }
    }

    // E x E
    iSet = new HashSet<>();
    for (PostfixExpression exp1 : evens.elementSet()) {
      if (iSet.contains(exp1.expressionResult())) {
        continue;
      }
      iSet.add(exp1.expressionResult());
      jSet = new HashSet<>();
      for (PostfixExpression exp2 : evens.elementSet()) {
        if (exp1 == exp2 && evens.count(exp1) == 1) {
          continue; // Don't operate an expression against itself, unless present more than once
        }
        if (jSet.contains(exp2.expressionResult())) {
          continue;
        }
        jSet.add(exp2.expressionResult());

        // E / E => O or E, skip if not an integer
        if (exp2.expressionResult() == 0
            || exp1.expressionResult() % exp2.expressionResult() != 0) {
          continue;
        }
        if (exp1.expressionResult() % 4 == 0) {
          result =
              calculateHelper(
                  valueSets,
                  odds,
                  newSet(PostfixExpression.create(exp1, exp2, Operator.DIV), evens, exp1, exp2),
                  targetSet);
        } else {
          result =
              calculateHelper(
                  valueSets,
                  newSet(PostfixExpression.create(exp1, exp2, Operator.DIV), odds),
                  newSet(evens, exp1, exp2),
                  targetSet);
        }
        if (!Strings.isNullOrEmpty(result)) {
          return result;
        }
      }
    }

    // The remaining ops will never generate a target number
    // If only two expressions remain, these operations can be skipped
    if (odds.size() + evens.size() <= 2) {
      return "";
    }

    // O x E or E x O
    iSet = new HashSet<>();
    for (PostfixExpression exp1 : odds.elementSet()) {
      if (iSet.contains(exp1.expressionResult())) {
        continue;
      }
      iSet.add(exp1.expressionResult());
      jSet = new HashSet<>();
      for (PostfixExpression exp2 : evens.elementSet()) {
        if (jSet.contains(exp2.expressionResult())) {
          continue;
        }
        jSet.add(exp2.expressionResult());

        // O * E => E, equivalently E * O => E
        result =
            calculateHelper(
                valueSets,
                newSet(odds, exp1),
                newSet(PostfixExpression.create(exp1, exp2, Operator.MULT), evens, exp2),
                targetSet);
        if (!Strings.isNullOrEmpty(result)) {
          return result;
        }

        // E / O => E, skip if not an integer
        if (exp1.expressionResult() == 0
            || exp2.expressionResult() % exp1.expressionResult() != 0) {
          continue;
        }
        result =
            calculateHelper(
                valueSets,
                newSet(odds, exp1),
                newSet(PostfixExpression.create(exp2, exp1, Operator.DIV), evens, exp2),
                targetSet);
        if (!Strings.isNullOrEmpty(result)) {
          return result;
        }

        // O / E  will never produce an integer, skip entirely
      }
    }

    // O x O
    iSet = new HashSet<>();
    for (PostfixExpression exp1 : odds.elementSet()) {
      if (iSet.contains(exp1.expressionResult())) {
        continue;
      }
      iSet.add(exp1.expressionResult());
      jSet = new HashSet<>();
      for (PostfixExpression exp2 : odds.elementSet()) {
        if (exp1 == exp2 && odds.count(exp1) == 1) {
          continue; // Don't operate an expression against itself, unless present more than once
        }
        if (jSet.contains(exp2.expressionResult())) {
          continue;
        }
        jSet.add(exp2.expressionResult());

        // O + O => E
        // O - O => E
        // O * O => O
        for (Operator operator : Operator.ADD_SUB_MULT) {
          result =
              calculateHelper(
                  valueSets,
                  newSet(odds, exp1, exp2),
                  newSet(PostfixExpression.create(exp1, exp2, operator), evens),
                  targetSet);
          if (!Strings.isNullOrEmpty(result)) {
            return result;
          }
        }
      }
    }

    // E x E
    iSet = new HashSet<>();
    for (PostfixExpression exp1 : evens.elementSet()) {
      if (iSet.contains(exp1.expressionResult())) {
        continue;
      }
      iSet.add(exp1.expressionResult());
      jSet = new HashSet<>();
      for (PostfixExpression exp2 : evens.elementSet()) {
        if (exp1 == exp2 && evens.count(exp1) == 1) {
          continue; // Don't operate an expression against itself, unless present more than once
        }
        if (jSet.contains(exp2.expressionResult())) {
          continue;
        }
        jSet.add(exp2.expressionResult());

        // E + E => E
        // E - E => E
        // E * E => E
        for (Operator operator : Operator.ADD_SUB_MULT) {
          result =
              calculateHelper(
                  valueSets,
                  odds,
                  newSet(PostfixExpression.create(exp1, exp2, operator), evens, exp1, exp2),
                  targetSet);
          if (!Strings.isNullOrEmpty(result)) {
            return result;
          }
        }
      }
    }

    return "";
  }

  @SafeVarargs
  static <E> Multiset<E> newSet(Multiset<E> expSet, E... expsToRemove) {
    ImmutableMultiset.Builder<E> setBuilder = ImmutableMultiset.<E>builder().addAll(expSet);
    for (Entry<E> e : ImmutableMultiset.copyOf(expsToRemove).entrySet()) {
      setBuilder.setCount(e.getElement(), expSet.count(e.getElement()) - e.getCount());
    }
    return setBuilder.build();
  }

  @SafeVarargs
  static <E> Multiset<E> newSet(E expToAdd, Multiset<E> expSet, E... expsToRemove) {
    ImmutableMultiset.Builder<E> setBuilder = ImmutableMultiset.<E>builder().addAll(expSet);
    for (Entry<E> e : ImmutableMultiset.copyOf(expsToRemove).entrySet()) {
      setBuilder.setCount(e.getElement(), expSet.count(e.getElement()) - e.getCount());
    }
    return setBuilder.add(expToAdd).build();
  }
}