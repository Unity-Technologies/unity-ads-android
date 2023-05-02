package com.unity3d.services.core.domain.task

import com.unity3d.services.core.configuration.Configuration
import com.unity3d.services.core.misc.Utilities
import com.unity3d.services.core.properties.SdkProperties
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class InitializeStateLoadCacheTest {
    // Injected into InitializeStateLoadCache constructor
    var dispatchers: TestSDKDispatchers = TestSDKDispatchers()

    @MockK
    lateinit var configMock: Configuration

    @InjectMockKs
    lateinit var initializeStateLoadCache: InitializeStateLoadCache

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        Dispatchers.setMain(dispatchers.main)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun doWork_localWebViewFileNull_taskSuccessWithNullDataResult() = runTest {
        mockkStatic(SdkProperties::class) {
            // given
            every { SdkProperties.getLocalWebViewFile() } returns null

            // when
            val stateLoadConfigFileResult =
                runCatching { initializeStateLoadCache(InitializeStateLoadCache.Params(Configuration())) }

            // then
            assertTrue(stateLoadConfigFileResult.isSuccess)
            assertNull(stateLoadConfigFileResult.getOrThrow().webViewData)
        }
    }

    @Test
    fun doWork_localWebViewDataHashMismatch_taskSuccessWithDataResultAndHashMismatch() = runTest {
        mockkStatic(SdkProperties::class) {
            mockkStatic(Utilities::class) {
                // given
                every { SdkProperties.getLocalWebViewFile() } returns ""
                every { Utilities.readFileBytes(any()) } returns "testData".toByteArray()
                every { configMock.webViewHash } returns "testHash"

                // when
                val stateLoadConfigFileResult =
                    runCatching { initializeStateLoadCache(InitializeStateLoadCache.Params(configMock)) }

                // then
                assertTrue(stateLoadConfigFileResult.isSuccess)
                assertEquals(InitializeStateLoadCache.LoadCacheResult(true, "testData"), stateLoadConfigFileResult.getOrThrow())
            }
        }
    }

    @Test
    fun doWork_localWebViewDataHashNull_taskSuccessWithDataResultAndHashMismatch() = runTest {
        mockkStatic(SdkProperties::class) {
            mockkStatic(Utilities::class) {
                // given
                every { SdkProperties.getLocalWebViewFile() } returns ""
                every { Utilities.readFileBytes(any()) } returns "testData".toByteArray()
                every { Utilities.Sha256(any<ByteArray>()) } returns null
                every { configMock.webViewHash } returns "testHash"

                // when
                val stateLoadConfigFileResult =
                    runCatching { initializeStateLoadCache(InitializeStateLoadCache.Params(configMock)) }

                // then
                assertTrue(stateLoadConfigFileResult.isSuccess)
                assertEquals(InitializeStateLoadCache.LoadCacheResult(true, "testData"), stateLoadConfigFileResult.getOrThrow())
            }
        }
    }

    @Test
    fun doWork_localWebViewDataHashMatch_taskSuccessWithWebViewData() = runTest {
        mockkStatic(SdkProperties::class) {
            mockkStatic(Utilities::class) {
                // given
                every { SdkProperties.getLocalWebViewFile() } returns ""
                every { Utilities.readFileBytes(any()) } returns "testData".toByteArray()
                every { configMock.webViewHash } returns "ba477a0ac57e10dd90bb5bf0289c5990fe839c619b26fde7c2aac62f526d4113"

                // when
                val stateLoadConfigFileResult =
                    runCatching { initializeStateLoadCache(InitializeStateLoadCache.Params(configMock)) }

                // then
                assertTrue(stateLoadConfigFileResult.isSuccess)
                assertEquals(InitializeStateLoadCache.LoadCacheResult(false, "testData"), stateLoadConfigFileResult.getOrNull())
            }
        }
    }

}