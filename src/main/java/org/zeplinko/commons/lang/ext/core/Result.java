package org.zeplinko.commons.lang.ext.core;

import jakarta.annotation.Nonnull;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A generic class for wrapping the result of an operation that can either
 * succeed or fail. This class holds either data (on success) or an error object
 * (on failure), but not both. Use static factory methods {@code ok} and
 * {@code error} to create instances of this class.
 *
 * @param <D> the type of the data returned on success
 * @param <E> the type of the error object returned on failure
 */
@SuppressWarnings("LombokGetterMayBeUsed")
public class Result<D, E> extends AbstractResult<D, E> {

    private Result(D data, E error) {
        super(data, error);
    }

    /**
     * Creates a successful result containing the provided data.
     *
     * @deprecated Instead use
     *             {@code org.zeplinko.commons.lang.ext.core.Result#success(java.lang.Object)}
     *
     * @param data The data to be contained in the result.
     * @param <D>  The type of the success data.
     * @param <E>  The type of the failure error object.
     * @return A {@code Result} instance representing success.
     */
    @Deprecated
    public static <D, E> Result<D, E> ok(D data) {
        return success(data);
    }

    /**
     * Creates a successful result containing the provided data.
     *
     * @param data The data to be contained in the result.
     * @param <D>  The type of the success data.
     * @param <E>  The type of the failure error object.
     * @return A {@code Result} instance representing success.
     */
    public static <D, E> Result<D, E> success(D data) {
        return new Result<>(data, null);
    }

    /**
     * Creates a failed result containing the provided error.
     *
     * @deprecated Instead use
     *             {@code org.zeplinko.commons.lang.ext.core.Result#failure(java.lang.Object)}
     *
     * @param error The error object to be contained in the result.
     * @param <D>   The type of the success data.
     * @param <E>   The type of the failure error object.
     * @return A {@code Result} instance representing failure.
     */
    @Deprecated
    public static <D, E> Result<D, E> error(@Nonnull E error) {
        return failure(error);
    }

    /**
     * Creates a failed result containing the provided error.
     *
     * @param error The error object to be contained in the result.
     * @param <D>   The type of the success data.
     * @param <E>   The type of the failure error object.
     * @return A {@code Result} instance representing failure.
     */
    public static <D, E> Result<D, E> failure(@Nonnull E error) {
        Objects.requireNonNull(error);
        return new Result<>(null, error);
    }

    @Override
    public boolean isFailure() {
        return this.getError() != null;
    }

    /**
     * Maps the successful result to a new result using the provided value.
     *
     * @deprecated Promotes wrong usage where the programmers might call this
     *             overload passing the method call as argument. Instead, use the
     *             overload
     *             {@code org.zeplinko.commons.lang.ext.core.Result#map(java.util.function.Function)}
     *             which takes the Function lambda as the argument.
     *
     * @param value The value to map to the new result.
     * @param <U>   The type of the new result's success data.
     * @return A new {@code Result} instance.
     */
    @Deprecated
    public <U> Result<U, E> map(U value) {
        return this.isFailure() ? Result.error(this.getError()) : Result.ok(value);
    }

    /**
     * Maps the successful result to a new result using the provided function.
     *
     * @param mapper The function to apply to the success data.
     * @param <U>    The type of the new result's success data.
     * @return A new {@code Result} instance.
     */

    public <U> Result<U, E> map(@Nonnull Function<? super D, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        return this.isFailure() ? Result.failure(this.getError()) : Result.success(mapper.apply(this.getData()));
    }

    /**
     * Returns a new result with the specified value if the result is a failure.
     *
     * @deprecated Promotes wrong usage where the programmers might call this
     *             overload passing the method call as argument. Instead, use the
     *             overload
     *             {@code org.zeplinko.commons.lang.ext.core.Result#otherwise(java.util.function.Function)}
     *             which takes the Function lambda as the argument.
     *
     * @param value The value to be contained in the new result if the current
     *              result is a failure.
     * @return A new {@code Result} instance.
     */
    @Deprecated
    public Result<D, E> otherwise(D value) {
        return this.isFailure() ? Result.ok(value) : this;
    }

    /**
     * Returns a new result by applying the provided function to the error if the
     * result is a failure.
     *
     * @param mapper The function to apply to the error if the result is a failure.
     * @return A new {@code Result} instance.
     */
    public Result<D, E> otherwise(@Nonnull Function<? super E, ? extends D> mapper) {
        Objects.requireNonNull(mapper);
        return this.isFailure() ? Result.success(mapper.apply(this.getError())) : this;
    }

    /**
     * Composes a new result by applying the provided functions to the success data
     * or error.
     *
     * @param successMapper The function to apply to the success data.
     * @param failureMapper The function to apply to the error.
     * @param <U>           The type of the new result's success data.
     * @param <V>           The type of the new result's error object.
     * @return A new {@code Result} instance.
     */
    public <U, V> Result<U, V> compose(
            @Nonnull Function<? super D, ? extends Result<U, V>> successMapper,
            @Nonnull Function<? super E, ? extends Result<U, V>> failureMapper
    ) {
        Objects.requireNonNull(successMapper);
        Objects.requireNonNull(failureMapper);
        return this.isFailure() ? failureMapper.apply(this.getError()) : successMapper.apply(this.getData());
    }

    /**
     * Flat maps the result to a new result using the provided function.
     *
     * @param mapper The function to apply to the success data.
     * @param <U>    The type of the new result's success data.
     * @return A new {@code Result} instance.
     */
    public <U> Result<U, E> flatMap(@Nonnull Function<? super D, ? extends Result<U, E>> mapper) {
        return compose(mapper, Result::failure);
    }

    /**
     * Recovers from an error by applying the provided function to the error to get
     * a new result.
     *
     * @param mapper The function to apply to the error.
     * @param <V>    The type of the new result's error object.
     * @return A new {@code Result} instance.
     */
    public <V> Result<D, V> recover(@Nonnull Function<? super E, ? extends Result<D, V>> mapper) {
        return compose(Result::success, mapper);
    }

    /**
     * Transforms the result to a new result using the provided function.
     *
     * @param mapper The function to apply to the result.
     * @param <U>    The type of the new result's success data.
     * @param <V>    The type of the new result's error object.
     * @return A new {@code Result} instance.
     */
    public <U, V> Result<U, V> transform(@Nonnull Function<? super Result<D, E>, ? extends Result<U, V>> mapper) {
        Objects.requireNonNull(mapper);
        return mapper.apply(this);
    }

    /**
     * Taps the success data or error, applying the provided consumers without
     * modifying the result.
     *
     * @deprecated Instead use
     *             {@code org.zeplinko.commons.lang.ext.core.Result#onResult(java.util.function.Consumer, java.util.function.Consumer)}
     *
     * @param successHandler The consumer to apply to the success data.
     * @param failureHandler The consumer to apply to the error.
     * @return The current {@code Result} instance.
     */
    @Deprecated
    public Result<D, E> handle(Consumer<? super D> successHandler, Consumer<? super E> failureHandler) {
        return onResult(successHandler, failureHandler);
    }

    /**
     * Taps the success data or error, applying the provided consumers without
     * modifying the result.
     *
     * @param dataConsumer  The consumer to apply to the success data.
     * @param errorConsumer The consumer to apply to the error.
     * @return The current {@code Result} instance.
     */
    public Result<D, E> onResult(Consumer<? super D> dataConsumer, Consumer<? super E> errorConsumer) {
        if (isFailure()) {
            Optional.ofNullable(errorConsumer).ifPresent(handler -> handler.accept(this.getError()));
        } else {
            Optional.ofNullable(dataConsumer).ifPresent(handler -> handler.accept(this.getData()));
        }
        return this;
    }

    /**
     * Taps the result, applying the provided consumer without modifying the result.
     *
     * @deprecated Instead use
     *             {@code org.zeplinko.commons.lang.ext.core.Result#onResult(java.util.function.Consumer)}
     *
     * @param resultHandler The consumer to apply to the result.
     * @return The current {@code Result} instance.
     */
    @Deprecated
    public Result<D, E> handleResult(Consumer<? super Result<D, E>> resultHandler) {
        return onResult(resultHandler);
    }

    /**
     * Taps the result, applying the provided consumer without modifying the result.
     *
     * @param resultConsumer The consumer to apply to the result.
     * @return The current {@code Result} instance.
     */
    public Result<D, E> onResult(Consumer<? super Result<D, E>> resultConsumer) {
        Optional.ofNullable(resultConsumer).ifPresent(it -> it.accept(this));
        return this;
    }

    /**
     * Taps the success data, applying the provided consumer without modifying the
     * result.
     *
     * @deprecated Instead use
     *             {@code org.zeplinko.commons.lang.ext.core.Result#onSuccess(java.util.function.Consumer)}
     *
     * @param successHandler The consumer to apply to the success data.
     * @return The current {@code Result} instance.
     */
    @Deprecated
    public Result<D, E> handleSuccess(Consumer<? super D> successHandler) {
        return onSuccess(successHandler);
    }

    /**
     * Taps the success data, applying the provided consumer without modifying the
     * result.
     *
     * @param dataConsumer The consumer to apply to the success data.
     * @return The current {@code Result} instance.
     */
    public Result<D, E> onSuccess(Consumer<? super D> dataConsumer) {
        return onResult(dataConsumer, null);
    }

    /**
     * Taps the error, applying the provided consumer without modifying the result.
     *
     * @deprecated Instead use
     *             {@code org.zeplinko.commons.lang.ext.core.Result#onFailure(java.util.function.Consumer)}
     *
     * @param failureHandler The consumer to apply to the error.
     * @return The current {@code Result} instance.
     */
    @Deprecated
    public Result<D, E> handleFailure(Consumer<? super E> failureHandler) {
        return onFailure(failureHandler);
    }

    /**
     * Taps the error, applying the provided consumer without modifying the result.
     *
     * @param errorConsumer The consumer to apply to the error.
     * @return The current {@code Result} instance.
     */
    public Result<D, E> onFailure(Consumer<? super E> errorConsumer) {
        return onResult(null, errorConsumer);
    }
}
