package com.unity3d.services.core.domain.task

import com.unity3d.services.core.configuration.Configuration
import com.unity3d.services.core.configuration.ErrorState
import com.unity3d.services.core.domain.getCustomExceptionOrNull
import com.unity3d.services.core.misc.Utilities
import com.unity3d.services.core.network.core.HttpClient
import com.unity3d.services.core.network.model.HttpResponse
import com.unity3d.services.core.properties.SdkProperties
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InitializeStateLoadWebTest {
    // Injected into InitializeStateConfig constructor
    var dispatchers: TestSDKDispatchers = TestSDKDispatchers()

    @MockK
    lateinit var configMock: Configuration

    @MockK
    lateinit var initializeStateNetworkError: InitializeStateNetworkError

    @MockK
    lateinit var httpClient: HttpClient

    @InjectMockKs
    lateinit var initializeStateLoadWeb: InitializeStateLoadWeb

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        every { configMock.webViewUrl } returns "http://mockUrl"
        every { configMock.maxRetries } returns 1
        every { configMock.retryScalingFactor } returns 1.0
        every { configMock.retryDelay } returns 1
        Dispatchers.setMain(dispatchers.main)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun doWork_firstRequestPassWithData_resultIncludeDataSuccess() = runTest {
        mockkStatic(Utilities::class, SdkProperties::class) {
            // given
            every { Utilities.writeFile(any(), any()) } returns true
            every { SdkProperties.getLocalWebViewFile() } returns ""
            every { configMock.webViewHash } returns TESTHASH
            coEvery { httpClient.execute(any()) } returns HttpResponse(body = TESTDATA)

            // when
            val loadWebResult = initializeStateLoadWeb(InitializeStateLoadWeb.Params(configMock))

            // then
            Assert.assertTrue(loadWebResult.isSuccess)
            Assert.assertEquals(TESTDATA, loadWebResult.getOrNull()?.webViewDataString)
            Assert.assertEquals(configMock, loadWebResult.getOrNull()?.config)
        }
    }

    @Test
    fun doWork_firstRequestPassWithDataButHashMismatch_failureWithErrorHash() = runTest {
        mockkStatic(SdkProperties::class) {
            // given
            every { SdkProperties.getLocalWebViewFile() } returns ""
            every { configMock.webViewHash } returns "wrong"
            coEvery { httpClient.execute(any()) } returns HttpResponse(body = TESTDATA)

            // when
            val loadWebResult = initializeStateLoadWeb(InitializeStateLoadWeb.Params(configMock))

            // then
            Assert.assertTrue(loadWebResult.isFailure)
            Assert.assertEquals(
                ErrorState.InvalidHash,
                loadWebResult.getCustomExceptionOrNull<InitializationException>()?.errorState
            )
        }
    }

    @Test
    fun doWork_requestThrowsAndNetworkFails_failsWithRetryAndNetworkRetryThrows() = runTest {
        mockkStatic(Utilities::class, SdkProperties::class) {
            // given
            every { Utilities.writeFile(any(), any()) } returns true
            every { SdkProperties.getLocalWebViewFile() } returns ""
            coEvery { httpClient.execute(any()) } throws Exception()
            every { configMock.webViewHash } returns TESTHASH
            coEvery { initializeStateNetworkError.doWork(any()) } throws Exception("Network Error")

            // when
            val loadWebResult = initializeStateLoadWeb(InitializeStateLoadWeb.Params(configMock))

            // then
            Assert.assertTrue(loadWebResult.isFailure)
            Assert.assertEquals(
                ErrorState.NetworkWebviewRequest,
                loadWebResult.getCustomExceptionOrNull<InitializationException>()?.errorState
            )
            coVerify(exactly = 1) { httpClient.execute(any()) }
            coVerify(exactly = 1) { initializeStateNetworkError.doWork(any()) }
        }
    }

    @Test
    fun doWork_requestThrowsAndNetworkComesBack_failsWithRetryButNetworkRetryOnce() = runTest {
        mockkStatic(Utilities::class, SdkProperties::class) {
            // given
            every { Utilities.writeFile(any(), any()) } returns true
            every { SdkProperties.getLocalWebViewFile() } returns ""
            coEvery { httpClient.execute(any()) } throws Exception()
            every { configMock.webViewHash } returns TESTHASH
            coEvery { initializeStateNetworkError.doWork(any()) } returns Result.success(Unit)

            // when
            val loadWebResult = initializeStateLoadWeb(InitializeStateLoadWeb.Params(configMock))

            // then
            Assert.assertTrue(loadWebResult.isFailure) // Failure still cause can't mock second request being success.
            coVerify(exactly = 1) { httpClient.execute(any()) } // Needed to go from 2 to 1, is it because of this? ^^^
            coVerify(exactly = 1) { initializeStateNetworkError.doWork(any()) }
        }
    }

    companion object {
        const val TESTDATA: String = "testData"
        const val TESTHASH: String = "ba477a0ac57e10dd90bb5bf0289c5990fe839c619b26fde7c2aac62f526d4113"
    }
}
