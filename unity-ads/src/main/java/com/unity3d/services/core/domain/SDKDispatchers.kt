package com.unity3d.services.core.domain

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * A wrapper class for common coroutine dispatchers.
 * An instance of this can be injected to classes which are concerned about executing code
 * on different threads, but they don't need to know about the underlying implementation.
 */
class SDKDispatchers: ISDKDispatchers {
    /**
     * Dispatcher for IO-bound work
     */
    override val io: CoroutineDispatcher = Dispatchers.IO

    /**
     * Dispatcher for standard work
     */
    override val default: CoroutineDispatcher = Dispatchers.Default

    /**
     * Dispatcher for main thread
     */
    override val main: CoroutineDispatcher = Dispatchers.Main
}