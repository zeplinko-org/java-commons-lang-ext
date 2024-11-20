package org.zeplinko.commons.lang.ext.core;

import jakarta.annotation.Nonnull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.Objects;
import java.util.function.Function;

@SuppressWarnings("LombokGetterMayBeUsed")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Either<L, R> {

    private final L left;

    private final R right;

    public static <L, R> Either<L, R> left(@Nonnull L left) {
        Objects.requireNonNull(left);
        return new Either<>(left, null);
    }

    public static <L, R> Either<L, R> right(@Nonnull R right) {
        Objects.requireNonNull(right);
        return new Either<>(null, right);
    }

    public L getLeft() {
        return this.left;
    }

    public R getRight() {
        return this.right;
    }

    public boolean isLeft() {
        return left != null;
    }

    public boolean isRight() {
        return !isLeft();
    }

    public Either<R, L> swap() {
        return isLeft() ? Either.right(this.left) : Either.left(this.right);
    }

    public <T, U> Either<T, U> map(
            Function<? super L, ? extends T> leftMapper,
            Function<? super R, ? extends U> rightMapper
    ) {
        if (isLeft()) {
            return Either.left(leftMapper.apply(this.left));
        } else {
            return Either.right(rightMapper.apply(this.right));
        }
    }

    public <T, U> Either<T, U> flatMap(
            Function<? super L, Either<T, U>> leftMapper,
            Function<? super R, Either<T, U>> rightMapper
    ) {
        if (isLeft()) {
            return leftMapper.apply(this.left);
        } else {
            return rightMapper.apply(this.right);
        }
    }

}
