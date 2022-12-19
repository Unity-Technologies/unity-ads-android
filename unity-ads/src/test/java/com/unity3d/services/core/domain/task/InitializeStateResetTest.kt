package com.unity3d.services.core.domain.task

import com.unity3d.services.core.configuration.Configuration
import com.unity3d.services.core.configuration.IModuleConfiguration
import com.unity3d.services.core.properties.SdkProperties
import com.unity3d.services.core.webview.WebView
import com.unity3d.services.core.webview.WebViewApp
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class InitializeStateResetTest {
    var dispatcher = TestCoroutineDispatcher()

    var dispatchers = TestSDKDispatchers(dispatcher, dispatcher, dispatcher)

    private val testScope = TestCoroutineScope(dispatcher)

    @MockK
    lateinit var moduleConfigurationMock : IModuleConfiguration

    @MockK
    lateinit var webViewAppMock : WebViewApp

    @MockK
    lateinit var webViewMock : WebView

    @MockK
    lateinit var configMock : Configuration

    var initializeStateReset: InitializeStateReset = InitializeStateReset(dispatchers)

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        every { configMock.moduleConfigurationList } returns arrayOf("mockModule")
        every { configMock.webViewAppCreateTimeout } returns 200
        every { moduleConfigurationMock.initCompleteState(any()) } returns true
        every { moduleConfigurationMock.resetState(any()) } returns true
    }

    @Test
    fun doWork_webViewAppReset_success() = runBlockingTest {
        mockkStatic(WebViewApp::class) {
            mockkStatic(SdkProperties::class) {
                // given
                every {WebViewApp.getCurrentApp()} returns webViewAppMock
                every {webViewAppMock.webView} returns webViewMock
                every {configMock.getModuleConfiguration(any()) } returns null
                every {SdkProperties.getCacheDirectory(any())} returns mockkClass(File::class)

                // when
                val result = initializeStateReset(InitializeStateReset.Params(configMock))

                // then
                verify (exactly = 1) { webViewAppMock.resetWebViewAppInitialization() }
                verify (exactly = 1) { SdkProperties.setInitialized(false) }
                assertTrue(result.isSuccess)
            }
        }
    }

    @Test
    fun doWork_webViewAppTimesOut_failureWithException()  {
        testScope.runBlockingTest {
            mockkStatic(WebViewApp::class) {
                mockkStatic(SdkProperties::class) {
                    // given
                    every { WebViewApp.getCurrentApp() } returns webViewAppMock
                    every { webViewAppMock.webView } returns webViewMock
                    every { webViewMock.destroy() } answers {
                        advanceTimeBy(300)
                    }
                    // when
                    val result = initializeStateReset(InitializeStateReset.Params(configMock))

                    // then
                    verify(exactly = 1) { webViewAppMock.resetWebViewAppInitialization() }
                    verify(exactly = 0) { SdkProperties.setInitialized(false) }
                    assertTrue(result.isFailure)
                    assertNotNull(result.exceptionOrNull())
                    assertEquals("Reset failed on opening ConditionVariable", result.exceptionOrNull()?.message)
                }
            }
        }
    }

    @Test
    fun doWork_webViewAppNull_success() = runBlockingTest {
        mockkStatic(SdkProperties::class) {
            mockkStatic(WebViewApp::class) {
                // given
                every { WebViewApp.getCurrentApp() } returns null
                every { configMock.getModuleConfiguration(any()) } returns null
                every { SdkProperties.getCacheDirectory(any()) } returns mockkClass(File::class)

                // when
                val result = initializeStateReset(InitializeStateReset.Params(configMock))

                // then
                assertTrue(result.isSuccess)
            }
        }
    }

    @Test
    fun doWork_moduleConfiguration_calledModuleConfigSuccess() = runBlockingTest {
        mockkStatic(SdkProperties::class) {
            // given
            every { configMock.getModuleConfiguration(any()) } returns moduleConfigurationMock
            every { SdkProperties.getCacheDirectory(any()) } returns mockkClass(File::class)

            // when
            val result = initializeStateReset(InitializeStateReset.Params(configMock))

            // then
            assertTrue(result.isSuccess)
            verify(exactly = 1) { moduleConfigurationMock.resetState(configMock) }
        }
    }
}