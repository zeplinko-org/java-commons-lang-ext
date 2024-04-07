package org.zeplinko.commons.lang.ext.core;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.function.Function;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Result<S, T> {
    private final S data;

    private final T error;

    public static <S, T> Result<S, T> ok(S data) {
        return Result.<S, T>builder().data(data).build();
    }

    public static <S, T> Result<S, T> error(T error) {
        return Result.<S, T>builder().error(error).build();
    }

    public boolean isSuccess() {
        return !isFailure();
    }

    public boolean isFailure() {
        return this.getError() != null;
    }

    public S orElse(S other) {
        return this.isFailure() ? other : this.getData();
    }

    public S orElseGet(Function<T, ? extends S> otherFunction) {
        return this.isFailure() ? otherFunction.apply(this.getError()) : this.getData();
    }

    public <X extends Throwable> S orElseThrow(Function<T, ? extends X> exceptionFunction) throws X {
        if (this.isFailure()) {
            throw exceptionFunction.apply(this.getError());
        }
        return this.getData();
    }
}
