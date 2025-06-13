package org.zeplinko.commons.lang.ext.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MutableContainerTest {

    @Test
    void test_givenNonNullInitialValue_whenGetValueIsCalled_thenCorrectValueIsReturned() {
        String data = "initial";
        MutableContainer<String> container = new MutableContainer<>(data);

        assertSame(data, container.getValue());
    }

    @Test
    void test_givenNullInitialValue_whenGetValueIsCalled_thenNullIsReturned() {
        MutableContainer<String> container = new MutableContainer<>(null);

        assertNull(container.getValue());
    }

    @Test
    void test_givenExistingValue_whenSetValueWithNewNonNullValue_thenValueIsUpdated() {
        MutableContainer<String> container = new MutableContainer<>("old");
        String newValue = "new";

        container.setValue(newValue);

        assertSame(newValue, container.getValue());
    }

    @Test
    void test_givenExistingValue_whenSetValueWithNull_thenValueIsCleared() {
        MutableContainer<String> container = new MutableContainer<>("data");

        container.setValue(null);

        assertNull(container.getValue());
    }

}
