package org.zeplinko.commons.lang.ext.core;

import jakarta.annotation.Nonnull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static org.zeplinko.commons.lang.ext.core.Retryable.Outcome.TerminationReason.*;

/**
 * Executes a {@link Callable} with retry semantics.
 * <p>
 * A {@code Retryable} encapsulates a task that may be re-executed in the event
 * of failure or validation rejection. Clients configure the behaviour fluently
 * using:
 * </p>
 * <ul>
 * <li>{@link #retryUntilResult(Predicate)} – validation predicate that must
 * hold for the <em>result</em> to be accepted</li>
 * <li>{@link #retryOnFailure(Predicate)} – declare <em>exceptions</em> that
 * trigger a retry</li>
 * <li>{@link #noRetryOnFailure(Predicate)} – declare <em>exceptions</em> that
 * should not trigger a retry</li>
 * <li>{@link #maxRetries(int)} – maximum retry attempts</li>
 * <li>{@link #baseDelay(long, TimeUnit)} – base delay between attempts</li>
 * <li>{@link #backoff(BackoffStrategy)} – back-off strategy for calculating the
 * next delay</li>
 * </ul>
 * The task is started by calling {@link #run()} which returns a {@link Outcome}
 * describing the outcome. Since {@code Retryable} implements {@link Callable},
 * it can also be used directly with
 * {@link java.util.concurrent.ExecutorService} and other concurrency utilities.
 * <p>
 * <strong>Thread-safety:</strong> Instances of {@code Retryable} are
 * <em>not</em> designed for concurrent use. Configure the instance in a single
 * thread and either invoke {@link #run()} once or ensure that invocations
 * happen from the same thread. For parallel work create a separate
 * {@code Retryable} per thread.
 * </p>
 *
 * @param <R> the type of the successful result returned by the task
 */
public final class Retryable<R> implements Callable<Retryable.Outcome<R>> {

    private final Callable<R> retryableTask;

    private Predicate<? super R> resultPredicate = r -> true; // Validation predicate: accept any result by default

    private Predicate<? super Exception> exceptionPredicate = e -> false; // Do not retry on exceptions by default

    private Predicate<? super Exception> noRetryOnFailure = e -> false; // By default there are no no-retry
                                                                        // exceptions

    private int maxRetries = 0;

    private long baseDelayMillis = 0;

    private BackoffStrategy backoffStrategy = Backoff.FIXED;

    private Retryable(Callable<R> retryableTask) {
        this.retryableTask = Objects.requireNonNull(retryableTask);
    }

    /**
     * Creates a new {@code Retryable} for the supplied task.
     *
     * @param task the {@link Callable} to execute
     * @param <D>  the result type produced by the task
     * @return a new {@code Retryable}
     */
    public static <D> Retryable<D> of(@Nonnull Callable<D> task) {
        return new Retryable<>(task);
    }

    /**
     * Sets the validation predicate that must evaluate to {@code true} for the
     * result to be considered successful. If the predicate returns {@code false}
     * the task will be retried (subject to {@link #maxRetries(int)} and
     * delay/back-off settings).
     * <p>
     * <strong>Note:</strong> If the predicate itself throws an exception, that
     * exception will propagate immediately and will not trigger a retry. Such
     * exceptions typically indicate logic errors in the predicate implementation.
     * </p>
     *
     * @param predicate validation predicate (must not be {@code null})
     * @return this instance for chaining
     */
    public Retryable<R> retryUntilResult(@Nonnull Predicate<? super R> predicate) {
        this.resultPredicate = Objects.requireNonNull(predicate);
        return this;
    }

    /**
     * Defines which exceptions qualify for a retry by supplying a predicate.
     * <p>
     * <strong>Note:</strong> If the predicate itself throws an exception, that
     * exception will propagate immediately and will not trigger a retry. Such
     * exceptions typically indicate logic errors in the predicate implementation.
     * </p>
     *
     * @param predicate predicate deciding whether an exception is retryable
     * @return this instance for chaining
     */
    public Retryable<R> retryOnFailure(@Nonnull Predicate<? super Exception> predicate) {
        this.exceptionPredicate = Objects.requireNonNull(predicate);
        return this;
    }

    /**
     * Defines which exceptions should not trigger a retry by supplying a predicate.
     * When an exception matches this predicate, the retryable will immediately
     * return a failure outcome without attempting any retries.
     * <p>
     * This is the opposite of {@link #retryOnFailure(Predicate)}. If an exception
     * matches both predicates, {@code noRetryExceptionPredicate} takes precedence.
     * </p>
     * <p>
     * <strong>Note:</strong> If the predicate itself throws an exception, that
     * exception will propagate immediately and will not trigger a retry. Such
     * exceptions typically indicate logic errors in the predicate implementation.
     * </p>
     *
     * @param predicate predicate deciding whether an exception should not be
     *                  retried
     * @return this instance for chaining
     */
    public Retryable<R> noRetryOnFailure(@Nonnull Predicate<? super Exception> predicate) {
        this.noRetryOnFailure = Objects.requireNonNull(predicate);
        return this;
    }

    /**
     * Sets how many times the operation should be retried after the initial
     * attempt. A negative value is normalised to {@code 0}. No hard upper bound is
     * enforced—use care when supplying very large numbers as they may lead to
     * long-running loops.
     *
     * @param maxRetries number of retries to permit (must be &gt;= 0)
     * @return this instance for fluent chaining
     */
    public Retryable<R> maxRetries(int maxRetries) {
        this.maxRetries = Math.max(0, maxRetries);
        return this;
    }

    /**
     * Sets the base delay that is applied before scheduling the next attempt.
     * <p>
     * Any {@link TimeUnit} can be supplied. The value is internally converted to
     * milliseconds. No upper bound is enforced, so passing large values (for
     * instance {@code HOURS} or {@code DAYS}) will produce equally large sleep
     * times. Callers are responsible for choosing sensible delays.
     * </p>
     *
     * @param duration amount of time to wait between attempts
     * @param unit     unit for the duration; any unit
     * @return this instance for method chaining
     * @throws IllegalArgumentException if {@code duration} is negative
     */
    public Retryable<R> baseDelay(long duration, @Nonnull TimeUnit unit) {
        Objects.requireNonNull(unit);
        if (duration < 0)
            throw new IllegalArgumentException("Duration must be positive");

        this.baseDelayMillis = unit.toMillis(duration);
        return this;
    }

    /**
     * Selects the back-off strategy to compute the delay before the next retry.
     *
     * @param strategy the {@link Backoff} strategy to apply
     * @return this instance for method chaining
     */
    public Retryable<R> backoff(@Nonnull BackoffStrategy strategy) {
        this.backoffStrategy = Objects.requireNonNull(strategy);
        return this;
    }

    /**
     * Executes the configured task applying all retry rules and validation.
     *
     * @return a {@link Outcome} that captures success/failure, number of attempts
     *         and stop reason
     */
    public Outcome<R> run() {
        return call();
    }

    /**
     * Executes the configured task applying all retry rules and validation. This
     * method implements the {@link Callable} interface, allowing {@code Retryable}
     * instances to be used with {@link java.util.concurrent.ExecutorService} and
     * other concurrency utilities.
     *
     * @return a {@link Outcome} that captures success/failure, number of attempts
     *         and stop reason
     * @throws RuntimeException if an unexpected error occurs during execution
     */
    @Override
    public Outcome<R> call() {
        int attempt = 0;
        Exception error = null;
        Outcome.TerminationReason reason;

        while (true) {
            attempt++;
            Exception taskException = null;
            R value = null;
            try {
                value = retryableTask.call();
            } catch (Exception e) {
                taskException = e;
            }

            if (taskException == null) {
                if (resultPredicate.test(value))
                    return Outcome.success(value, attempt);
            } else {
                if (noRetryOnFailure.test(taskException))
                    return Outcome.failure(taskException, attempt, ABORTED_SKIP_RETRY_EXCEPTION);
                if (!exceptionPredicate.test(taskException))
                    return Outcome.failure(taskException, attempt, ABORTED_NON_RETRYABLE_EXCEPTION);
                error = taskException;
            }

            if (attempt > maxRetries) {
                reason = (error == null) ? RETRIES_EXHAUSTED_INVALID_RESULT : RETRIES_EXHAUSTED_RETRYABLE_EXCEPTION;
                break;
            }

            long delay = backoffStrategy.nextDelay(baseDelayMillis, attempt);
            if (delay > 0) {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    error = ie;
                    reason = INTERRUPTED;
                    break;
                }
            }
        }

        if (error == null) {
            error = new MaxRetriesExceededException("Validation failed after " + attempt + " attempts");
        }
        return Outcome.failure(error, attempt, reason);
    }

    /**
     * Strategy interface to calculate delay before each retry. Custom strategies
     * can implement this interface and be supplied via
     * {@link #backoff(BackoffStrategy)}.
     */
    public interface BackoffStrategy {
        long nextDelay(long base, int attempt);
    }

    public enum Backoff implements BackoffStrategy {
        FIXED {
            @Override
            public long nextDelay(long base, int attempt) {
                return base;
            }
        },
        LINEAR {
            @Override
            public long nextDelay(long base, int attempt) {
                return base * attempt;
            }
        },
        EXPONENTIAL {
            @Override
            public long nextDelay(long base, int attempt) {
                if (attempt <= 0 || base <= 0)
                    return 0;

                int shift = attempt - 1;
                if (shift >= Long.SIZE - 1)
                    return Long.MAX_VALUE;

                long multiplier = 1L << shift;
                if (base > Long.MAX_VALUE / multiplier)
                    return Long.MAX_VALUE;
                return base * multiplier;
            }
        };
    }

    private static final class MaxRetriesExceededException extends Exception {
        private MaxRetriesExceededException(String message) {
            super(message);
        }
    }

    /**
     * Immutable value object describing the outcome of a {@link Retryable#run()}
     * call.
     */
    @Getter
    @RequiredArgsConstructor
    public static final class Outcome<T> {
        public enum TerminationReason {
            SUCCEEDED,
            RETRIES_EXHAUSTED_INVALID_RESULT,
            RETRIES_EXHAUSTED_RETRYABLE_EXCEPTION,
            ABORTED_NON_RETRYABLE_EXCEPTION,
            ABORTED_SKIP_RETRY_EXCEPTION,
            INTERRUPTED
        }

        private final boolean success;

        private final int attempts;

        private final T data;

        private final Exception error;

        private final TerminationReason reason;

        static <T> Outcome<T> success(T data, int attempts) {
            return new Outcome<>(true, attempts, data, null, TerminationReason.SUCCEEDED);
        }

        static <T> Outcome<T> failure(Exception error, int attempts, TerminationReason reason) {
            return new Outcome<>(false, attempts, null, error, reason);
        }

        public Result<T, Exception> toResult() {
            return success ? Result.success(data) : Result.failure(error);
        }
    }
}
