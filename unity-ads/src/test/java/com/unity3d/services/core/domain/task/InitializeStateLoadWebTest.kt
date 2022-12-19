package com.unity3d.services.core.domain.task

import com.unity3d.services.core.configuration.Configuration
import com.unity3d.services.core.configuration.ErrorState
import com.unity3d.services.core.domain.getCustomExceptionOrNull
import com.unity3d.services.core.misc.Utilities
import com.unity3d.services.core.properties.SdkProperties
import com.unity3d.services.core.request.WebRequest
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InitializeStateLoadWebTest {
    // Injected into InitializeStateConfig constructor
    var dispatchers : TestSDKDispatchers = TestSDKDispatchers()

    @MockK
    lateinit var configMock: Configuration

    @MockK
    lateinit var initializeStateNetworkError: InitializeStateNetworkError

    @InjectMockKs
    lateinit var initializeStateLoadWeb: InitializeStateLoadWeb

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        every { configMock.webViewUrl } returns "http://mockUrl"
        every { configMock.maxRetries } returns 1
        every { configMock.retryScalingFactor } returns 1.0
        every { configMock.retryDelay } returns 1
    }

    @Test
    fun doWork_firstRequestPassWithData_resultIncludeDataSuccess() = runBlockingTest {
        mockkStatic(Utilities::class, SdkProperties::class) {
            mockkClass(WebRequest::class) {
                // given
                mockkConstructor(WebRequest::class)
                every { Utilities.writeFile(any(), any()) } returns true
                every { SdkProperties.getLocalWebViewFile() } returns ""
                every { anyConstructed<WebRequest>().makeRequest() } returns TESTDATA
                every { configMock.webViewHash } returns TESTHASH

                // when
                val loadWebResult = initializeStateLoadWeb(
                    InitializeStateLoadWeb.Params(configMock)
                )

                // then
                Assert.assertTrue(loadWebResult.isSuccess)
                Assert.assertEquals(TESTDATA, loadWebResult.getOrNull()?.webViewDataString)
                Assert.assertEquals(configMock, loadWebResult.getOrNull()?.config)
            }
        }
    }

    @Test
    fun doWork_firstRequestPassWithDataButHashMismatch_failureWithErrorHash() = runBlockingTest {
        mockkStatic(SdkProperties::class) {
            mockkClass(WebRequest::class) {
                // given
                mockkConstructor(WebRequest::class)
                every { SdkProperties.getLocalWebViewFile() } returns ""
                every { anyConstructed<WebRequest>().makeRequest() } returns TESTDATA
                every { configMock.webViewHash } returns "wrong"

                // when
                val loadWebResult = initializeStateLoadWeb(
                    InitializeStateLoadWeb.Params(configMock)
                )

                // then
                Assert.assertTrue(loadWebResult.isFailure)
                Assert.assertEquals(ErrorState.InvalidHash, loadWebResult.getCustomExceptionOrNull<InitializationException>()?.errorState)
            }
        }
    }

    @Test
    fun doWork_requestThrowsAndNetworkFails_failsWithRetryAndNetworkRetryThrows() = runBlockingTest {
        mockkStatic(Utilities::class, SdkProperties::class) {
            mockkClass(WebRequest::class) {
                // given
                mockkConstructor(WebRequest::class)
                every { Utilities.writeFile(any(), any()) } returns true
                every { SdkProperties.getLocalWebViewFile() } returns ""
                every { anyConstructed<WebRequest>().makeRequest() } throws Exception()
                every { configMock.webViewHash } returns TESTHASH
                coEvery { initializeStateNetworkError(any()) } returns Result.failure(Exception("Network Error"))

                // when
                val loadWebResult = initializeStateLoadWeb(
                    InitializeStateLoadWeb.Params(configMock)
                )

                // then
                Assert.assertTrue(loadWebResult.isFailure)
                Assert.assertEquals(ErrorState.NetworkWebviewRequest, loadWebResult.getCustomExceptionOrNull<InitializationException>()?.errorState)
                verify(exactly = 1) { anyConstructed<WebRequest>().makeRequest() }
                coVerify(exactly = 1) { initializeStateNetworkError(any()) }
            }
        }
    }

    @Test
    fun doWork_requestThrowsAndNetworkComesBack_failsWithRetryButNetworkRetryOnce() = runBlockingTest {
        mockkStatic(Utilities::class, SdkProperties::class) {
            mockkClass(WebRequest::class) {
                // given
                mockkConstructor(WebRequest::class)
                every { Utilities.writeFile(any(), any()) } returns true
                every { SdkProperties.getLocalWebViewFile() } returns ""
                every { anyConstructed<WebRequest>().makeRequest() } throws Exception()
                every { configMock.webViewHash } returns TESTHASH
                coEvery { initializeStateNetworkError(any()) } returns Result.success(Unit)

                // when
                val loadWebResult = initializeStateLoadWeb(
                    InitializeStateLoadWeb.Params(configMock)
                )

                // then
                Assert.assertTrue(loadWebResult.isFailure) // Failure still cause can't mock second request being success.
                verify(exactly = 2) { anyConstructed<WebRequest>().makeRequest() }
                coVerify(exactly = 1) { initializeStateNetworkError(any()) }
            }
        }
    }

    companion object {
        const val TESTDATA: String = "testData"
        const val TESTHASH: String = "ba477a0ac57e10dd90bb5bf0289c5990fe839c619b26fde7c2aac62f526d4113"
    }
}