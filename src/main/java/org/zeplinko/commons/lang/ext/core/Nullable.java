package org.zeplinko.commons.lang.ext.core;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A container object which may or may not contain a non-null value. If a value
 * is present, {@code isNull()} will return {@code false} and {@code unwrap()}
 * will return the value.
 *
 * @param <T> the type of value
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Nullable<T> {
    private static final Nullable<?> EMPTY = new Nullable<>(null);

    private final T value;

    /**
     * Returns a {@code Nullable} with the specified value.
     *
     * @param value the value to wrap inside Nullable
     * @param <T>   the type of the value
     * @return a {@code Nullable} with the value present
     */
    public static <T> Nullable<T> of(T value) {
        return value == null ? Nullable.empty() : new Nullable<>(value);
    }

    /**
     * Returns an empty {@code Nullable} instance. No value is present for this
     * {@code Nullable}.
     *
     * @param <T> Type of the non-existent value
     * @return an empty {@code Nullable}
     */
    public static <T> Nullable<T> empty() {
        @SuppressWarnings("unchecked")
        Nullable<T> nullable = (Nullable<T>) EMPTY;
        return nullable;
    }

    /**
     * Returns {@code true} if there is no value present, otherwise {@code false}.
     *
     * @return {@code true} if there is no value present, otherwise {@code false}
     */
    public boolean isNull() {
        return value == null;
    }

    /**
     * Returns {@code true} if there is a value present, otherwise {@code false}.
     *
     * @return {@code true} if there is a value present, otherwise {@code false}
     */
    public boolean isNotNull() {
        return !isNull();
    }

    /**
     * If a value is present, invoke the specified consumer with the value,
     * otherwise do nothing.
     *
     * @param action the consumer to be executed if a value is present
     */
    public void ifPresent(Consumer<? super T> action) {
        if (this.isNull()) {
            return;
        }
        action.accept(value);
    }

    /**
     * If a value is present, invoke the specified consumer with the value,
     * otherwise invoke the specified runnable.
     *
     * @param action      the consumer to be executed if a value is present
     * @param emptyAction the runnable to be executed if no value is present
     */
    public void ifPresentOrElse(Consumer<? super T> action, Runnable emptyAction) {
        if (this.isNull()) {
            emptyAction.run();
        } else {
            action.accept(value);
        }
    }

    /**
     * If a value is present, and the value matches the given predicate, return a
     * {@code Nullable} describing the value, otherwise return an empty
     * {@code Nullable}.
     *
     * @param predicate the predicate to apply to the value, if present
     * @return a {@code Nullable} describing the value of this {@code Nullable} if a
     *         value is present and the value matches the given predicate, otherwise
     *         an empty {@code Nullable}
     */
    public Nullable<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        if (this.isNull()) {
            return this;
        }
        return predicate.test(value) ? this : Nullable.empty();
    }

    /**
     * If a value is present, apply the provided mapping function to it, and if the
     * result is non-null, return a {@code Nullable} describing the result.
     * Otherwise, return an empty {@code Nullable}.
     *
     * @param <U>    The type of the result of the mapping function
     * @param mapper a mapping function to apply to the value, if present
     * @return a {@code Nullable} describing the result of applying a mapping
     *         function to the value of this {@code Nullable}, if a value is
     *         present, otherwise an empty {@code Nullable}
     */
    public <U> Nullable<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        if (this.isNull()) {
            return Nullable.empty();
        }
        return Nullable.of(mapper.apply(value));
    }

    /**
     * If a value is present, apply the provided {@code Nullable}-bearing mapping
     * function to it, return that result, otherwise return an empty
     * {@code Nullable}.
     *
     * @param <U>    The type of value of the {@code Nullable} returned by the
     *               mapping function
     * @param mapper a mapping function to apply to the value, if present
     * @return the result of applying an {@code Nullable}-bearing mapping function
     *         to the value of this {@code Nullable}, if a value is present,
     *         otherwise an empty {@code Nullable}
     */
    public <U> Nullable<U> flatMap(Function<? super T, Nullable<? extends U>> mapper) {
        Objects.requireNonNull(mapper);
        if (isNull()) {
            return Nullable.empty();
        }
        @SuppressWarnings("unchecked")
        Nullable<U> nullable = (Nullable<U>) mapper.apply(value);
        return Objects.requireNonNull(nullable);
    }

    /**
     * Return the value if present, otherwise return an {@code Nullable} produced by
     * the supplying function.
     *
     * @param supplier the supplying function that produces an {@code Nullable} to
     *                 be returned
     * @return the value, if present, otherwise an {@code Nullable} produced by the
     *         supplying function
     */
    public Nullable<T> or(Supplier<Nullable<? extends T>> supplier) {
        Objects.requireNonNull(supplier);
        if (isNull()) {
            @SuppressWarnings("unchecked")
            Nullable<T> nullable = (Nullable<T>) supplier.get();
            return Objects.requireNonNull(nullable);
        }
        return this;
    }

    /**
     * If a value is present, returns a sequential {@code Stream} containing only
     * that value, otherwise returns an empty {@code Stream}.
     *
     * @return a {@code Stream} containing the value of this {@code Nullable} if a
     *         value is present, otherwise an empty {@code Stream}
     */
    public Stream<T> stream() {
        if (isNull()) {
            return Stream.empty();
        }
        return Stream.of(value);
    }

    /**
     * Return the value if present, otherwise return {@code other}.
     *
     * @param other the value to be returned if there is no value present, may be
     *              null
     * @return the value, if present, otherwise {@code other}
     */
    public T orElse(T other) {
        return this.isNull() ? other : value;
    }

    /**
     * Return the value if present, otherwise invoke {@code supplier} and return the
     * result of that invocation.
     *
     * @param supplier a {@code Supplier} whose result is returned if no value is
     *                 present
     * @return the value if present, otherwise the result of {@code supplier.get()}
     */
    public T orElseGet(Supplier<? extends T> supplier) {
        return this.isNull() ? supplier.get() : value;
    }

    /**
     * Return the contained value, if present, otherwise throw a
     * {@link NoSuchElementException}.
     *
     * @return the present value
     * @throws NoSuchElementException if there is no value present
     */
    public T orElseThrow() {
        if (this.isNull()) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    /**
     * Return the contained value, if present, otherwise throw an exception to be
     * created by the provided supplier.
     *
     * @param <X>               Type of the exception to be thrown
     * @param exceptionSupplier The supplier which will return the exception to be
     *                          thrown
     * @return the present value
     * @throws X if there is no value present
     */
    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        if (this.isNull()) {
            throw exceptionSupplier.get();
        }
        return value;
    }

    /**
     * Indicates whether some other object is "equal to" this {@code Nullable}.
     *
     * @param obj an object to be tested for equality
     * @return {@code true} if the other object is "equal to" this object, otherwise
     *         {@code false}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        return obj instanceof Nullable<?>
                && Objects.equals(value, ((Nullable<?>) obj).value);
    }

    /**
     * Returns the hash code value for this {@code Nullable}.
     *
     * @return the hash code value for this object
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    /**
     * Returns a non-empty string representation of this {@code Nullable} suitable
     * for debugging. The exact presentation format is unspecified and may vary
     * between implementations and versions.
     *
     * @return the string representation of this instance
     */
    @Override
    public String toString() {
        return this.isNull() ? "Nullable.empty" : ("Nullable[" + value + "]");
    }

}
