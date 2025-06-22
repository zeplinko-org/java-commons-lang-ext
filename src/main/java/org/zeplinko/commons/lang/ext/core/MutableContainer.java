package org.zeplinko.commons.lang.ext.core;

/**
 * A mutable wrapper that holds a single value of type {@code T}.
 *
 * <p>
 * Unlike its immutable counterpart {@link Container}, this class offers a
 * {@linkplain #setValue(Object) mutator} that lets callers replace the stored
 * value after construction. No internal synchronisation is performed, so
 * instances are <strong>not</strong> thread-safe and must be externally
 * coordinated if accessed concurrently.
 * </p>
 *
 * @param <T> the type of the wrapped value
 */
public class MutableContainer<T> extends Container<T> {

    /**
     * Creates a new container initialised with the supplied value.
     *
     * @param value the initial value to store (may be {@code null})
     */
    public MutableContainer(T value) {
        super(value);
    }

    /**
     * Replaces the value held by this container.
     *
     * <p>
     * The new value completely overwrites the previous one. Passing {@code null} is
     * allowed and will set the container’s content as null.
     * </p>
     *
     * @param value the new value to store — may be {@code null}
     */
    public void setValue(T value) {
        super.value = value;
    }

    /**
     * Static factory method to create a {@code MutableContainer} instance.
     *
     * @param value the initial value to store (may be {@code null})
     * @param <T>   the type of the value
     * @return a new {@code MutableContainer} containing the given value
     */
    public static <T> MutableContainer<T> of(T value) {
        return new MutableContainer<>(value);
    }
}
