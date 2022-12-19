package com.unity3d.services.core.domain.task

import com.unity3d.services.core.configuration.Configuration
import com.unity3d.services.core.configuration.ErrorState
import com.unity3d.services.core.webview.WebViewApp
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class InitializeStateCreateWithRemoteTest {
    // Injected into InitializeStateConfig constructor
    var dispatchers: TestSDKDispatchers = TestSDKDispatchers()

    @MockK
    lateinit var configMock: Configuration

    @InjectMockKs
    lateinit var initializeSateCreateWithRemote: InitializeStateCreateWithRemote

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
    }

    @Test
    fun doWork_webViewAppCreateSuccess_successWithConfig() = runBlockingTest {

        mockkStatic(WebViewApp::class) {
            // given
            every { WebViewApp.create(any(), true) } returns null

            // when
            val result: Result<Configuration> =
                initializeSateCreateWithRemote(InitializeStateCreateWithRemote.Params(configMock))

            // then
            assertTrue(result.isSuccess)
            assertEquals(configMock, result.getOrThrow())
            coVerify(exactly = 1) { WebViewApp.create(any(), true) }
        }
    }

    @Test
    fun doWork_webViewAppCreateFails_failsWithCustomExceptionMessage() = runBlockingTest {
        mockkStatic(WebViewApp::class) {
            // given
            every {
                WebViewApp.getCurrentApp().webAppFailureMessage
            } returns "Webview failed to initialize"
            every { WebViewApp.getCurrentApp().errorStateFromWebAppCode } returns ErrorState.CreateWebview
            every { WebViewApp.create(any(), true) } returns ErrorState.CreateWebview

            // when
            val result: Result<Configuration> =
                initializeSateCreateWithRemote(InitializeStateCreateWithRemote.Params(configMock))

            // then
            assertTrue(result.isFailure)
            val exception =
                assertFailsWith<InitializationException> {
                    result.getOrThrow()
                }
            assertEquals(ErrorState.CreateWebview, exception.errorState)
            assertEquals("Webview failed to initialize", exception.originalException.message)
            assertEquals(configMock, exception.config)
            coVerify(exactly = 1) { WebViewApp.create(any(), true) }
        }
    }

    @Test
    fun doWork_webViewAppCreateFails_failsWithDefaultExceptionMessage() = runBlockingTest {
        mockkStatic(WebViewApp::class) {
            // given
            every {
                WebViewApp.getCurrentApp().webAppFailureMessage
            } returns null
            every { WebViewApp.getCurrentApp().errorStateFromWebAppCode } returns ErrorState.CreateWebview
            every { WebViewApp.create(any(), true) } returns ErrorState.CreateWebview

            // when
            val result: Result<Configuration> =
                initializeSateCreateWithRemote(InitializeStateCreateWithRemote.Params(configMock))

            // then
            assertTrue(result.isFailure)
            val exception =
                assertFailsWith<InitializationException> {
                    result.getOrThrow()
                }
            assertEquals(ErrorState.CreateWebview, exception.errorState)
            assertEquals("Unity Ads WebApp creation failed", exception.originalException.message)
            assertEquals(configMock, exception.config)
            coVerify(exactly = 1) { WebViewApp.create(any(), true) }
        }
    }

}