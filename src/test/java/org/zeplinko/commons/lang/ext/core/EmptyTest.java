package org.zeplinko.commons.lang.ext.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EmptyTest {
    /**
     * Test to ensure that the {@link Empty} class returns a non-null instance.
     */
    @Test
    void testGetInstanceIsNotNull() {
        Assertions.assertNotNull(Empty.getInstance(), "Empty.getInstance() should not return null");
    }

    /**
     * Test to ensure that {@link Empty#getInstance()} always returns the same
     * instance (singleton pattern).
     */
    @Test
    void testGetInstanceReturnsSameInstance() {
        Empty instance1 = Empty.getInstance();
        Empty instance2 = Empty.getInstance();
        Assertions.assertSame(instance1, instance2, "Empty.getInstance() should always return the same instance");
    }

    /**
     * Test to ensure that the {@link Empty} class cannot be instantiated from
     * outside.
     */
    @Test
    @SuppressWarnings("all")
    void testPrivateConstructor() {
        try {
            Empty.class.getDeclaredConstructor().setAccessible(true);
            Empty empty = Empty.class.getDeclaredConstructor().newInstance();
            Assertions.fail("Instantiation via reflection should not be allowed");
        } catch (Exception e) {
            Assertions.assertTrue(
                    e instanceof IllegalAccessException ||
                            e.getCause() instanceof IllegalAccessException ||
                            e.getCause() instanceof SecurityException,
                    "Expected an IllegalAccessException when trying to instantiate via reflection"
            );
        }
    }

}
