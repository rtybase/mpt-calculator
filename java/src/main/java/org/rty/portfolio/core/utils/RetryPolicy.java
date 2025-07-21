package org.rty.portfolio.core.utils;

import java.util.Map;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.core.functions.CheckedSupplier;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;

public class RetryPolicy {
	public static final int MAX_RETRY = 3;

	private static final long INITIAL_WAIT_IN_MS = 1000L;
	private static final long MAXIMUM_WAIT_IN_MS = 4000L;
	private static final String RETRY_ALL_EXCEPTIONS_NAME = "retry-all-exceptions";

	private static final RetryConfig RETRY_ALL_EXCEPTIONS_CONFIG = RetryConfig.custom()
			  .maxAttempts(MAX_RETRY)
			  .failAfterMaxAttempts(true)
			  .intervalFunction(IntervalFunction.ofExponentialBackoff(INITIAL_WAIT_IN_MS,
					  2,
					  MAXIMUM_WAIT_IN_MS))
			  .build();

	private static final RetryRegistry REGISTRY = RetryRegistry
			.of(Map.of(RETRY_ALL_EXCEPTIONS_NAME, RETRY_ALL_EXCEPTIONS_CONFIG));

	private static final Retry RETRY_ALL_EXCEPTIONS = REGISTRY.retry(RETRY_ALL_EXCEPTIONS_NAME,
			RETRY_ALL_EXCEPTIONS_NAME);

	private RetryPolicy() {
	}

	public static <T> T execute(CheckedSupplier<T> checkedSupplier) throws Throwable {
		return RETRY_ALL_EXCEPTIONS.executeCheckedSupplier(checkedSupplier);
	}
}
