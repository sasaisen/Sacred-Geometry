package sg.common;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;

public class SetUtil {

  static <E> Multiset<E> difference(Multiset<E> left, Multiset<E> right) {
    ImmutableMultiset.Builder<E> builder = ImmutableMultiset.builder();
    for (E e : left.elementSet()) {
      builder.addCopies(e, left.count(e) - right.count(e));
    }
    return builder.build();
  }

  static <E> boolean isSupersetOf(Multiset<E> left, Multiset<E> right) {
    for (E e : right.elementSet()) {
      if (left.count(e) < right.count(e)) {
        return false;
      }
    }
    return true;
  }

  @SafeVarargs
  static <E> Multiset<E> newSet(Multiset<E> set, E... valuesToRemove) {
    ImmutableMultiset.Builder<E> builder = ImmutableMultiset.builder();
    Multiset<E> removeSet = ImmutableMultiset.copyOf(valuesToRemove);

    for (E e : set.elementSet()) {
      builder.addCopies(e, set.count(e) - removeSet.count(e));
    }
    return builder.build();
  }

  @SafeVarargs
  static <E> Multiset<E> newSet(E valueToAdd, Multiset<E> set, E... valuesToRemove) {
    ImmutableMultiset.Builder<E> builder = ImmutableMultiset.builder();
    Multiset<E> removeSet = ImmutableMultiset.copyOf(valuesToRemove);

    for (E e : set.elementSet()) {
      builder.addCopies(e, set.count(e) - removeSet.count(e));
    }
    return builder.add(valueToAdd).build();
  }
}
