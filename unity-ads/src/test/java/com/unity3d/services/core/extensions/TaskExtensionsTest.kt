package com.unity3d.services.core.extensions

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskExtensionsTest {

    @Test
    fun withRetry_retriesAlwaysThrows_failsWithFallback() = runTest {
        // given
        var counter = 0
        val functionBlock = {
            counter++
            throw Exception()
        }

        // when
        val result = runCatching {
            withRetry(
                retries = 10,
                retryDelay = 1,
                scalingFactor = 1.0,
                fallbackException = Exception("fallback")
            ) {
                functionBlock()
            }
        }

        // then
        Assert.assertTrue(result.isFailure)
        Assert.assertEquals("fallback", result.exceptionOrNull()?.message)
        Assert.assertEquals(10, counter)
    }

    @Test
    fun withRetry_retriesSucceedsAfter5Attempts_succeedAfterRetries() = runTest {
        // given
        var counter = 0
        val functionBlock = {
            counter++
            if (counter < 5) {
                throw Exception()
            }
        }

        // when
        val result = runCatching {
            withRetry(
                fallbackException = Exception("fallback")
            ) {
                functionBlock()
            }
        }

        // then
        Assert.assertTrue(result.isSuccess)
        Assert.assertEquals(5, counter)
    }

    @Test
    fun withRetry_succeedsOnFirstAttempts_succeedWithoutRetries() = runTest {
        // given
        var counter = 0
        val functionBlock = { counter++ }

        // when
        val result = runCatching {
            withRetry(
                fallbackException = Exception("fallback")
            ) {
                functionBlock()
            }
        }

        // then
        Assert.assertTrue(result.isSuccess)
        Assert.assertEquals(1, counter)
    }

    @Test
    fun withRetry_throwsAbortRetry_failsWithoutRetries() = runTest {
        // given
        val functionBlock = { throw AbortRetryException("") }

        // when
        val result = runCatching {
            withRetry(
                fallbackException = Exception("fallback")
            ) {
                functionBlock()
            }
        }

        // then
        Assert.assertTrue(result.isFailure)
        Assert.assertTrue(result.exceptionOrNull() is AbortRetryException)
    }
}