package com.xd.oktopus;


import java.util.Objects;

class Pair<L, R> {

    private final L left;
    private final R right;

    private Pair(final L left, final R right) {
        this.left = left;
        this.right = right;
    }

    static <L, R> Pair<L, R> of(final L left, final R right) {
        return new Pair<>(left, right);
    }

    L getLeft() {
        return left;
    }

    R getRight() {
        return right;
    }

    L getKey() {
        return getLeft();
    }

    R getValue() {
        return getRight();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(left, pair.left) && Objects.equals(right, pair.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }

}
