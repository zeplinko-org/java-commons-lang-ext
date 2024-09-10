package org.zeplinko.commons.lang.ext.core;

import jakarta.annotation.Nonnull;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;

public class Try<T> extends AbstractResult<T, Exception> {

    private Try(T data, Exception error) {
        super(data, error);
    }

    public static <T> Try<T> success(T data) {
        return new Try<>(data, null);
    }

    public static <T> Try<T> failure(@Nonnull Exception error) {
        return new Try<>(null, error);
    }

    public static <T> Try<T> call(Callable<T> callable) {
        try {
            T result = callable.call();
            return Try.success(result);
        } catch (Exception e) {
            return Try.failure(e);
        }
    }

    public static <T> Try<T> join(Future<T> future) {
        try {
            T result = future.get();
            return Try.success(result);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Try.failure(e);
        } catch (Exception e) {
            return Try.failure(e);
        }
    }

    @Override
    public boolean isFailure() {
        return this.getError() != null;
    }

    public <U> Try<U> map(@Nonnull Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        return this.isFailure() ? Try.failure(this.getError()) : Try.success(mapper.apply(this.getData()));
    }

    public Try<T> otherwise(@Nonnull Function<? super Exception, ? extends T> mapper) {
        Objects.requireNonNull(mapper);
        return this.isFailure() ? Try.success(mapper.apply(this.getError())) : this;
    }

    public <U> Try<U> compose(
            @Nonnull Function<? super T, ? extends Try<U>> successMapper,
            @Nonnull Function<? super Exception, ? extends Try<U>> failureMapper
    ) {
        Objects.requireNonNull(successMapper);
        Objects.requireNonNull(failureMapper);
        return this.isFailure() ? failureMapper.apply(this.getError()) : successMapper.apply(this.getData());
    }

    public <U> Try<U> flatMap(@Nonnull Function<? super T, ? extends Try<U>> mapper) {
        return compose(mapper, Try::failure);
    }

    public Try<T> recover(@Nonnull Function<? super Exception, ? extends Try<T>> mapper) {
        return compose(Try::success, mapper);
    }

    public <U> Try<U> transform(@Nonnull Function<? super Try<T>, ? extends Try<U>> mapper) {
        Objects.requireNonNull(mapper);
        return mapper.apply(this);
    }

    public Try<T> onResult(Consumer<? super T> dataConsumer, Consumer<? super Exception> errorConsumer) {
        if (isFailure()) {
            Optional.ofNullable(errorConsumer).ifPresent(handler -> handler.accept(this.getError()));
        } else {
            Optional.ofNullable(dataConsumer).ifPresent(handler -> handler.accept(this.getData()));
        }
        return this;
    }

    public Try<T> onResult(Consumer<? super Try<T>> tryConsumer) {
        Optional.ofNullable(tryConsumer).ifPresent(handler -> handler.accept(this));
        return this;
    }

    public Try<T> onSuccess(Consumer<? super T> dataConsumer) {
        return onResult(dataConsumer, null);
    }

    public Try<T> onFailure(Consumer<? super Exception> errorConsumer) {
        return onResult(null, errorConsumer);
    }

    public Result<T, Exception> toResult() {
        return this.isFailure() ? Result.failure(this.getError()) : Result.success(this.getData());
    }
}
