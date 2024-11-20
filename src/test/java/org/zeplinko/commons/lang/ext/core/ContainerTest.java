package org.zeplinko.commons.lang.ext.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContainerTest {

    @Test
    void test_givenNonNullValue_whenGetValueIsCalled_thenCorrectValueIsReturned() {
        String data = "test value";
        Container<String> container = new Container<>(data);

        assertSame(data, container.getValue());
    }

    @Test
    void test_givenNullValue_whenGetValueIsCalled_thenNullIsReturned() {
        Container<String> container = new Container<>(null);

        assertNull(container.getValue());
    }

    @Test
    void test_givenIntegerValue_whenGetValueIsCalled_thenCorrectValueIsReturned() {
        Integer data = 123;
        Container<Integer> container = new Container<>(data);

        assertEquals(123, container.getValue());
    }

    @Test
    void test_givenCustomObjectValue_whenGetValueIsCalled_thenCorrectObjectIsReturned() {
        MyCustomClass customObject = new MyCustomClass("custom value");
        Container<MyCustomClass> container = new Container<>(customObject);

        assertSame(customObject, container.getValue());
    }

    static class MyCustomClass {
        private final String value;

        public MyCustomClass(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
