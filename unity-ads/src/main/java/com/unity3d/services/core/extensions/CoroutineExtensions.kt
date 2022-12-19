package com.unity3d.services.core.extensions

import kotlin.coroutines.cancellation.CancellationException

/**
 * Stops runCatching from swallowing [CancellationException] and messing up structured concurrency
 */
inline fun <R> runSuspendCatching(block: () -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (c: CancellationException) {
        throw c
    } catch (e: Throwable) {
        Result.failure(e)
    }
}

inline fun <R> runReturnSuspendCatching(block: () -> R): Result<R> {
    return runSuspendCatching(block)
        .onSuccess { return Result.success(it) }
        .onFailure { return Result.failure(it) }
}