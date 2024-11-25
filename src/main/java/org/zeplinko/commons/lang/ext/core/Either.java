package org.zeplinko.commons.lang.ext.core;

import java.util.function.Function;

/**
 * Represents a value of one of two possible types (a disjoint union). An
 * {@code Either} can contain a value of type {@code L} (left) or type {@code R}
 * (right), but not both simultaneously.
 *
 * @param <L> The type of the left value.
 * @param <R> The type of the right value.
 */
@SuppressWarnings("LombokGetterMayBeUsed")
public class Either<L, R> {

    private final Container<L> left;

    private final Container<R> right;

    private Either(Container<L> left, Container<R> right) {
        this.left = left;
        this.right = right;
    }

    /**
     * Creates an {@code Either} instance containing a left value.
     *
     * @param left The value to store in the left.
     * @param <L>  The type of the left value.
     * @param <R>  The type of the right value.
     * @return An {@code Either} containing the left value.
     */
    public static <L, R> Either<L, R> left(L left) {
        Container<L> leftContainer = new Container<>(left);
        return new Either<>(leftContainer, null);
    }

    /**
     * Creates an {@code Either} instance containing a right value.
     *
     * @param right The value to store in the right.
     * @param <L>   The type of the left value.
     * @param <R>   The type of the right value.
     * @return An {@code Either} containing the right value.
     */
    public static <L, R> Either<L, R> right(R right) {
        Container<R> rightContainer = new Container<>(right);
        return new Either<>(null, rightContainer);
    }

    /**
     * Retrieves the left value if present.
     *
     * @return The left value if present, or {@code null} if absent.
     */
    public L getLeft() {
        return Nullable.of(this.left).map(Container::getValue).orElse(null);
    }

    /**
     * Retrieves the right value if present.
     *
     * @return The right value if present, or {@code null} if absent.
     */
    public R getRight() {
        return Nullable.of(this.right).map(Container::getValue).orElse(null);
    }

    /**
     * Checks if the {@code Either} contains a left value.
     *
     * @return {@code true} if the {@code Either} contains a left value,
     *         {@code false} otherwise.
     */
    public boolean isLeft() {
        return left != null;
    }

    /**
     * Checks if the {@code Either} contains a right value.
     *
     * @return {@code true} if the {@code Either} contains a right value,
     *         {@code false} otherwise.
     */
    public boolean isRight() {
        return !isLeft();
    }

    /**
     * Swaps the left and right values.
     *
     * @return A new {@code Either} with the left and right values swapped.
     */
    public Either<R, L> swap() {
        return isLeft() ? Either.right(getLeft()) : Either.left(getRight());
    }

    /**
     * Transforms the values in the {@code Either} using the provided mapping
     * functions.
     *
     * @param leftMapper  Function to transform the left value if present.
     * @param rightMapper Function to transform the right value if present.
     * @param <T>         The type of the transformed left value.
     * @param <U>         The type of the transformed right value.
     * @return A new {@code Either} containing the transformed value.
     */
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

    /**
     * Applies the provided mapping functions to flatten and transform the values in
     * the {@code Either}.
     *
     * @param leftMapper  Function to transform the left value into another
     *                    {@code Either}.
     * @param rightMapper Function to transform the right value into another
     *                    {@code Either}.
     * @param <T>         The type of the flattened left value.
     * @param <U>         The type of the flattened right value.
     * @return A flattened {@code Either} containing the transformed value.
     */
    public <T, U> Either<T, U> flatMap(
            Function<? super L, ? extends Either<T, U>> leftMapper,
            Function<? super R, ? extends Either<T, U>> rightMapper
    ) {
        if (isLeft()) {
            return leftMapper.apply(getLeft());
        } else {
            return rightMapper.apply(getRight());
        }
    }

}
