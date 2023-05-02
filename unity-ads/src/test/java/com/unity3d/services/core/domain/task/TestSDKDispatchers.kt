package com.unity3d.services.core.domain.task

import com.unity3d.services.core.domain.ISDKDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler

@OptIn(ExperimentalCoroutinesApi::class)
class TestSDKDispatchers(
    testCoroutineScheduler: TestCoroutineScheduler = TestCoroutineScheduler(),
    ioDispatcher: CoroutineDispatcher = StandardTestDispatcher(testCoroutineScheduler, "io"),
    defaultDispatcher: CoroutineDispatcher = StandardTestDispatcher(testCoroutineScheduler, "default"),
    mainDispatcher: CoroutineDispatcher = StandardTestDispatcher(testCoroutineScheduler, "main")
) : ISDKDispatchers {
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