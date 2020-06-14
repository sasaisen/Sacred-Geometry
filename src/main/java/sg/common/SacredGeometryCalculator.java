package sg.common;

import static sg.common.Operator.ADD_SUB;
import static sg.common.Operator.DIV;
import static sg.common.Operator.OPS;
import static sg.common.Operator.SUB;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
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
   * <LI>Operations are prioritized so that target numbers are approached faster. Additionally, once
   *     the rolls have been condensed down to two expressions, operations that can't generate
   *     target numbers are skipped.
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

    switch (odds.size()) {
      case 0 -> {
        switch (evens.size()) {
          case 0, 1 -> {
            // No results
            return "";
          }
          case 2 -> {
            // E / E
            result = permuteEvensEvens(valueSets, odds, evens, targetSet, ImmutableSet.of(DIV));
            if (!Strings.isNullOrEmpty(result)) {
              return result;
            }
          }
          default -> {
            // E * E, E + E, E - E, E / E
            result = permuteEvensEvens(valueSets, odds, evens, targetSet, OPS);
            if (!Strings.isNullOrEmpty(result)) {
              return result;
            }
          }
        }
      }
      case 1 -> {
        switch (evens.size()) {
          case 0 -> {
            // Might be a target number
            for (PostfixExpression expression : odds) {
              if (targetSet.contains(expression.expressionResult())) {
                return expression.expressionString();
              }
            }
            return "";
          }
          case 1 -> {
            // O + E, O - E, E - O
            result = permuteOddsEvens(valueSets, odds, evens, targetSet, ADD_SUB);
            if (!Strings.isNullOrEmpty(result)) {
              return result;
            }
          }
          default -> {
            // O * E, O + E, O - E, E - O, E / O
            result = permuteOddsEvens(valueSets, odds, evens, targetSet, OPS);
            if (!Strings.isNullOrEmpty(result)) {
              return result;
            }

            // E * E, E + E, E - E, E / E
            result = permuteEvensEvens(valueSets, odds, evens, targetSet, OPS);
            if (!Strings.isNullOrEmpty(result)) {
              return result;
            }
          }
        }
      }
      case 2 -> {
        if (evens.size() == 0) {
          // O / O
          result = permuteOddsOdds(valueSets, odds, evens, targetSet, ImmutableSet.of(DIV));
          if (!Strings.isNullOrEmpty(result)) {
            return result;
          }
        } else {
          // O * E, O + E, O - E, E - O, E / O
          result = permuteOddsEvens(valueSets, odds, evens, targetSet, OPS);
          if (!Strings.isNullOrEmpty(result)) {
            return result;
          }

          // O * O, O + O, O - O, O / O
          result = permuteOddsOdds(valueSets, odds, evens, targetSet, OPS);
          if (!Strings.isNullOrEmpty(result)) {
            return result;
          }

          // E * E, E + E, E - E, E / E
          result = permuteEvensEvens(valueSets, odds, evens, targetSet, OPS);
          if (!Strings.isNullOrEmpty(result)) {
            return result;
          }
        }
      }
      default -> {
            // O * E, O + E, O - E, E - O, E / O
            result = permuteOddsEvens(valueSets, odds, evens, targetSet, OPS);
            if (!Strings.isNullOrEmpty(result)) {
              return result;
            }

            // O * O, O + O, O - O, O / O
            result = permuteOddsOdds(valueSets, odds, evens, targetSet, OPS);
            if (!Strings.isNullOrEmpty(result)) {
              return result;
            }

            // E * E, E + E, E - E, E / E
            result = permuteEvensEvens(valueSets, odds, evens, targetSet, OPS);
            if (!Strings.isNullOrEmpty(result)) {
              return result;
            }
        }
      }

    return "";
  }

  static String permuteOddsOdds(
      Set<Multiset<Long>> valueSets,
      Multiset<PostfixExpression> odds,
      Multiset<PostfixExpression> evens,
      Set<Long> targetSet,
      Set<Operator> operators) {
    Set<Long> iSet;
    Set<Long> jSet;
    Multiset<PostfixExpression> newOdds;
    Multiset<PostfixExpression> newEvens;
    String result;

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

        for (Operator op : operators) {
          switch (op) {
            case MULT -> {
              // O * O
              newOdds = newSet(PostfixExpression.create(exp1, exp2, op), odds, exp1, exp2);
              newEvens = evens;
            }
            case ADD, SUB -> {
              // O + O, O - O
              newOdds = newSet(odds, exp1, exp2);
              newEvens = newSet(PostfixExpression.create(exp1, exp2, op), evens);
            }
            case DIV -> {
              // O / O
              if (exp2.expressionResult() == 0
                  || exp1.expressionResult() % exp2.expressionResult() != 0) {
                continue;
              }
              newOdds = newSet(PostfixExpression.create(exp1, exp2, op), odds, exp1, exp2);
              newEvens = evens;
            }
            default -> throw new IllegalStateException(op + " is not a valid operator.");
          }
          result = calculateHelper(valueSets, newOdds, newEvens, targetSet);
          if (!Strings.isNullOrEmpty(result)) {
            return result;
          }
        }
      }
    }
    return "";
  }

  static String permuteOddsEvens(
      Set<Multiset<Long>> valueSets,
      Multiset<PostfixExpression> odds,
      Multiset<PostfixExpression> evens,
      Set<Long> targetSet,
      Set<Operator> operators) {
    Set<Long> iSet;
    Set<Long> jSet;
    Multiset<PostfixExpression> newOdds;
    Multiset<PostfixExpression> newEvens;
    String result;

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

        for (Operator op : operators) {
          switch (op) {
            case MULT -> {
              // O * E, E * O
              newOdds = newSet(odds, exp1);
              newEvens = newSet(PostfixExpression.create(exp1, exp2, op), evens, exp2);
            }
            case ADD, SUB -> {
              // O + E, E + O, O - E
              newOdds = newSet(PostfixExpression.create(exp1, exp2, op), odds, exp1);
              newEvens = newSet(evens, exp2);
            }
            case DIV -> {
              // E / O; O / E will never produce an integer.
              if (exp1.expressionResult() == 0
                  || exp2.expressionResult() % exp1.expressionResult() != 0) {
                continue;
              }
              newOdds = newSet(odds, exp1);
              newEvens = newSet(PostfixExpression.create(exp2, exp1, op), evens, exp2);
              }
            default -> throw new IllegalStateException(op + " is not a valid operator.");
          }
          result = calculateHelper(valueSets, newOdds, newEvens, targetSet);
          if (!Strings.isNullOrEmpty(result)) {
            return result;
          }

          // Extra case for E - O
          if (op == SUB) {
            result =
                calculateHelper(
                    valueSets,
                    newSet(PostfixExpression.create(exp2, exp1, op), odds, exp1),
                    newSet(evens, exp2),
                    targetSet);
          if (!Strings.isNullOrEmpty(result)) {
            return result;
            }
          }
        }
      }
    }
    return "";
  }

  static String permuteEvensEvens(
      Set<Multiset<Long>> valueSets,
      Multiset<PostfixExpression> odds,
      Multiset<PostfixExpression> evens,
      Set<Long> targetSet,
      Set<Operator> operators) {
    Set<Long> iSet;
    Set<Long> jSet;
    Multiset<PostfixExpression> newOdds;
    Multiset<PostfixExpression> newEvens;
    String result;

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

        for (Operator op : operators) {
          switch (op) {
            case MULT, ADD, SUB -> {
              // E * E, E + E, E - E
              newOdds = odds;
              newEvens = newSet(PostfixExpression.create(exp1, exp2, op), evens, exp1, exp2);
            }
            case DIV -> {
              // E / E
              if (exp2.expressionResult() == 0
                  || exp1.expressionResult() % exp2.expressionResult() != 0) {
                continue;
              }
              if (exp1.expressionResult() % 4 == 0) {
                newOdds = odds;
                newEvens = newSet(PostfixExpression.create(exp1, exp2, op), evens, exp1, exp2);
              } else {
                newOdds = newSet(PostfixExpression.create(exp1, exp2, op), odds);
                newEvens = newSet(evens, exp1, exp2);
              }
            }
            default -> throw new IllegalStateException(op + " is not a valid operator.");
          }
          result = calculateHelper(valueSets, newOdds, newEvens, targetSet);
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
