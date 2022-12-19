package com.unity3d.services.core.domain.task

import com.unity3d.services.core.configuration.Configuration
import com.unity3d.services.core.connectivity.ConnectivityMonitor
import com.unity3d.services.core.connectivity.IConnectivityListener
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class InitializeStateNetworkErrorTest {
    var dispatcher = TestCoroutineDispatcher()

    var dispatchers = TestSDKDispatchers(dispatcher, dispatcher, dispatcher)

    private val testScope = TestCoroutineScope(dispatcher)

    @MockK
    lateinit var configMock: Configuration

    var initializeStateNetworkError: InitializeStateNetworkError = InitializeStateNetworkError(dispatchers)

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        every { configMock.connectedEventThreshold } returns 1
        every { configMock.maximumConnectedEvents } returns 1
    }

    @Test
    fun doWork_connectivityConnects_success()  {
        testScope.runBlockingTest {
            mockkStatic(ConnectivityMonitor::class) {
                // given
                every { configMock.networkErrorTimeout } returns 20000
                every { ConnectivityMonitor.addListener(any())} answers {
                    val listener : IConnectivityListener = firstArg()
                    listener.onConnected()
                }

                // when
                val job = async {initializeStateNetworkError(InitializeStateNetworkError.Params(configMock))}
                val result = job.await()

                // then
                verify(exactly = 0) { ConnectivityMonitor.removeListener(any()) }
                assertTrue(result.isSuccess)
            }
        }
    }

    @Test
    fun doWork_connectivityTimesOut_failureWithException()  {
        testScope.runBlockingTest {
            mockkStatic(ConnectivityMonitor::class) {
                // given
                every { configMock.networkErrorTimeout } returns 200
                every { ConnectivityMonitor.addListener(any()) } answers {
                    advanceTimeBy(300)
                }
                // when
                val result = initializeStateNetworkError(InitializeStateNetworkError.Params(configMock))

                // then
                verify(exactly = 1) { ConnectivityMonitor.removeListener(any()) }
                assertTrue(result.isFailure)
                assertNotNull(result.exceptionOrNull())
                assertEquals("No connected events within the timeout!", result.exceptionOrNull()?.message)

            }
        }
    }
}