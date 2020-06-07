package sg.common;

import com.google.common.collect.ImmutableSet;
import java.util.Set;

public class Operators {
  public static final char ADD = '+';
  public static final char SUB = '-';
  public static final char MULT = '*';
  public static final char DIV = '/';

  public static final Set<Character> OPS = ImmutableSet.of(ADD, SUB, MULT, DIV);
  public static final Set<Character> ADD_SUB = ImmutableSet.of(ADD, SUB);
  public static final Set<Character> ADD_SUB_MULT = ImmutableSet.of(ADD, SUB, MULT);
  public static final Set<Character> MULT_DIV = ImmutableSet.of(MULT, DIV);
}
