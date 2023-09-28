package com.unity3d.services.core.domain.task

import com.unity3d.services.core.configuration.Configuration
import com.unity3d.services.core.connectivity.ConnectivityMonitor
import com.unity3d.services.core.connectivity.IConnectivityListener
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class InitializeStateNetworkErrorTest {
    var dispatchers = TestSDKDispatchers()

    @MockK
    lateinit var configMock: Configuration

    @InjectMockKs // Had to change because was not re-instanciating the class after each test
    lateinit var initializeStateNetworkError: InitializeStateNetworkError

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        every { configMock.connectedEventThreshold } returns 1
        every { configMock.maximumConnectedEvents } returns 1
        Dispatchers.setMain(dispatchers.main)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun doWork_connectivityConnects_success() = runTest {
        mockkStatic(ConnectivityMonitor::class) {
            // given
            every { configMock.networkErrorTimeout } returns 20000
            every { ConnectivityMonitor.addListener(any()) } answers {
                val listener: IConnectivityListener = firstArg()
                listener.onConnected()
            }

            // when
            val job =
                async { runCatching { initializeStateNetworkError(InitializeStateNetworkError.Params(configMock)) } }
            val result = job.await()

            // then
            verify(exactly = 0) { ConnectivityMonitor.removeListener(any()) }
            assertTrue(result.isSuccess)
        }
    }

    @Test
    fun doWork_connectivityTimesOut_failureWithException() = runTest {
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