package com.unity3d.services.core.domain

import com.unity3d.services.core.domain.task.InitializationException
import java.lang.IllegalArgumentException

inline fun <reified E: Exception> Result<*>.getCustomExceptionOrNull(): E? {
    val exception = this.exceptionOrNull()
    return if (exception is E) {
        exception
    } else {
        null
    }
}

inline fun <reified E: Exception> Result<*>.getCustomExceptionOrThrow(): E {
    val exception = this.exceptionOrNull()
    return if (exception is E) {
        exception
    } else {
        throw IllegalArgumentException("Wrong Exception type found")
    }
}

fun Result<*>.getInitializationExceptionOrNull(): InitializationException? {
    val exception = this.exceptionOrNull()
    return if (exception is InitializationException) {
        exception
    } else {
        null
    }
}

fun Result<*>.getInitializationExceptionOrThrow(): InitializationException {
    val exception = this.exceptionOrNull()
    return if (exception is InitializationException) {
        exception
    } else {
        throw IllegalArgumentException("Wrong Exception type found")
    }
}