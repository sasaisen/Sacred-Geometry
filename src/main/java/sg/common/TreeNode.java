package sg.common;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class TreeNode<E> {

  private final E value;
  private final TreeNode<E> left;
  private final TreeNode<E> right;

  TreeNode(@Nonnull E value, @Nullable TreeNode<E> left, @Nullable TreeNode<E> right) {
    this.value = value;
    this.left = left;
    this.right = right;
  }

  int size() {
    return 1 + (left != null ? left.size() : 0) + (right != null ? right.size() : 0);
  }

  E value() {
    return value;
  }

  TreeNode<E> left() {
    return left;
  }

  TreeNode<E> right() {
    return right;
  }
}