package com.unity3d.services.core.domain.task

import com.unity3d.services.core.configuration.Configuration
import com.unity3d.services.core.configuration.ErrorState
import com.unity3d.services.core.domain.ISDKDispatchers
import com.unity3d.utils.corountine.CoroutineNameUsed
import com.unity3d.utils.corountine.coroutineNameIsUsed
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class InitializeSDKTest {
    val dispatchers: ISDKDispatchers = TestSDKDispatchers()

    @MockK
    lateinit var configMock: Configuration

    @MockK
    lateinit var configFileFromLocalStorage: ConfigFileFromLocalStorage

    @MockK
    lateinit var initializeStateReset: InitializeStateReset

    @MockK
    lateinit var initializeStateError: InitializeStateError

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
        every { configMock.experiments.isNativeWebViewCacheEnabled } returns false
        every { configMock.experiments.isWebViewAsyncDownloadEnabled } returns false
        Dispatchers.setMain(dispatchers.main)
        defaultSuccessMocks()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun doWork_everyStateSuccess_initCompleteCalledReturnSuccess() = runTest {
        // when
        val result = runCatching { initializeSDK(EmptyParams) }

        // then
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { initializeStateReset.doWork(InitializeStateReset.Params(configMock)) }
        coVerify(exactly = 0) { initializeStateError.doWork(any()) }
        coVerify(exactly = 1) { initializeStateComplete.doWork(any()) }
    }

    @Test
    fun doWork_failOnlyOnLoadConfigFile_returnSuccess() = runTest {
        // given
        coEvery { configFileFromLocalStorage(any()) } throws Exception()

        // when
        val result = runCatching { initializeSDK(EmptyParams) }

        // then
        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { initializeStateReset.doWork(InitializeStateReset.Params(configMock)) }
        coVerify(exactly = 0) { initializeStateError.doWork(any()) }
        coVerify(exactly = 1) { initializeStateComplete.doWork(any()) }
    }

    @Test
    fun doWork_failOnlyStateReset_returnFailure() = runTest {
        //given
        val exception = Exception("Reset failed on opening ConditionVariable")
        coEvery { initializeStateReset(any()) } throws exception

        // when
        val result = runCatching { initializeSDK(EmptyParams) }

        // then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull()?.cause)
        coVerify(exactly = 1) { initializeStateReset.doWork(any()) }
        coVerify(exactly = 1) { initializeStateError.doWork(any()) }
        coVerify(exactly = 0) { initializeStateComplete.doWork(any()) }
    }

    @Test
    fun doWork_failOnlyConfig_returnFailure() = runTest {
        //given
        val exception = InitializationException(
            ErrorState.NetworkConfigRequest,
            Exception("No connected events within the timeout!"),
            configMock
        )
        coEvery { initializeStateConfig(any()) } throws exception

        // when
        val result = runCatching { initializeSDK(EmptyParams) }

        // then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 1) { initializeStateConfig.doWork(any()) }
        coVerify(exactly = 1) { initializeStateError.doWork(any()) }
        coVerify(exactly = 0) { initializeStateComplete.doWork(any()) }
    }

    @Test
    fun doWork_failOnlyCreateWithRemoteWithNativeWebViewCacheTrue_returnFailure() = runTest {
        //given
        every { configMock.experiments.isNativeWebViewCacheEnabled } returns true
        val exception = InitializationException(ErrorState.CreateWebApp, Exception(), configMock)
        coEvery { initializeStateCreateWithRemote(any()) } throws exception

        // when
        val result = runCatching { initializeSDK(EmptyParams) }

        // then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 1) { initializeStateCreateWithRemote.doWork(any()) }
        coVerify(exactly = 1) { initializeStateError.doWork(any()) }
        coVerify(exactly = 0) { initializeStateComplete.doWork(any()) }
    }

    @Test
    fun doWork_failOnlyLoadCacheWithNativeWebViewCacheFalse_returnFailure() = runTest {
        //given
        val exception = Exception(ErrorState.LoadCache.toString())
        coEvery { initializeStateLoadCache(any()) } throws exception

        // when
        val result = runCatching { initializeSDK(EmptyParams) }

        // then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull()?.cause)
        coVerify(exactly = 1) { initializeStateLoadCache.doWork(any()) }
        coVerify(exactly = 1) { initializeStateError.doWork(any()) }
        coVerify(exactly = 0) { initializeStateComplete.doWork(any()) }
    }

    @Test
    fun doWork_loadCacheReturnNullWithNativeWebViewCacheFalse_returnSuccessWithWebViewDownloadSameContext() = runTest {
        withContext(CoroutineNameUsed("LaunchLoadWeb")) {
            //given
            mockkConstructor(Configuration::class)
            every { configMock.experiments.isWebViewAsyncDownloadEnabled } returns true
            every { anyConstructed<Configuration>().experiments.isNativeWebViewCacheEnabled } returns false
            coEvery { initializeStateLoadCache.doWork(any()) } returns InitializeStateLoadCache.LoadCacheResult(
                true,
                null
            )

            // when
            val result = runCatching { initializeSDK(EmptyParams) }

            // then
            assertTrue(result.isSuccess)
            assertFalse(coroutineNameIsUsed(), "WebView download started a new context but expected to run in the original context.")
            coVerify(exactly = 1) { initializeStateLoadCache.doWork(any()) }
            coVerify(exactly = 1) { initializeStateLoadWeb.doWork(any()) }
            coVerify(exactly = 0) { initializeStateError.doWork(any()) }
            coVerify(exactly = 1) { initializeStateComplete.doWork(any()) }
        }
    }

    @Test
    fun doWork_loadCacheReturnNullAndLoadWebFailsWithNativeWebViewCacheFalse_returnFailure() = runTest {
        //given
        val webException = InitializationException(ErrorState.InvalidHash, Exception("Invalid webViewHash"), configMock)
        coEvery { initializeStateLoadCache.doWork(any()) } returns InitializeStateLoadCache.LoadCacheResult(true, null)
        coEvery { initializeStateLoadWeb.doWork(any()) } throws webException

        // when
        val result = runCatching { initializeSDK(EmptyParams) }

        // then
        assertTrue(result.isFailure)
        assertEquals(webException, result.exceptionOrNull())
        coVerify(exactly = 1) { initializeStateLoadCache.doWork(any()) }
        coVerify(exactly = 1) { initializeStateLoadWeb.doWork(any()) }
        coVerify(exactly = 1) { initializeStateError.doWork(any()) }
        coVerify(exactly = 0) { initializeStateComplete.doWork(any()) }
    }

    @Test
    fun doWork_loadCacheReturnDataWithMismatchWithNativeWebViewCacheFalse_returnSuccessWithAsyncWebViewDownload() = runTest {
        withContext(CoroutineNameUsed("LaunchLoadWeb")) {

            //given
            mockkConstructor(Configuration::class)
            every { anyConstructed<Configuration>().experiments.isNativeWebViewCacheEnabled } returns false
            every { configMock.experiments.isWebViewAsyncDownloadEnabled } returns true
            coEvery { initializeStateLoadCache.doWork(any()) } returns InitializeStateLoadCache.LoadCacheResult(
                true,
                "WebView HTML Data"
            )

            // when
            val result = runCatching { initializeSDK(EmptyParams) }

            // then
            assertTrue(result.isSuccess)
            assertTrue(coroutineNameIsUsed(), "WebView download didn't run in a new coroutine context but was expected to.")
            coVerify(exactly = 1) { initializeStateLoadCache.doWork(any()) }
            coVerify(exactly = 1) { initializeStateLoadWeb.doWork(any()) }
            coVerify(exactly = 0) { initializeStateError.doWork(any()) }
            coVerify(exactly = 1) { initializeStateComplete.doWork(any()) }

        }
    }

    @Test
    fun doWork_initializeStateCreateFailsAndLoadWebWithNativeWebViewCacheFalse_returnFailure() = runTest {
        //given
        val exception = InitializationException(ErrorState.CreateWebApp, Exception(), configMock)
        coEvery { initializeStateCreate(any()) } throws exception

        // when
        val result = runCatching { initializeSDK(EmptyParams) }

        // then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 1) { initializeStateError.doWork(any()) }
        coVerify(exactly = 0) { initializeStateComplete.doWork(any()) }
    }

    @Test
    fun doWork_initializeStateCompleteFailsAndLoadWebWithNativeWebViewCacheFalse_returnFailure() = runTest {
        //given
        val exception = Exception()
        coEvery { initializeStateComplete(any()) } throws exception

        // when
        val result = runCatching { initializeSDK(EmptyParams) }

        // then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull()?.cause)
        coVerify(exactly = 0) { initializeStateError.doWork(any()) }
        coVerify(exactly = 1) { initializeStateComplete.doWork(any()) }
    }

    @Test
    fun doWork_initializeStateCompleteFailsAndLoadWebWithNativeWebViewCacheTrue_returnFailure() = runTest {
        //given
        val exception = Exception()
        coEvery { initializeStateComplete(any()) } throws exception

        // when
        val result = runCatching { initializeSDK(EmptyParams) }

        // then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull()?.cause)
        coVerify(exactly = 0) { initializeStateError.doWork(any()) }
        coVerify(exactly = 1) { initializeStateComplete.doWork(any()) }
    }

    private fun defaultSuccessMocks() {
        coEvery { configFileFromLocalStorage.doWork(any()) } returns configMock
        coEvery { configFileFromLocalStorage.getMetricName() } returns "loadConfigFile"
        coEvery { initializeStateReset.doWork(any()) } returns configMock
        coEvery { initializeStateReset.getMetricName() } returns "reset"
        coEvery { initializeStateError.doWork(any()) } returns Unit
        coEvery { initializeStateError.getMetricName() } returns "error"
        coEvery { initializeStateConfig.doWork(any()) } returns configMock
        coEvery { initializeStateConfig.getMetricName() } returns "config"
        coEvery { initializeStateCreate.doWork(any()) } returns configMock
        coEvery { initializeStateCreate.getMetricName() } returns "create"
        coEvery { initializeStateLoadCache.doWork(any()) } returns InitializeStateLoadCache.LoadCacheResult(false, "")
        coEvery { initializeStateLoadCache.getMetricName() } returns "loadCache"
        coEvery { initializeStateCreateWithRemote.doWork(any()) } returns configMock
        coEvery { initializeStateCreateWithRemote.getMetricName() } returns "createWithRemote"
        coEvery { initializeStateLoadWeb.doWork(any()) } returns InitializeStateLoadWeb.LoadWebResult(configMock, "")
        coEvery { initializeStateLoadWeb.getMetricName() } returns "loadWeb"
        coEvery { initializeStateComplete.doWork(any()) } returns Unit
        coEvery { initializeStateComplete.getMetricName() } returns "complete"
    }
}
