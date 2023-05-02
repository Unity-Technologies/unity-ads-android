package com.unity3d.services.core.extensions

import com.unity3d.services.core.log.DeviceLog
import kotlinx.coroutines.delay

suspend fun <T> withRetry(
    retryDelay: Long = 5000,
    retries: Int = 6,
    scalingFactor: Double = 2.0,
    fallbackException: Exception,
    block: suspend (attempt: Int) -> T
): T {
    var nextDelay: Long = retryDelay

    repeat(retries) { attempt ->

        val result = runCatching { block(attempt) }

        when {
            result.isSuccess -> return result.getOrThrow()
            result.isFailure -> {
                // If we fail and want to abort retry, exit with the failureException itself (AbortRetryException)
                val failureException = result.exceptionOrNull()
                if (failureException is AbortRetryException) { throw failureException }

                // Check if we should retry again
                if (attempt+1 == retries) { throw fallbackException }

                DeviceLog.debug("Unity Ads init: retrying in $nextDelay milliseconds")
                delay(nextDelay)

                nextDelay = (retryDelay * scalingFactor).toLong()
            }
        }
    }
    // We should never hit here
    throw IllegalStateException("Unknown exception from withRetry")
}

class AbortRetryException(private val reason: String) : Exception(reason)