package org.zeplinko.commons.lang.ext.core;

import jakarta.annotation.Nonnull;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

public class Try<T> extends AbstractResult<T, Exception> {

    private Try(T data, Exception error) {
        super(data, error);
    }

    /**
     * Creates a successful Try containing the provided data.
     *
     * @param data The data to be contained in the Try.
     * @param <T>  The type of the success data.
     * @return A {@code Try} instance representing success.
     */
    public static <T> Try<T> success(T data) {
        return new Try<>(data, null);
    }

    /**
     * Creates a failed Try containing the provided exception.
     *
     * @param exception The exception to be contained in the Try.
     * @param <T>       The type of the success data.
     * @return A {@code Try} instance representing failure.
     */
    public static <T> Try<T> failure(@Nonnull Exception exception) {
        Objects.requireNonNull(exception);
        return new Try<>(null, exception);
    }

    /**
     * Creates a successful Try if the callable doesn't throw any exception;
     * otherwise, creates a failure Try with the exception thrown.
     *
     * @param callable The Callable lambda to be called.
     * @param <T>      The type of the success data.
     * @return A successful {@code Try} instance containing the returned data if the
     *         callable doesn't throw any exception; otherwise, a failed {@code Try}
     *         instance with the exception thrown
     */
    public static <T> Try<T> to(@Nonnull Callable<T> callable) {
        Objects.requireNonNull(callable);
        try {
            T result = callable.call();
            return Try.success(result);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Try.failure(e);
        } catch (Exception e) {
            return Try.failure(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFailure() {
        return this.getError() != null;
    }

    /**
     * Maps the success value of this {@code Try} using the provided mapping
     * function. If this {@code Try} is a failure, the same failure is returned.
     *
     * @param mapper A function to transform the success value.
     * @param <U>    The type of the mapped success value.
     * @return A {@code Try} containing the mapped value if successful, or the same
     *         failure.
     */
    public <U> Try<U> map(@Nonnull Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        return this.isFailure() ? Try.failure(this.getError()) : Try.success(mapper.apply(this.getData()));
    }

    /**
     * Transforms the failure value of this {@code Try} into a success value using
     * the provided mapping function. If this {@code Try} is a success, it is
     * returned unchanged.
     *
     * @param mapper A function to transform the failure value into a success value.
     * @return A new {@code Try} with the transformed value if this was a failure,
     *         or the same success.
     */
    public Try<T> otherwise(@Nonnull Function<? super Exception, ? extends T> mapper) {
        Objects.requireNonNull(mapper);
        return this.isFailure() ? Try.success(mapper.apply(this.getError())) : this;
    }

    /**
     * Composes this {@code Try} into a new {@code Try} using the provided functions
     * for success and failure cases.
     *
     * @param successMapper Function to transform the success value.
     * @param failureMapper Function to transform the failure value.
     * @param <U>           The type of the resulting {@code Try}.
     * @return A {@code Try} resulting from applying the appropriate mapper.
     */
    public <U> Try<U> compose(
            @Nonnull Function<? super T, ? extends Try<U>> successMapper,
            @Nonnull Function<? super Exception, ? extends Try<U>> failureMapper
    ) {
        Objects.requireNonNull(successMapper);
        Objects.requireNonNull(failureMapper);
        return this.isFailure() ? failureMapper.apply(this.getError()) : successMapper.apply(this.getData());
    }

    /**
     * Flattens and maps the success value of this {@code Try} into another
     * {@code Try} using the provided mapping function. If this {@code Try} is a
     * failure, the same failure is returned.
     *
     * @param mapper A function to map the success value to a new {@code Try}.
     * @param <U>    The type of the resulting success value.
     * @return A {@code Try} resulting from the mapping function if successful, or
     *         the same failure.
     */
    public <U> Try<U> flatMap(@Nonnull Function<? super T, ? extends Try<U>> mapper) {
        return compose(mapper, Try::failure);
    }

    /**
     * Recovers from a failure by transforming it another {@code Try} using the
     * provided mapping function. If this {@code Try} is a success, it is returned
     * unchanged.
     *
     * @param mapper A function to transform the failure into a success {@code Try}.
     * @return A {@code Try} resulting from the recovery function if this was a
     *         failure, or the same success.
     */
    public Try<T> recover(@Nonnull Function<? super Exception, ? extends Try<T>> mapper) {
        return compose(Try::success, mapper);
    }

    /**
     * Transforms this {@code Try} into another {@code Try} using the provided
     * transformation function.
     *
     * @param mapper A function to transform this {@code Try}.
     * @param <U>    The type of the resulting {@code Try}.
     * @return A {@code Try} resulting from the transformation function.
     */
    public <U> Try<U> transform(@Nonnull Function<? super Try<T>, ? extends Try<U>> mapper) {
        Objects.requireNonNull(mapper);
        return mapper.apply(this);
    }

    /**
     * Handles the success or failure values of this {@code Try} using the provided
     * consumers.
     *
     * @param dataConsumer  Consumer to handle the success value.
     * @param errorConsumer Consumer to handle the failure value.
     * @return This {@code Try} instance.
     */
    public Try<T> onHandle(Consumer<? super T> dataConsumer, Consumer<? super Exception> errorConsumer) {
        if (isFailure()) {
            Optional.ofNullable(errorConsumer).ifPresent(handler -> handler.accept(this.getError()));
        } else {
            Optional.ofNullable(dataConsumer).ifPresent(handler -> handler.accept(this.getData()));
        }
        return this;
    }

    /**
     * Handles this {@code Try} using a single consumer that receives the entire
     * {@code Try}.
     *
     * @param tryConsumer Consumer to handle this {@code Try}.
     * @return This {@code Try} instance.
     */
    public Try<T> onHandle(Consumer<? super Try<T>> tryConsumer) {
        Optional.ofNullable(tryConsumer).ifPresent(handler -> handler.accept(this));
        return this;
    }

    /**
     * Executes the provided consumer if this {@code Try} is successful.
     *
     * @param dataConsumer Consumer to handle the success value.
     * @return This {@code Try} instance.
     */
    public Try<T> onSuccess(Consumer<? super T> dataConsumer) {
        return onHandle(dataConsumer, null);
    }

    /**
     * Executes the provided consumer if this {@code Try} is a failure.
     *
     * @param errorConsumer Consumer to handle the failure value.
     * @return This {@code Try} instance.
     */
    public Try<T> onFailure(Consumer<? super Exception> errorConsumer) {
        return onHandle(null, errorConsumer);
    }

    /**
     * Converts this {@code Try} into a {@code Result} object.
     *
     * @return A {@code Result} representing the same success or failure as this
     *         {@code Try}.
     */
    public Result<T, Exception> toResult() {
        return this.isFailure() ? Result.failure(this.getError()) : Result.success(this.getData());
    }
}
