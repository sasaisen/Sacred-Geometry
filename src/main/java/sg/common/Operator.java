package sg.common;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;

public enum Operator {
  ADD('+'),
  SUB('-'),
  MULT('*'),
  DIV('/');

  public static final Set<Operator> OPS = ImmutableSet.of(ADD, DIV, MULT, SUB);
  public static final Set<Operator> ADD_SUB = ImmutableSet.of(ADD, SUB);
  public static final Set<Operator> MULT_DIV = ImmutableSet.of(MULT, DIV);

  private final char character;
  static final Map<Character, Operator> valueMap;

  static {
    ImmutableMap.Builder<Character, Operator> builder = ImmutableMap.builder();
    for (Operator value : values()) {
      builder.put(value.character(), value);
    }
    valueMap = builder.build();
  }

  Operator(char character) {
    this.character = character;
  }

  char character() {
    return character;
  }

  static Operator fromCharacter(char c) {
    return valueMap.get(c);
  }
}
