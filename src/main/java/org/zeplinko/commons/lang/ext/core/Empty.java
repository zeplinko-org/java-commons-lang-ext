package org.zeplinko.commons.lang.ext.core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * A singleton class representing an empty placeholder. This class is used as a
 * substitute for {@link Void} when a non-null placeholder is required.
 * <p>
 * The {@link Empty} class provides a singleton instance that can be used in
 * scenarios where a method or an operation requires a placeholder object, but
 * the usage of {@code null} is not desired or allowed.
 * </p>
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>
 * public Result&lt;String, Empty&gt; performOperation() {
 *     // Perform some operation
 *     return Result.error(Empty.getInstance());
 * }
 * </pre>
 *
 * <p>
 * This class cannot be instantiated from outside as its constructor is private.
 * The only way to get an instance of this class is through the
 * {@link #getInstance()} method.
 * </p>
 *
 * @see Void
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Empty {
    @SuppressWarnings("InstantiationOfUtilityClass")
    private static final Empty INSTANCE = new Empty();

    public static Empty getInstance() {
        return Empty.INSTANCE;
    }
}
