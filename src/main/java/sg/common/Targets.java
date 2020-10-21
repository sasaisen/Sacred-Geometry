package sg.common;

import com.google.common.collect.ImmutableSet;
import java.util.Set;

public enum Targets {
  ZERO(ImmutableSet.of(0L), FileUtil.SG_0),
  ONE(ImmutableSet.of(3L, 5L, 7L), FileUtil.SG_1),
  TWO(ImmutableSet.of(11L, 13L, 17L), FileUtil.SG_2),
  THREE(ImmutableSet.of(19L, 23L, 29L), FileUtil.SG_3),
  FOUR(ImmutableSet.of(31L, 37L, 41L), FileUtil.SG_4),
  FIVE(ImmutableSet.of(43L, 47L, 53L), FileUtil.SG_5),
  SIX(ImmutableSet.of(59L, 61L, 67L), FileUtil.SG_6),
  SEVEN(ImmutableSet.of(71L, 73L, 79L), FileUtil.SG_7),
  EIGHT(ImmutableSet.of(83L, 89L, 97L), FileUtil.SG_8),
  NINE(ImmutableSet.of(101L, 103L, 107L), FileUtil.SG_9);

  private final Set<Long> targetValues;
  private final String fileName;

  Targets(Set<Long> targetValues, String fileName) {
    this.targetValues = targetValues;
    this.fileName = fileName;
  }

  public Set<Long> targetValues() {
    return targetValues;
  }

  public String fileName() {
    return fileName;
  }
}
