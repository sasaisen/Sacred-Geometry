package sg.common;

import com.google.common.collect.ImmutableSet;
import java.util.Set;

public enum Targets {
  ZERO(ImmutableSet.of(0L)),
  ONE(ImmutableSet.of(3L, 5L, 7L)),
  TWO(ImmutableSet.of(11L, 13L, 17L)),
  THREE(ImmutableSet.of(19L, 23L, 29L)),
  FOUR(ImmutableSet.of(31L, 37L, 41L)),
  FIVE(ImmutableSet.of(43L, 47L, 53L)),
  SIX(ImmutableSet.of(59L, 61L, 67L)),
  SEVEN(ImmutableSet.of(71L, 73L, 79L)),
  EIGHT(ImmutableSet.of(83L, 89L, 97L)),
  NINE(ImmutableSet.of(101L, 103L, 107L));

  private final Set<Long> targetValues;

  Targets(Set<Long> targetValues) {
    this.targetValues = targetValues;
  }

  public Set<Long> targetValues() { return targetValues; }

}
