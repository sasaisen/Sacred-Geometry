package sg.common;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;

public enum Operators {
  ADD('+'),
  SUB('-'),
  MULT('*'),
  DIV('/');

  public static final Set<Operators> OPS = ImmutableSet.of(ADD, SUB, MULT, DIV);
  public static final Set<Operators> ADD_SUB = ImmutableSet.of(ADD, SUB);
  public static final Set<Operators> ADD_SUB_MULT = ImmutableSet.of(ADD, SUB, MULT);
  public static final Set<Operators> MULT_DIV = ImmutableSet.of(MULT, DIV);

  private final char character;
  static final Map<Character, Operators> valueMap;

  static {
    ImmutableMap.Builder<Character, Operators> builder = ImmutableMap.builder();
    for (Operators value : values()) {
      builder.put(value.character(), value);
    }
    valueMap = builder.build();
  }

  Operators(char character) {
    this.character = character;
  }

  public char character() {
    return character;
  }

  public static Operators fromCharacter(char c) {
    return valueMap.get(c);
  }
}
