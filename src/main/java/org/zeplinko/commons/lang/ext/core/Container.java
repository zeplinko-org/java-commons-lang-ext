package org.zeplinko.commons.lang.ext.core;

/**
 * A simple container class that holds a single immutable value.
 *
 * @author Astha&nbsp;Singh
 *
 * @param <T> The type of the value stored in the container.
 */
@SuppressWarnings("LombokGetterMayBeUsed")
public class Container<T> {

    /**
     * The stored value of type {@code T}.
     */
    protected T value;

    /**
     * Constructs a new {@code Container} with the specified value.
     *
     * @param value The value to be stored in the container. Can be {@code null}.
     */
    public Container(T value) {
        this.value = value;
    }

    /**
     * Retrieves the value stored in this container.
     *
     * @return The stored value of type {@code T}, or {@code null} if no value is
     *         present.
     */
    public T getValue() {
        return this.value;
    }

    /**
     * Static factory method to create a {@code Container} instance.
     *
     * @param value The value to be stored in the container.
     * @param <T>   The type of the value.
     * @return A new {@code Container} containing the given value.
     */
    public static <T> Container<T> of(T value) {
        return new Container<>(value);
    }
}
