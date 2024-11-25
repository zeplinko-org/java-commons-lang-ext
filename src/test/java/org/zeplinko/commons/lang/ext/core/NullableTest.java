package org.zeplinko.commons.lang.ext.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class NullableTest {
    @Test
    void createNullableWithNonNullValueTest() {
        Nullable<String> nullable1 = Nullable.of("abc");
        Nullable<Long> nullable2 = Nullable.of(0L);

        Assertions.assertNotNull(nullable1);
        Assertions.assertNotNull(nullable2);
    }

    @Test
    void createNullableWithNullValueTest() {
        Nullable<String> nullable = Nullable.of(null);

        Assertions.assertNotNull(nullable);
    }

    @Test
    void emptyNullableTest() {
        Nullable<String> nullable1 = Nullable.empty();
        Nullable<Long> nullable2 = Nullable.empty();

        Assertions.assertNotNull(nullable1);
        Assertions.assertNotNull(nullable2);
    }

    @Test
    void isNullTest() {
        Nullable<String> nullable1 = Nullable.of("abc");
        Nullable<String> nullable2 = Nullable.of(null);

        Assertions.assertFalse(nullable1.isNull());
        Assertions.assertTrue(nullable2.isNull());
    }

    @Test
    void isNotNullTest() {
        Nullable<String> nullable1 = Nullable.of("abc");
        Nullable<String> nullable2 = Nullable.of(null);

        Assertions.assertTrue(nullable1.isNotNull());
        Assertions.assertFalse(nullable2.isNotNull());
    }

    @Test
    void ifPresentWithNonNullValueTest() {
        Nullable<String> abc = Nullable.of("abc");

        boolean[] flag = { false };
        abc.ifPresent(value -> flag[0] = true);

        Assertions.assertTrue(flag[0]);
    }

    @Test
    void ifPresentWithNullValueTest() {
        Nullable<String> abc = Nullable.empty();

        boolean[] flag = { false };
        abc.ifPresent(value -> flag[0] = true);

        Assertions.assertFalse(flag[0]);
    }

    @Test
    void ifPresentOrElseWithNonNullValueTest() {
        Nullable<String> abc = Nullable.of("abc");

        int[] flag = { 0 };
        abc.ifPresentOrElse(value -> flag[0] = 1, () -> flag[0] = -1);

        Assertions.assertEquals(1, flag[0]);
    }

    @Test
    void ifPresentOrElseWithNullValueTest() {
        Nullable<String> abc = Nullable.empty();

        int[] flag = { 0 };
        abc.ifPresentOrElse(value -> flag[0] = 1, () -> flag[0] = -1);

        Assertions.assertEquals(-1, flag[0]);
    }

    @Test
    void filterWithNonNullValuePositiveTest() {
        Nullable<Integer> nullable = Nullable.of(10);

        Nullable<Integer> result = nullable.filter(value -> value > 5);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isNotNull());
    }

    @Test
    void filterWithNonNullValueNegativeTest() {
        Nullable<Integer> nullable = Nullable.of(1);

        Nullable<Integer> result = nullable.filter(value -> value > 5);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isNull());
    }

    @Test
    void filterWithNullValueTest() {
        Nullable<Integer> nullable = Nullable.empty();

        Nullable<Integer> result = nullable.filter(value -> value > 5);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isNull());
    }

    @Test
    void mapNullableWithNonNullValueTest() {
        Nullable<String> nullable = Nullable.of("abc");

        Nullable<Integer> integerNullable = nullable.map(String::length).map(num -> num / 2);

        Assertions.assertNotNull(integerNullable);
        Assertions.assertNotNull(integerNullable.orElse(null));
    }

    @Test
    void mapNullableWithNullValueTest() {
        Nullable<String> nullable = Nullable.of(null);
        Nullable<Integer> integerNullable = nullable.map(String::length).map(num -> num / 2);

        Assertions.assertNotNull(integerNullable);
        Assertions.assertNull(integerNullable.orElse(null));
    }

    @Test
    void flatMapNullableWithNonNullValueTest() {
        Nullable<String> nullable = Nullable.of("abc");
        Nullable<Integer> integerNullable = nullable.flatMap(string -> Nullable.of(string.length()));

        Assertions.assertNotNull(integerNullable);
        Assertions.assertNotNull(integerNullable.orElse(null));
    }

    @Test
    void flatMapNullableWithNullValueTest() {
        Nullable<String> nullable = Nullable.of(null);
        Nullable<Integer> integerNullable = nullable.flatMap(string -> Nullable.of(string.length()));

        Assertions.assertNotNull(integerNullable);
        Assertions.assertNull(integerNullable.orElse(null));
    }

    @Test
    void orNullableWithNonNullValueTest() {
        Nullable<String> nullable1 = Nullable.of("abc");
        Nullable<String> nullable2 = Nullable.of("def");

        Nullable<String> nullable3 = nullable1.or(() -> nullable2);

        Assertions.assertNotNull(nullable3);
        Assertions.assertNotNull(nullable3.orElse(null));
        Assertions.assertEquals("abc", nullable3.orElse(null));
    }

    @Test
    void orNullableWithNullValueTest() {
        Nullable<String> nullable1 = Nullable.empty();
        Nullable<String> nullable2 = Nullable.of("def");

        Nullable<String> nullable3 = nullable1.or(() -> nullable2);

        Assertions.assertNotNull(nullable3);
        Assertions.assertNotNull(nullable3.orElse(null));
        Assertions.assertEquals("def", nullable3.orElse(null));
    }

    @Test
    void streamWithNonNullValueTest() {
        Nullable<String> nullable = Nullable.of("abc");
        Stream<String> stream = nullable.stream();

        Assertions.assertNotNull(stream);
        List<String> list = stream.collect(Collectors.toList());
        Assertions.assertEquals(1, list.size());
        Assertions.assertEquals("abc", list.get(0));
    }

    @Test
    void streamWithNullValueTest() {
        Nullable<String> nullable = Nullable.empty();
        Stream<String> stream = nullable.stream();

        Assertions.assertNotNull(stream);
        List<String> list = stream.collect(Collectors.toList());
        Assertions.assertTrue(list.isEmpty());
    }

    @Test
    void orElseWithNonNullValueTest() {
        Nullable<String> nullable = Nullable.of("abc");
        String value = nullable.orElse("def");

        Assertions.assertEquals("abc", value);
    }

    @Test
    void orElseWithNullValueTest() {
        Nullable<String> nullable = Nullable.empty();
        String value = nullable.orElse("def");

        Assertions.assertEquals("def", value);
    }

    @Test
    void orElseGetWithNonNullValueTest() {
        Nullable<String> nullable = Nullable.of("abc");
        String value = nullable.orElseGet(() -> "def");

        Assertions.assertEquals("abc", value);
    }

    @Test
    void orElseGetWithNullValueTest() {
        Nullable<String> nullable = Nullable.empty();
        String value = nullable.orElseGet(() -> "def");

        Assertions.assertEquals("def", value);
    }

    @Test
    void orElseThrowWithNonNullValueTest() {
        Nullable<String> nullable = Nullable.of("abc");

        String[] result = new String[1];
        Assertions.assertDoesNotThrow(() -> {
            result[0] = nullable.orElseThrow();
        });
        Assertions.assertEquals("abc", result[0]);
    }

    @Test
    void orElseThrowWithNullValueTest() {
        Nullable<String> nullable = Nullable.empty();

        NoSuchElementException exception = Assertions.assertThrows(NoSuchElementException.class, nullable::orElseThrow);
        Assertions.assertEquals("No value present", exception.getMessage());
    }

    @Test
    void orElseThrowGetWithNonNullValueTest() {
        Nullable<String> nullable = Nullable.of("abc");

        String[] result = new String[1];
        Assertions.assertDoesNotThrow(() -> {
            result[0] = nullable.orElseThrow(() -> new IllegalArgumentException("Illegal Argument"));
        });
        Assertions.assertEquals("abc", result[0]);
    }

    @Test
    void orElseThrowGetWithNullValueTest() {
        Nullable<String> nullable = Nullable.empty();

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> nullable.orElseThrow(() -> new IllegalArgumentException("Illegal Argument"))
        );
        Assertions.assertEquals("Illegal Argument", exception.getMessage());
    }

    @Test
    void equalsWithSameInstanceTest() {
        Nullable<String> nullable = Nullable.of("abc");

        @SuppressWarnings("EqualsWithItself")
        boolean equals = nullable.equals(nullable);

        // noinspection ConstantValue
        Assertions.assertTrue(equals);
    }

    @Test
    void equalsWithSameNonNullValueTest() {
        Nullable<String> nullable1 = Nullable.of("abc");
        Nullable<String> nullable2 = Nullable.of("abc");

        boolean equals = nullable1.equals(nullable2);

        Assertions.assertTrue(equals);
    }

    @Test
    void equalsWithSameNullValueTest() {
        Nullable<String> nullable1 = Nullable.empty();
        Nullable<String> nullable2 = Nullable.empty();

        boolean equals = nullable1.equals(nullable2);

        Assertions.assertTrue(equals);
    }

    @Test
    void equalsWithDifferentValueTest() {
        Nullable<String> nullable1 = Nullable.of("abc");
        Nullable<String> nullable2 = Nullable.of("def");

        boolean equals = nullable1.equals(nullable2);

        Assertions.assertFalse(equals);
    }

    @Test
    void equalsWithOneNullAndOneNonNullValueTest() {
        Nullable<String> nullable1 = Nullable.of("abc");
        Nullable<String> nullable2 = Nullable.empty();

        boolean equals1 = nullable1.equals(nullable2);
        boolean equals2 = nullable2.equals(nullable1);

        Assertions.assertFalse(equals1);
        Assertions.assertFalse(equals2);
    }

    @Test
    void equalsWithDifferentObjectTest() {
        Nullable<String> nullable = Nullable.of("abc");
        Optional<String> optional = Optional.of("abc");

        @SuppressWarnings("EqualsBetweenInconvertibleTypes")
        boolean equals = nullable.equals(optional);

        Assertions.assertFalse(equals);
    }

    @Test
    void hashCodeWithNonNullValueTest() {
        Nullable<String> nullable1 = Nullable.of("abc");
        Nullable<String> nullable2 = Nullable.of("abc");

        Assertions.assertEquals(nullable1.hashCode(), nullable2.hashCode());
    }

    @Test
    void hashCodeWithNullValueTest() {
        Nullable<String> nullable1 = Nullable.empty();
        Nullable<String> nullable2 = Nullable.empty();

        Assertions.assertEquals(nullable1.hashCode(), nullable2.hashCode());
    }

    @Test
    void toStringWithNonNullValueTest() {
        Nullable<String> nullable = Nullable.of("abc");
        Assertions.assertEquals("Nullable[abc]", nullable.toString());
    }

    @Test
    void toStringWithNullValueTest() {
        Nullable<String> nullable = Nullable.empty();
        Assertions.assertEquals("Nullable.empty", nullable.toString());
    }
}
