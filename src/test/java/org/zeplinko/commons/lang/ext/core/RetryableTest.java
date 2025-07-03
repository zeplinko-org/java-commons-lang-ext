package org.zeplinko.commons.lang.ext.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.ArrayList;

@DisplayName("Retryable Tests")
class RetryableTest {

    @Test
    @DisplayName("Should return success immediately when result is valid")
    void shouldReturnSuccessImmediatelyWhenResultIsValid() {
        Retryable<Integer> retryable = Retryable.of(() -> 42)
                .retryUntilResult(r -> r % 2 == 0)
                .maxRetries(3)
                .baseDelay(0, TimeUnit.MILLISECONDS);

        Retryable.Outcome<Integer> outcome = retryable.run();
        Assertions.assertTrue(outcome.isSuccess());
        Assertions.assertEquals(42, outcome.getData());
        Assertions.assertEquals(1, outcome.getAttempts());
        Assertions.assertEquals(
                Retryable.Outcome.TerminationReason.SUCCEEDED,
                outcome.getReason()
        );

        Result<Integer, Exception> result = outcome.toResult();
        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals(42, result.getData());
    }

    @Test
    @DisplayName("Should retry until validation passes")
    void shouldRetryUntilValidationPasses() {
        AtomicInteger counter = new AtomicInteger();
        Retryable<Integer> retryable = Retryable.of(counter::incrementAndGet)
                .retryUntilResult(r -> r % 2 == 0)
                .maxRetries(3)
                .baseDelay(0, TimeUnit.MILLISECONDS);

        Retryable.Outcome<Integer> outcome = retryable.run();
        Assertions.assertTrue(outcome.isSuccess());
        Assertions.assertEquals(2, outcome.getData());
        Assertions.assertEquals(2, outcome.getAttempts());
        Assertions.assertEquals(
                Retryable.Outcome.TerminationReason.SUCCEEDED,
                outcome.getReason()
        );
        Assertions.assertEquals(2, counter.get());
    }

    @Test
    @DisplayName("Should fail after max retries when validation never passes")
    void shouldFailAfterMaxRetriesWhenValidationNeverPasses() {
        AtomicInteger attemptCounter = new AtomicInteger();

        Retryable<Integer> retryable = Retryable.of(() -> {
            attemptCounter.incrementAndGet();
            return 1;
        })
                .retryUntilResult(r -> r % 2 == 0)
                .maxRetries(1)
                .baseDelay(0, TimeUnit.MILLISECONDS);

        Retryable.Outcome<Integer> outcome = retryable.run();
        Assertions.assertFalse(outcome.isSuccess());
        Assertions.assertTrue(outcome.getError().getMessage().contains("Validation failed"));
        Assertions.assertEquals(2, outcome.getAttempts());
        Assertions.assertEquals(
                Retryable.Outcome.TerminationReason.RETRIES_EXHAUSTED_INVALID_RESULT,
                outcome.getReason()
        );
        Assertions.assertEquals(2, attemptCounter.get());
    }

    @Test
    @DisplayName("Should retry when exception is retryable and eventually succeed")
    void shouldRetryWhenExceptionIsRetryableAndEventuallySucceed() {
        AtomicInteger counter = new AtomicInteger();
        Retryable<Integer> retryable = Retryable.of(() -> {
            int attempt = counter.incrementAndGet();
            if (attempt == 1) {
                throw new IOException("IOException");
            }
            return 100;
        })
                .retryOnFailure(e -> e instanceof IOException)
                .retryUntilResult(r -> true)
                .maxRetries(2)
                .baseDelay(0, TimeUnit.MILLISECONDS);

        Retryable.Outcome<Integer> outcome = retryable.run();
        Assertions.assertTrue(outcome.isSuccess());
        Assertions.assertEquals(100, outcome.getData());
        Assertions.assertEquals(2, outcome.getAttempts());
        Assertions.assertEquals(
                Retryable.Outcome.TerminationReason.SUCCEEDED,
                outcome.getReason()
        );
        Assertions.assertEquals(2, counter.get());
    }

    @Test
    @DisplayName("Should fail immediately when exception is not retryable")
    void shouldFailImmediatelyWhenExceptionIsNotRetryable() {
        AtomicInteger counter = new AtomicInteger();
        Retryable<Integer> retryable = Retryable.<Integer>of(() -> {
            counter.incrementAndGet();
            throw new IllegalArgumentException("IllegalArgumentException");
        })
                .retryOnFailure(e -> e instanceof IOException)
                .maxRetries(3)
                .baseDelay(0, TimeUnit.MILLISECONDS);

        Retryable.Outcome<Integer> outcome = retryable.run();
        Assertions.assertFalse(outcome.isSuccess());
        Assertions.assertInstanceOf(IllegalArgumentException.class, outcome.getError());
        Assertions.assertEquals(1, outcome.getAttempts());
        Assertions.assertEquals(
                Retryable.Outcome.TerminationReason.ABORTED_NON_RETRYABLE_EXCEPTION,
                outcome.getReason()
        );
        Assertions.assertEquals(1, counter.get());
    }

    @Test
    @DisplayName("Should attempt max retries plus initial when exceptions stay retryable")
    void shouldAttemptMaxRetriesPlusInitialWhenExceptionsStayRetryable() {
        AtomicInteger counter = new AtomicInteger();
        Retryable<Integer> retryable = Retryable.<Integer>of(() -> {
            counter.incrementAndGet();
            throw new IOException("IOException");
        })
                .retryOnFailure(e -> e instanceof IOException)
                .maxRetries(3)
                .baseDelay(0, TimeUnit.MILLISECONDS);

        Retryable.Outcome<Integer> outcome = retryable.run();
        Assertions.assertFalse(outcome.isSuccess());
        Assertions.assertEquals(4, outcome.getAttempts());
        Assertions.assertEquals(
                Retryable.Outcome.TerminationReason.RETRIES_EXHAUSTED_RETRYABLE_EXCEPTION,
                outcome.getReason()
        );
        Assertions.assertInstanceOf(IOException.class, outcome.getError());
        Assertions.assertEquals(4, counter.get());
    }

    @Test
    @DisplayName("Should attempt requested retries plus initial when validation always fails")
    void shouldAttemptRequestedRetriesPlusInitialWhenValidationAlwaysFails() {
        AtomicInteger counter = new AtomicInteger();
        int requestedRetries = 5000;
        Retryable<Integer> retryable = Retryable.of(() -> {
            counter.incrementAndGet();
            return 1;
        })
                .retryUntilResult(r -> false)
                .maxRetries(requestedRetries)
                .baseDelay(0, TimeUnit.MILLISECONDS);

        Retryable.Outcome<Integer> outcome = retryable.run();
        Assertions.assertFalse(outcome.isSuccess());
        Assertions.assertEquals(requestedRetries + 1, outcome.getAttempts());
        Assertions.assertEquals(
                Retryable.Outcome.TerminationReason.RETRIES_EXHAUSTED_INVALID_RESULT,
                outcome.getReason()
        );
        Assertions.assertEquals(requestedRetries + 1, counter.get());
    }

    @Test
    @DisplayName("Should return failure when interrupted during delay")
    void shouldReturnFailureWhenInterruptedDuringDelay() throws InterruptedException {
        Retryable<Integer> retryable = Retryable.of(() -> 1)
                .retryUntilResult(r -> false)
                .maxRetries(1)
                .baseDelay(1000, TimeUnit.MILLISECONDS);

        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(1);
        AtomicReference<Retryable.Outcome<Integer>> ref = new AtomicReference<>();

        Thread worker = new Thread(() -> {
            started.countDown();
            try {
                ref.set(retryable.run());
            } finally {
                finished.countDown();
            }
        });

        worker.start();
        started.await();

        worker.interrupt();
        finished.await();

        Retryable.Outcome<Integer> outcome = ref.get();
        Assertions.assertNotNull(outcome);
        Assertions.assertFalse(outcome.isSuccess());
        Assertions.assertEquals(
                Retryable.Outcome.TerminationReason.INTERRUPTED,
                outcome.getReason()
        );
        Assertions.assertInstanceOf(InterruptedException.class, outcome.getError());

        // verify toResult mapping
        Result<Integer, Exception> resultObj = outcome.toResult();
        Assertions.assertNotNull(resultObj);
        Assertions.assertTrue(resultObj.isFailure());
        Assertions.assertInstanceOf(InterruptedException.class, resultObj.getError());
    }

    @MethodSource("backoffParameters")
    @ParameterizedTest(name = "{0} baseDelay={1}ms retries={2}")
    @DisplayName("Should accumulate expected delay for backoff strategies")
    void shouldAccumulateExpectedDelayForBackoffStrategies(
            Retryable.Backoff strategy,
            long baseDelay,
            int retries,
            long expectedMinDelay
    ) {
        AtomicInteger counter = new AtomicInteger();
        Retryable<Integer> retryable = Retryable.of(() -> {
            int attempt = counter.incrementAndGet();
            return attempt == retries + 1 ? 123 : 0;
        })
                .retryUntilResult(r -> r == 123)
                .maxRetries(retries)
                .baseDelay(baseDelay, TimeUnit.MILLISECONDS)
                .backoff(strategy);

        long start = System.nanoTime();
        retryable.run().toResult();
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;

        // Allow for some scheduling variance
        long tolerance = 10; // milliseconds
        Assertions.assertTrue(
                elapsedMillis >= expectedMinDelay - tolerance,
                String.format(
                        "Elapsed time (%d ms) should be close to expected delay (%d ms) for %s",
                        elapsedMillis,
                        expectedMinDelay,
                        strategy
                )
        );
    }

    private static Stream<Arguments> backoffParameters() {
        return Stream.of(
                Arguments.of(Retryable.Backoff.FIXED, 25L, 3, 75L),
                Arguments.of(Retryable.Backoff.LINEAR, 25L, 3, 150L),
                Arguments.of(Retryable.Backoff.EXPONENTIAL, 25L, 3, 175L)
        );
    }

    @Test
    @DisplayName("Should return zero in exponential backoff for non-positive attempt")
    void shouldReturnZeroInExponentialBackoffForNonPositiveAttempt() {
        long base = 25L;
        Assertions.assertEquals(0, Retryable.Backoff.EXPONENTIAL.nextDelay(base, 0));
        Assertions.assertEquals(0, Retryable.Backoff.EXPONENTIAL.nextDelay(base, -2));
    }

    @Test
    @DisplayName("Should return zero in exponential backoff for non-positive base")
    void shouldReturnZeroInExponentialBackoffForNonPositiveBase() {
        Assertions.assertEquals(0, Retryable.Backoff.EXPONENTIAL.nextDelay(0, 5));
        Assertions.assertEquals(0, Retryable.Backoff.EXPONENTIAL.nextDelay(-10, 5));
    }

    @Test
    @DisplayName("Should cap delay to Long.MAX_VALUE when shift overflow in exponential backoff")
    void shouldCapDelayToLongMaxWhenShiftOverflowInExponentialBackoff() {
        long base = 1L;
        int bigAttempt = 65;
        Assertions.assertEquals(Long.MAX_VALUE, Retryable.Backoff.EXPONENTIAL.nextDelay(base, bigAttempt));
    }

    @Test
    @DisplayName("Should cap delay to Long.MAX_VALUE when multiplication overflow in exponential backoff")
    void shouldCapDelayToLongMaxWhenMultiplicationOverflowInExponentialBackoff() {
        long base = Long.MAX_VALUE;
        int attempt = 2;
        Assertions.assertEquals(Long.MAX_VALUE, Retryable.Backoff.EXPONENTIAL.nextDelay(base, attempt));
    }

    @Test
    @DisplayName("Should accept custom backoff strategy")
    void shouldAcceptCustomBackoffStrategy() {
        AtomicInteger counter = new AtomicInteger();
        Retryable.BackoffStrategy tripleBackoff = (base, attempt) -> base * 3;

        Retryable<Integer> retryable = Retryable.of(() -> {
            int att = counter.incrementAndGet();
            return att == 2 ? 7 : 0;
        })
                .retryUntilResult(r -> r == 7)
                .maxRetries(1)
                .baseDelay(10, TimeUnit.MILLISECONDS)
                .backoff(tripleBackoff);

        long start = System.nanoTime();
        Retryable.Outcome<Integer> outcome = retryable.run();
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;

        Assertions.assertTrue(outcome.isSuccess());
        Assertions.assertEquals(7, outcome.getData());
        Assertions.assertEquals(2, outcome.getAttempts());
        Assertions.assertEquals(
                Retryable.Outcome.TerminationReason.SUCCEEDED,
                outcome.getReason()
        );
        // Allow for some scheduling variance
        long tolerance = 10; // milliseconds
        Assertions.assertTrue(
                elapsedMillis >= 30 - tolerance,
                String.format("Elapsed time (%d ms) should be close to expected delay (30 ms)", elapsedMillis)
        );
    }

    @Test
    @DisplayName("Should throw exception for negative base delay")
    void shouldThrowExceptionForNegativeBaseDelay() {
        Retryable<Integer> retryable = Retryable.of(() -> 1);
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> retryable.baseDelay(-1, TimeUnit.MILLISECONDS)
        );
    }

    @Test
    @DisplayName("Should throw exception for null time unit")
    void shouldThrowExceptionForNullTimeUnit() {
        Retryable<Integer> retryable = Retryable.of(() -> 1);
        Assertions.assertThrows(
                NullPointerException.class,
                () -> retryable.baseDelay(1, null)
        );
    }

    @Test
    @DisplayName("Should convert base delay to milliseconds")
    void shouldConvertBaseDelayToMillis() throws Exception {
        Retryable<Integer> retryable = Retryable.of(() -> 1)
                .baseDelay(2, TimeUnit.SECONDS);

        Field f = Retryable.class.getDeclaredField("baseDelayMillis");
        f.setAccessible(true);
        long millis = f.getLong(retryable);
        Assertions.assertEquals(2000L, millis);
    }

    @Test
    @DisplayName("Should abort on exception with default exception predicate")
    void shouldAbortOnExceptionWithDefaultExceptionPredicate() {
        Retryable<Integer> retryable = Retryable.of((java.util.concurrent.Callable<Integer>) () -> {
            throw new IllegalStateException("boom");
        })
                .maxRetries(5);

        Retryable.Outcome<Integer> outcome = retryable.run();

        Assertions.assertFalse(outcome.isSuccess());
        Assertions.assertEquals(1, outcome.getAttempts());
        Assertions.assertEquals(
                Retryable.Outcome.TerminationReason.ABORTED_NON_RETRYABLE_EXCEPTION,
                outcome.getReason()
        );
        Assertions.assertInstanceOf(IllegalStateException.class, outcome.getError());
    }

    @Test
    @DisplayName("Should indicate retries exhausted when validation never passes")
    void shouldIndicateRetriesExhaustedWhenValidationNeverPasses() {
        AtomicInteger counter = new AtomicInteger();

        Retryable<Integer> retryable = Retryable.of(counter::incrementAndGet)
                .retryUntilResult(r -> false) // always invalid
                .maxRetries(2)
                .baseDelay(0, TimeUnit.MILLISECONDS);

        Retryable.Outcome<Integer> outcome = retryable.run();

        Assertions.assertFalse(outcome.isSuccess());
        Assertions.assertEquals(3, outcome.getAttempts());
        Assertions.assertEquals(
                Retryable.Outcome.TerminationReason.RETRIES_EXHAUSTED_INVALID_RESULT,
                outcome.getReason()
        );
        Assertions.assertNotNull(outcome.getError());
    }

    @Test
    @DisplayName("Should produce different outcomes when same retryable is run multiple times")
    void shouldProduceDifferentOutcomesWhenSameRetryableIsRunMultipleTimes() {
        AtomicInteger globalCounter = new AtomicInteger();

        Retryable<Integer> retryable = Retryable.of(() -> {
            int attempt = globalCounter.incrementAndGet();
            if (attempt % 2 == 0) {
                return 100; // success
            } else {
                return 1; // Will fail validation
            }
        })
                .retryUntilResult(r -> r == 100)
                .baseDelay(0, TimeUnit.MILLISECONDS);

        // First run - should fail (odd attempt)
        Retryable.Outcome<Integer> outcome1 = retryable.run();
        Assertions.assertFalse(outcome1.isSuccess());
        Assertions.assertEquals(1, outcome1.getAttempts());
        Assertions.assertEquals(
                Retryable.Outcome.TerminationReason.RETRIES_EXHAUSTED_INVALID_RESULT,
                outcome1.getReason()
        );

        // Second run - should succeed (even attempt)
        Retryable.Outcome<Integer> outcome2 = retryable.run();
        Assertions.assertTrue(outcome2.isSuccess());
        Assertions.assertEquals(100, outcome2.getData());
        Assertions.assertEquals(1, outcome2.getAttempts());
        Assertions.assertEquals(
                Retryable.Outcome.TerminationReason.SUCCEEDED,
                outcome2.getReason()
        );

        // Third run - should fail again (odd attempt)
        Retryable.Outcome<Integer> outcome3 = retryable.run();
        Assertions.assertFalse(outcome3.isSuccess());
        Assertions.assertEquals(1, outcome3.getAttempts());
        Assertions.assertEquals(
                Retryable.Outcome.TerminationReason.RETRIES_EXHAUSTED_INVALID_RESULT,
                outcome3.getReason()
        );

        // Verify that the global counter was incremented correctly
        Assertions.assertEquals(3, globalCounter.get());

        Assertions.assertNotSame(outcome1, outcome2);
        Assertions.assertNotSame(outcome2, outcome3);
        Assertions.assertNotSame(outcome1, outcome3);
    }

    @Test
    @DisplayName("Should work as a Callable with ExecutorService")
    void shouldWorkAsCallableWithExecutorService() throws Exception {
        AtomicInteger counter = new AtomicInteger();

        Retryable<String> retryable = Retryable.of(() -> {
            int attempt = counter.incrementAndGet();
            if (attempt == 1) {
                throw new IOException("First attempt fails");
            }
            return "Success on attempt " + attempt;
        })
                .retryOnFailure(e -> e instanceof IOException)
                .maxRetries(2)
                .baseDelay(1, TimeUnit.MILLISECONDS);

        ExecutorService executor = Executors.newSingleThreadExecutor();

        try {
            Future<Retryable.Outcome<String>> future = executor.submit(retryable);
            Retryable.Outcome<String> outcome = future.get();

            Assertions.assertTrue(outcome.isSuccess());
            Assertions.assertEquals("Success on attempt 2", outcome.getData());
            Assertions.assertEquals(2, outcome.getAttempts());
            Assertions.assertEquals(
                    Retryable.Outcome.TerminationReason.SUCCEEDED,
                    outcome.getReason()
            );

            Assertions.assertEquals(2, counter.get());

        } finally {
            executor.shutdown();
        }
    }

    @Test
    @DisplayName("Should work with CompletableFuture.supplyAsync")
    void shouldWorkWithCompletableFutureSupplyAsync() throws Exception {
        AtomicInteger counter = new AtomicInteger();

        Retryable<Integer> retryable = Retryable.of(() -> {
            int attempt = counter.incrementAndGet();
            if (attempt < 3) {
                return 0; // Will fail validation
            }
            return 42;
        })
                .retryUntilResult(r -> r == 42)
                .maxRetries(3)
                .baseDelay(1, TimeUnit.MILLISECONDS);

        CompletableFuture<Retryable.Outcome<Integer>> future = CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return retryable.call();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

        Retryable.Outcome<Integer> outcome = future.get();

        Assertions.assertTrue(outcome.isSuccess());
        Assertions.assertEquals(42, outcome.getData());
        Assertions.assertEquals(3, outcome.getAttempts());
        Assertions.assertEquals(
                Retryable.Outcome.TerminationReason.SUCCEEDED,
                outcome.getReason()
        );

        Assertions.assertEquals(3, counter.get());
    }

    @Test
    @DisplayName("Should handle concurrent execution safely with separate retryable instances")
    void shouldHandleConcurrentExecutionSafelyWithSeparateRetryableInstances() throws Exception {
        final int threadCount = 10;
        final int executionsPerThread = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        try {
            List<Future<List<Retryable.Outcome<Integer>>>> futures = IntStream.range(0, threadCount)
                    .mapToObj(threadId -> executor.submit(() -> getOutcomesForAThread(executionsPerThread, threadId)))
                    .collect(Collectors.toList());

            List<Retryable.Outcome<Integer>> allOutcomes = futures.stream()
                    .map(RetryableTest::getFutureResult)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());

            Assertions.assertEquals(threadCount * executionsPerThread, allOutcomes.size());

            // All outcomes should be successful (since we retry on RuntimeException)
            long successCount = allOutcomes.stream()
                    .mapToLong(outcome -> outcome.isSuccess() ? 1 : 0)
                    .sum();
            Assertions.assertEquals(threadCount * executionsPerThread, successCount);

            // Verify that each outcome has the expected number of attempts (should be 2)
            for (Retryable.Outcome<Integer> outcome : allOutcomes) {
                Assertions.assertTrue(outcome.isSuccess());
                Assertions.assertEquals(2, outcome.getAttempts());
                Assertions.assertEquals(
                        Retryable.Outcome.TerminationReason.SUCCEEDED,
                        outcome.getReason()
                );
                Assertions.assertNotNull(outcome.getData());
            }

            // Verify that we got unique values from different threads
            long uniqueValues = allOutcomes.stream()
                    .mapToInt(Retryable.Outcome::getData)
                    .distinct()
                    .count();
            Assertions.assertEquals(threadCount * executionsPerThread, uniqueValues);
        } finally {
            executor.shutdown();
        }
    }

    private static List<Retryable.Outcome<Integer>> getFutureResult(
            Future<List<Retryable.Outcome<Integer>>> futureList
    ) {
        try {
            return futureList.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Retryable.Outcome<Integer>> getOutcomesForAThread(int executionsPerThread, int threadId) {
        List<Retryable.Outcome<Integer>> outcomes = new ArrayList<>();
        AtomicInteger localCounter = new AtomicInteger();

        for (int j = 0; j < executionsPerThread; j++) {
            // Each thread creates its own retryable instance
            Retryable<Integer> retryable = Retryable.of(() -> {
                int attempt = localCounter.incrementAndGet();
                // Succeed on attempts 2, 4, 6, etc.
                if (attempt % 2 == 0) {
                    return threadId * 100 + attempt; // Unique success value per thread
                } else {
                    throw new RuntimeException("Simulated failure");
                }
            })
                    .retryOnFailure(e -> e instanceof RuntimeException)
                    .maxRetries(2)
                    .baseDelay(1, TimeUnit.MILLISECONDS);

            outcomes.add(retryable.run());
        }
        return outcomes;
    }

    @Test
    @DisplayName("Should propagate validation predicate exceptions without retry")
    void shouldPropagateValidationPredicateExceptionsWithoutRetry() {
        AtomicInteger taskCounter = new AtomicInteger();

        Retryable<Integer> retryableWithBadResultPredicate = Retryable.of(() -> {
            taskCounter.incrementAndGet();
            return 42;
        })
                .retryUntilResult(r -> {
                    throw new IllegalStateException("Predicate logic error");
                })
                .maxRetries(3);

        IllegalStateException resultPredicateException = Assertions.assertThrows(
                IllegalStateException.class,
                retryableWithBadResultPredicate::run
        );
        Assertions.assertEquals("Predicate logic error", resultPredicateException.getMessage());
        Assertions.assertEquals(1, taskCounter.get());
    }

    @Test
    @DisplayName("Should propagate exception predicate exceptions without retry")
    void shouldPropagateExceptionPredicateExceptionsWithoutRetry() {
        AtomicInteger taskCounter = new AtomicInteger();
        Retryable<Integer> retryableWithBadExceptionPredicate = Retryable.<Integer>of(() -> {
            taskCounter.incrementAndGet();
            throw new IOException("Task failure");
        })
                .retryOnFailure(e -> {
                    throw new IllegalArgumentException("Exception predicate logic error");
                })
                .maxRetries(3);

        IllegalArgumentException exceptionPredicateException = Assertions.assertThrows(
                IllegalArgumentException.class,
                retryableWithBadExceptionPredicate::run
        );
        Assertions.assertEquals("Exception predicate logic error", exceptionPredicateException.getMessage());
        Assertions.assertEquals(1, taskCounter.get());
    }

    @Test
    @DisplayName("Should not retry when exception matches noRetryExceptionPredicate predicate")
    void shouldNotRetryWhenExceptionMatchesNoRetryOnFailure() {
        AtomicInteger counter = new AtomicInteger();

        Retryable<Integer> retryable = Retryable.<Integer>of(() -> {
            counter.incrementAndGet();
            throw new IllegalArgumentException("Should not retry this");
        })
                .noRetryOnFailure(e -> e instanceof IllegalArgumentException)
                .maxRetries(3)
                .baseDelay(0, TimeUnit.MILLISECONDS);

        Retryable.Outcome<Integer> outcome = retryable.run();

        Assertions.assertFalse(outcome.isSuccess());
        Assertions.assertEquals(1, outcome.getAttempts());
        Assertions.assertEquals(
                Retryable.Outcome.TerminationReason.ABORTED_SKIP_RETRY_EXCEPTION,
                outcome.getReason()
        );
        Assertions.assertInstanceOf(IllegalArgumentException.class, outcome.getError());
        Assertions.assertEquals("Should not retry this", outcome.getError().getMessage());
        Assertions.assertEquals(1, counter.get());
    }

    @Test
    @DisplayName("Should retry when exception does not match noRetryExceptionPredicate predicate")
    void shouldRetryWhenExceptionDoesNotMatchNoRetryOnFailure() {
        AtomicInteger counter = new AtomicInteger();

        Retryable<Integer> retryable = Retryable.<Integer>of(() -> {
            counter.incrementAndGet();
            throw new IOException("Should retry this");
        })
                .noRetryOnFailure(e -> e instanceof IllegalArgumentException)
                .retryOnFailure(e -> e instanceof IOException)
                .maxRetries(2)
                .baseDelay(0, TimeUnit.MILLISECONDS);

        Retryable.Outcome<Integer> outcome = retryable.run();

        Assertions.assertFalse(outcome.isSuccess());
        Assertions.assertEquals(3, outcome.getAttempts());
        Assertions.assertEquals(
                Retryable.Outcome.TerminationReason.RETRIES_EXHAUSTED_RETRYABLE_EXCEPTION,
                outcome.getReason()
        );
        Assertions.assertInstanceOf(IOException.class, outcome.getError());
        Assertions.assertEquals(3, counter.get());
    }

    @Test
    @DisplayName("Should prioritize noRetryExceptionPredicate over retryOnFailure when both match")
    void shouldPrioritizeNoRetryExceptionPredicateOverRetryOnFailureWhenBothMatch() {
        AtomicInteger counter = new AtomicInteger();

        Retryable<Integer> retryable = Retryable.<Integer>of(() -> {
            counter.incrementAndGet();
            throw new RuntimeException("Both predicates match");
        })
                .retryOnFailure(e -> e instanceof RuntimeException)
                .noRetryOnFailure(e -> e instanceof RuntimeException)
                .maxRetries(3)
                .baseDelay(0, TimeUnit.MILLISECONDS);

        Retryable.Outcome<Integer> outcome = retryable.run();

        Assertions.assertFalse(outcome.isSuccess());
        Assertions.assertEquals(1, outcome.getAttempts());
        Assertions.assertEquals(
                Retryable.Outcome.TerminationReason.ABORTED_SKIP_RETRY_EXCEPTION,
                outcome.getReason()
        );
        Assertions.assertInstanceOf(RuntimeException.class, outcome.getError());
        Assertions.assertEquals(1, counter.get());
    }

    @Test
    @DisplayName("Should work with default predicates (no retries by default due to exceptionPredicate)")
    void shouldWorkWithDefaultPredicates() {
        AtomicInteger counter = new AtomicInteger();

        // Default behavior:
        // - exceptionPredicate = e -> false (don't retry any exception)
        // - noRetryPredicate = e -> false (don't block any exception)
        // Result: no retries because exceptionPredicate rejects all exceptions
        Retryable<Integer> retryable = Retryable.<Integer>of(() -> {
            counter.incrementAndGet();
            throw new IOException("Default behavior test");
        })
                .maxRetries(3)
                .baseDelay(0, TimeUnit.MILLISECONDS);
        Retryable.Outcome<Integer> outcome = retryable.run();

        Assertions.assertFalse(outcome.isSuccess());
        Assertions.assertEquals(1, outcome.getAttempts());
        Assertions.assertEquals(
                Retryable.Outcome.TerminationReason.ABORTED_NON_RETRYABLE_EXCEPTION,
                outcome.getReason()
        );
        Assertions.assertInstanceOf(IOException.class, outcome.getError());
        Assertions.assertEquals(1, counter.get());
    }

    @Test
    @DisplayName("Should allow retries when noRetryExceptionPredicate predicate is set to always false")
    void shouldAllowRetriesWhenNoRetryOnFailureIsSetToAlwaysFalse() {
        AtomicInteger counter = new AtomicInteger();

        Retryable<Integer> retryable = Retryable.<Integer>of(() -> {
            counter.incrementAndGet();
            throw new IOException("Should retry this");
        })
                .noRetryOnFailure(e -> false) // Never block retries
                .retryOnFailure(e -> e instanceof IOException) // Allow retry on IOException
                .maxRetries(2)
                .baseDelay(0, TimeUnit.MILLISECONDS);

        Retryable.Outcome<Integer> outcome = retryable.run();

        Assertions.assertFalse(outcome.isSuccess());
        Assertions.assertEquals(3, outcome.getAttempts());
        Assertions.assertEquals(
                Retryable.Outcome.TerminationReason.RETRIES_EXHAUSTED_RETRYABLE_EXCEPTION,
                outcome.getReason()
        );
        Assertions.assertInstanceOf(IOException.class, outcome.getError());
        Assertions.assertEquals(3, counter.get());
    }

    @Test
    @DisplayName("Should propagate noRetryExceptionPredicate predicate exceptions without retry")
    void shouldPropagateNoRetryExceptionPredicatePredicateExceptionsWithoutRetry() {
        AtomicInteger taskCounter = new AtomicInteger();

        Retryable<Integer> retryable = Retryable.<Integer>of(() -> {
            taskCounter.incrementAndGet();
            throw new IOException("Task failure");
        })
                .noRetryOnFailure(e -> {
                    throw new IllegalStateException("noRetryExceptionPredicate predicate logic error");
                })
                .maxRetries(3);

        IllegalStateException predicateException = Assertions.assertThrows(
                IllegalStateException.class,
                retryable::run
        );
        Assertions.assertEquals("noRetryExceptionPredicate predicate logic error", predicateException.getMessage());
        Assertions.assertEquals(1, taskCounter.get());
    }
}
