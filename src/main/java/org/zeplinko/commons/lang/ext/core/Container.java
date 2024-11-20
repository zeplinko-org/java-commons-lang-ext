package org.zeplinko.commons.lang.ext.core;

/**
 * A simple container class that holds a single immutable value.
 *
 * @param <T> The type of the value stored in the container.
 */
@SuppressWarnings("LombokGetterMayBeUsed")
public class Container<T> {

    /**
     * The stored value of type {@code T}.
     */
    private final T value;

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
}
