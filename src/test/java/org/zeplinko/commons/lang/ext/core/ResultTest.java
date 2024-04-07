package org.zeplinko.commons.lang.ext.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ResultTest {

    @Test
    void testOkResultShouldBeSuccess() {
        Result<Integer, String> result = Result.ok(10);
        Assertions.assertTrue(result.isSuccess());
        Assertions.assertFalse(result.isFailure());
        Assertions.assertEquals(10, result.getData());
    }

    @Test
    void testErrorResultShouldBeFailure() {
        Result<Integer, String> result = Result.error("Error occurred");
        Assertions.assertTrue(result.isFailure());
        Assertions.assertFalse(result.isSuccess());
        Assertions.assertEquals("Error occurred", result.getError());
    }

    @Test
    void testOrElseWithSuccessResult() {
        Result<Integer, String> result = Result.ok(10);
        int value = result.orElse(5);
        Assertions.assertEquals(10, value);
    }

    @Test
    void testOrElseWithFailureResult() {
        Result<Integer, String> result = Result.error("Error occurred");
        int value = result.orElse(5);
        Assertions.assertEquals(5, value);
    }

    @Test
    void testOrElseGetWithSuccessResult() {
        Result<Integer, String> result = Result.ok(10);
        int value = result.orElseGet(error -> 5);
        Assertions.assertEquals(10, value);
    }

    @Test
    void testOrElseGetWithFailureResult() {
        Result<Integer, String> result = Result.error("Error occurred");
        int value = result.orElseGet(error -> 5);
        Assertions.assertEquals(5, value);
    }

    @Test
    void testOrElseThrowWithSuccessResult() throws Exception {
        Result<Integer, String> result = Result.ok(10);
        int value = result.orElseThrow(Exception::new);
        Assertions.assertEquals(10, value);
    }

    @Test
    void testOrElseThrowWithFailureResult() {
        Result<Integer, String> result = Result.error("Error occurred");
        Exception exception = Assertions.assertThrows(
                Exception.class,
                () -> result.orElseThrow(Exception::new)
        );
        Assertions.assertEquals("Error occurred", exception.getMessage());
    }
}
