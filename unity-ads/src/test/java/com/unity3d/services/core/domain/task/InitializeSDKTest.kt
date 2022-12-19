package com.unity3d.services.core.domain.task

import com.unity3d.services.core.configuration.Configuration
import com.unity3d.services.core.configuration.ErrorState
import com.unity3d.services.core.domain.ISDKDispatchers
import com.unity3d.services.core.request.WebRequest
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class InitializeSDKTest {

    val dispatchers: ISDKDispatchers = TestSDKDispatchers()

    @MockK
    lateinit var configMock: Configuration

    @MockK
    lateinit var initializeStateLoadConfigFile: InitializeStateLoadConfigFile

    @MockK
    lateinit var initializeStateReset: InitializeStateReset

    @MockK
    lateinit var initializeStateError: InitializeStateError

    @MockK
    lateinit var initializeStateInitModules: InitializeStateInitModules

    @MockK
    lateinit var initializeStateConfig: InitializeStateConfig

    @MockK
    lateinit var initializeStateCreate: InitializeStateCreate

    @MockK
    lateinit var initializeStateLoadCache: InitializeStateLoadCache

    @MockK
    lateinit var initializeStateCreateWithRemote: InitializeStateCreateWithRemote

    @MockK
    lateinit var initializeStateLoadWeb: InitializeStateLoadWeb

    @MockK
    lateinit var initializeStateComplete: InitializeStateComplete

    @InjectMockKs
    lateinit var initializeSDK: InitializeSDK

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        defaultSuccessMocks()
        every { configMock.experiments.isNativeWebViewCacheEnabled } returns false
    }

    @Test
    fun doWork_everyStateSuccess_initCompleteCalledReturnSuccess() = runBlockingTest {
        // when
        val result = initializeSDK(EmptyParams)

        // then
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { initializeStateReset(InitializeStateReset.Params(configMock)) }
        coVerify(exactly = 0) { initializeStateError(any()) }
        coVerify(exactly = 1) { initializeStateComplete(any()) }
    }

    @Test
    fun doWork_failOnlyOnLoadConfigFile_returnSuccess() = runBlockingTest {
        // given
        coEvery { initializeStateLoadConfigFile(any()) } returns Result.failure(Exception())

        // when
        val result = initializeSDK(EmptyParams)

        // then
        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { initializeStateReset(InitializeStateReset.Params(configMock)) }
        coVerify(exactly = 0) { initializeStateError(any()) }
        coVerify(exactly = 1) { initializeStateComplete(any()) }
    }

    @Test
    fun doWork_failOnlyStateReset_returnFailure() = runBlockingTest {
        //given
        val exception = Exception("Reset failed on opening ConditionVariable")
        coEvery { initializeStateReset(any()) } returns Result.failure(exception)

        // when
        val result = initializeSDK(EmptyParams)

        // then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 1) { initializeStateReset(any()) }
        coVerify(exactly = 1) { initializeStateError(any()) }
        coVerify(exactly = 0) { initializeStateComplete(any()) }
    }

    @Test
    fun doWork_failOnlyInitModules_returnFailure() = runBlockingTest {
        //given
        val exception = Exception(ErrorState.InitModules.toString())
        coEvery { initializeStateInitModules(any()) } returns Result.failure(exception)

        // when
        val result = initializeSDK(EmptyParams)

        // then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 1) { initializeStateInitModules(any()) }
        coVerify(exactly = 1) { initializeStateError(any()) }
        coVerify(exactly = 0) { initializeStateComplete(any()) }
    }

    @Test
    fun doWork_failOnlyConfig_returnFailure() = runBlockingTest {
        //given
        val exception = InitializationException(
            ErrorState.NetworkConfigRequest,
            Exception("No connected events within the timeout!"),
           configMock
        )
        coEvery { initializeStateConfig(any()) } returns Result.failure(exception)

        // when
        val result = initializeSDK(EmptyParams)

        // then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 1) { initializeStateConfig(any()) }
        coVerify(exactly = 1) { initializeStateError(any()) }
        coVerify(exactly = 0) { initializeStateComplete(any()) }
    }

    @Test
    fun doWork_failOnlyCreateWithRemoteWithNativeWebViewCacheTrue_returnFailure() = runBlockingTest {
        //given
        mockkConstructor(Configuration::class)
        every { configMock.experiments.isNativeWebViewCacheEnabled } returns true
        val exception = InitializationException(ErrorState.CreateWebApp, Exception(), configMock)
        coEvery { initializeStateCreateWithRemote(any()) } returns Result.failure(exception)

        // when
        val result = initializeSDK(EmptyParams)

        // then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 1) { initializeStateCreateWithRemote(any()) }
        coVerify(exactly = 1) { initializeStateError(any()) }
        coVerify(exactly = 0) { initializeStateComplete(any()) }
    }

    @Test
    fun doWork_failOnlyLoadCacheWithNativeWebViewCacheFalse_returnFailure() = runBlockingTest {
        //given
        mockkConstructor(Configuration::class)
        every { anyConstructed<Configuration>().experiments.isNativeWebViewCacheEnabled } returns false
        val exception = Exception(ErrorState.LoadCache.toString())
        coEvery { initializeStateLoadCache(any()) } returns Result.failure(exception)

        // when
        val result = initializeSDK(EmptyParams)

        // then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 1) { initializeStateLoadCache(any()) }
        coVerify(exactly = 1) { initializeStateError(any()) }
        coVerify(exactly = 0) { initializeStateComplete(any()) }
    }

    @Test
    fun doWork_loadCacheReturnNullWithNativeWebViewCacheFalse_returnSuccess() = runBlockingTest {
        //given
        mockkConstructor(Configuration::class)
        every { anyConstructed<Configuration>().experiments.isNativeWebViewCacheEnabled } returns false
        coEvery { initializeStateLoadCache(any()) } returns Result.success(null)

        // when
        val result = initializeSDK(EmptyParams)

        // then
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { initializeStateLoadCache(any()) }
        coVerify(exactly = 1) { initializeStateLoadWeb(any()) }
        coVerify(exactly = 0) { initializeStateError(any()) }
        coVerify(exactly = 1) { initializeStateComplete(any()) }
    }

    @Test
    fun doWork_loadCacheReturnNullAndLoadWebFailsWithNativeWebViewCacheFalse_returnFailure() = runBlockingTest {
        //given
        mockkConstructor(Configuration::class)
        every { anyConstructed<Configuration>().experiments.isNativeWebViewCacheEnabled } returns false
        val webException = InitializationException(ErrorState.InvalidHash, Exception("Invalid webViewHash"), configMock)
        coEvery { initializeStateLoadCache(any()) } returns Result.success(null)
        coEvery { initializeStateLoadWeb(any()) } returns Result.failure(webException)

        // when
        val result = initializeSDK(EmptyParams)

        // then
        assertTrue(result.isFailure)
        assertEquals(webException, result.exceptionOrNull())
        coVerify(exactly = 1) { initializeStateLoadCache(any()) }
        coVerify(exactly = 1) { initializeStateLoadWeb(any()) }
        coVerify(exactly = 1) { initializeStateError(any()) }
        coVerify(exactly = 0) { initializeStateComplete(any()) }
    }

    @Test
    fun doWork_initializeStateCreateFailsAndLoadWebWithNativeWebViewCacheFalse_returnFailure() = runBlockingTest {
        //given
        mockkConstructor(Configuration::class)
        every { anyConstructed<Configuration>().experiments.isNativeWebViewCacheEnabled } returns false
        val exception = InitializationException(ErrorState.CreateWebApp, Exception(), configMock)
        coEvery { initializeStateCreate(any()) } returns Result.failure(exception)

        // when
        val result = initializeSDK(EmptyParams)

        // then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 1) { initializeStateError(any()) }
        coVerify(exactly = 0) { initializeStateComplete(any()) }
    }

    @Test
    fun doWork_initializeStateCompleteFailsAndLoadWebWithNativeWebViewCacheFalse_returnFailure() = runBlockingTest {
        //given
        mockkConstructor(Configuration::class)
        every { anyConstructed<Configuration>().experiments.isNativeWebViewCacheEnabled } returns false
        val exception = Exception()
        coEvery { initializeStateComplete(any()) } returns Result.failure(exception)

        // when
        val result = initializeSDK(EmptyParams)

        // then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 0) { initializeStateError(any()) }
        coVerify(exactly = 1) { initializeStateComplete(any()) }
    }

    @Test
    fun doWork_initializeStateCompleteFailsAndLoadWebWithNativeWebViewCacheTrue_returnFailure() = runBlockingTest {
        //given
        mockkConstructor(Configuration::class)
        every { anyConstructed<Configuration>().experiments.isNativeWebViewCacheEnabled } returns true
        val exception = Exception()
        coEvery { initializeStateComplete(any()) } returns Result.failure(exception)

        // when
        val result = initializeSDK(EmptyParams)

        // then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 0) { initializeStateError(any()) }
        coVerify(exactly = 1) { initializeStateComplete(any()) }
    }

    private fun defaultSuccessMocks() {
        coEvery { initializeStateLoadConfigFile(any()) } returns Result.success(configMock)
        coEvery { initializeStateReset(any()) } returns Result.success(configMock)
        coEvery { initializeStateInitModules(any()) } returns Result.success(configMock)
        coEvery { initializeStateError(any()) } returns Result.success(Unit)
        coEvery { initializeStateConfig(any()) } returns Result.success(configMock)
        coEvery { initializeStateCreate(any()) } returns Result.success(configMock)
        coEvery { initializeStateLoadCache(any()) } returns Result.success("")
        coEvery { initializeStateCreateWithRemote(any()) } returns Result.success(configMock)
        coEvery { initializeStateLoadWeb(any()) } returns Result.success(
            InitializeStateLoadWeb.LoadWebResult(
                configMock,
                ""
            )
        )
        coEvery { initializeStateComplete(any()) } returns Result.success(Unit)
    }
}