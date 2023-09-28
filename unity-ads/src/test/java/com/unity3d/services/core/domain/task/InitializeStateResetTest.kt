package com.unity3d.services.core.domain.task

import com.unity3d.services.core.configuration.Configuration
import com.unity3d.services.core.configuration.IModuleConfiguration
import com.unity3d.services.core.properties.SdkProperties
import com.unity3d.services.core.webview.WebView
import com.unity3d.services.core.webview.WebViewApp
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class InitializeStateResetTest {
    var dispatchers = TestSDKDispatchers()

    @MockK
    lateinit var moduleConfigurationMock: IModuleConfiguration

    @MockK
    lateinit var webViewAppMock: WebViewApp

    @MockK
    lateinit var webViewMock: WebView

    @MockK
    lateinit var configMock: Configuration

    @InjectMockKs
    lateinit var initializeStateReset: InitializeStateReset

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        every { configMock.moduleConfigurationList } returns arrayOf(String::class.java)
        every { configMock.webViewAppCreateTimeout } returns 200
        every { moduleConfigurationMock.initCompleteState(any()) } returns true
        every { moduleConfigurationMock.resetState(any()) } returns true
        Dispatchers.setMain(dispatchers.main)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun doWork_webViewAppReset_success() = runTest {
        mockkStatic(WebViewApp::class) {
            mockkStatic(SdkProperties::class) {
                // given
                every { WebViewApp.getCurrentApp() } returns webViewAppMock
                every { webViewAppMock.webView } returns webViewMock
                every { configMock.getModuleConfiguration(any()) } returns null
                every { SdkProperties.getCacheDirectory(any()) } returns mockkClass(File::class)

                // when
                val result = runCatching { initializeStateReset(InitializeStateReset.Params(configMock)) }

                // then
                verify(exactly = 1) { webViewAppMock.resetWebViewAppInitialization() }
                verify(exactly = 1) { SdkProperties.setInitialized(false) }
                assertTrue(result.isSuccess)
            }
        }
    }

    @Test
    fun doWork_webViewAppTimesOut_failureWithException() = runTest {
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

    @Test
    fun doWork_webViewAppNull_success() = runTest {
        mockkStatic(SdkProperties::class) {
            mockkStatic(WebViewApp::class) {
                // given
                every { WebViewApp.getCurrentApp() } returns null
                every { configMock.getModuleConfiguration(any()) } returns null
                every { SdkProperties.getCacheDirectory(any()) } returns mockkClass(File::class)

                // when
                val result = runCatching { initializeStateReset(InitializeStateReset.Params(configMock)) }

                // then
                assertTrue(result.isSuccess)
            }
        }
    }

    @Test
    fun doWork_moduleConfiguration_calledModuleConfigSuccess() = runTest {
        mockkStatic(SdkProperties::class) {
            // given
            every { configMock.getModuleConfiguration(any()) } returns moduleConfigurationMock
            every { SdkProperties.getCacheDirectory(any()) } returns mockkClass(File::class)

            // when
            val result = runCatching { initializeStateReset(InitializeStateReset.Params(configMock)) }

            // then
            assertTrue(result.isSuccess)
            verify(exactly = 1) { moduleConfigurationMock.resetState(configMock) }
        }
    }
}