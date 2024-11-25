package org.zeplinko.commons.lang.ext.core;

import java.util.function.Function;

@SuppressWarnings("LombokGetterMayBeUsed")
public class Either<L, R> {

    private final Container<L> left;

    private final Container<R> right;

    private Either(Container<L> left, Container<R> right) {
        this.left = left;
        this.right = right;
    }

    public static <L, R> Either<L, R> left(L left) {
        Container<L> leftContainer = new Container<>(left);
        return new Either<>(leftContainer, null);
    }

    public static <L, R> Either<L, R> right(R right) {
        Container<R> rightContainer = new Container<>(right);
        return new Either<>(null, rightContainer);
    }

    public L getLeft() {
        return this.left.getValue();
    }

    public R getRight() {
        return this.right.getValue();
    }

    public boolean isLeft() {
        return left != null;
    }

    public boolean isRight() {
        return !isLeft();
    }

    public Either<R, L> swap() {
        return isLeft() ? Either.right(this.left.getValue()) : Either.left(this.right.getValue());
    }

    public <T, U> Either<T, U> map(
            Function<? super L, ? extends T> leftMapper,
            Function<? super R, ? extends U> rightMapper
    ) {
        if (isLeft()) {
            return Either.left(leftMapper.apply(getLeft()));
        } else {
            return Either.right(rightMapper.apply(getRight()));
        }
    }

    public <T, U> Either<T, U> flatMap(
            Function<? super L, Either<T, U>> leftMapper,
            Function<? super R, Either<T, U>> rightMapper
    ) {
        if (isLeft()) {
            return leftMapper.apply(getLeft());
        } else {
            return rightMapper.apply(getRight());
        }
    }

}
