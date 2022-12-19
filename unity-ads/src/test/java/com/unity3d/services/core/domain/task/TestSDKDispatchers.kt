package com.unity3d.services.core.domain.task

import com.unity3d.services.core.domain.ISDKDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
class TestSDKDispatchers(
    ioDispatcher: CoroutineDispatcher = TestCoroutineDispatcher(),
    defaultDispatcher: CoroutineDispatcher = TestCoroutineDispatcher(),
    mainDispatcher: CoroutineDispatcher = TestCoroutineDispatcher(),
): ISDKDispatchers {
    /**
     * Dispatcher for IO-bound work
     */
    override val io: CoroutineDispatcher = ioDispatcher

    /**
     * Dispatcher for standard work
     */
    override val default: CoroutineDispatcher = defaultDispatcher

    /**
     * Dispatcher for main thread
     */
    override val main: CoroutineDispatcher = mainDispatcher
}