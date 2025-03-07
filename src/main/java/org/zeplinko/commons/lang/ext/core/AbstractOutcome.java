package org.zeplinko.commons.lang.ext.core;

import jakarta.annotation.Nonnull;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("LombokGetterMayBeUsed")
public abstract class AbstractOutcome<D, E> {
    private final D data;

    private final E error;

    protected AbstractOutcome(D data, E error) {
        this.data = data;
        this.error = error;
    }

    /**
     * Returns the data of the operation if it succeeded; otherwise, returns the
     * null value.
     *
     * @return The success data, or null if the AbstractOutcome is a failure.
     */
    public D getData() {
        return data;
    }

    /**
     * Return the error object of the operation if it failed; otherwise, returns the
     * null value.
     *
     * @return The error object, or null if the AbstractOutcome is successful.
     */
    public E getError() {
        return error;
    }

    /**
     * Checks if the AbstractOutcome represents success.
     *
     * @return {@code true} if the AbstractOutcome is successful, {@code false}
     *         otherwise.
     */
    public boolean isSuccess() {
        return !isFailure();
    }

    /**
     * Checks if the AbstractOutcome represents failure.
     *
     * @return {@code true} if the AbstractOutcome is a failure, {@code false}
     *         otherwise.
     */
    public abstract boolean isFailure();

    /**
     * Returns the data if the AbstractOutcome is successful; otherwise, returns the
     * other specified value.
     *
     * @param other The value to return if the AbstractOutcome is a failure.
     * @return The success data, or {@code other} if the AbstractOutcome is a
     *         failure.
     */
    public D orElse(D other) {
        return this.isFailure() ? other : this.getData();
    }

    /**
     * Returns the data if the AbstractOutcome is successful; otherwise, applies the
     * provided supplier to get the other value.
     *
     * @param otherSupplier The supplier function to provide the other value if the
     *                      AbstractOutcome is a failure.
     * @return The success data, or the value provided by {@code otherSupplier} if
     *         the AbstractOutcome is a failure.
     */
    public D orElseGet(@Nonnull Supplier<? extends D> otherSupplier) {
        Objects.requireNonNull(otherSupplier);
        return this.isFailure() ? otherSupplier.get() : this.getData();
    }

    /**
     * Returns the data if the AbstractOutcome is successful; otherwise, applies the
     * provided function to the error and returns the AbstractOutcome.
     *
     * @param otherFunction The function to apply to the error if the
     *                      AbstractOutcome is a failure.
     * @return The success data, or the AbstractOutcome of applying
     *         {@code otherFunction} to the error.
     */
    public D orElseGet(@Nonnull Function<? super E, ? extends D> otherFunction) {
        Objects.requireNonNull(otherFunction);
        return this.isFailure() ? otherFunction.apply(this.getError()) : this.getData();
    }

    /**
     * Returns the data if the AbstractOutcome is successful; otherwise, throws the
     * exception provided by the function applied to the error.
     *
     * @param exceptionFunction The function to apply to the error to generate an
     *                          exception.
     * @param <X>               The type of the exception to be thrown if the
     *                          AbstractOutcome is a failure.
     * @return The success data.
     * @throws X if the AbstractOutcome is a failure, the exception generated by
     *           applying {@code exceptionFunction} to the error.
     */
    public <X extends Throwable> D orElseThrow(@Nonnull Function<? super E, ? extends X> exceptionFunction) throws X {
        Objects.requireNonNull(exceptionFunction);
        if (this.isFailure()) {
            throw exceptionFunction.apply(this.getError());
        }
        return this.getData();
    }

    /**
     * Returns the Optional with value if the AbstractOutcome is successful;
     * otherwise, returns the Empty Optional.
     *
     * @return Optional with value if the AbstractOutcome is successful, otherwise
     *         Empty Optional
     */
    public Optional<D> toOptional() {
        return isFailure() ? Optional.empty() : Optional.ofNullable(this.getData());
    }
}
