package com.unity3d.services.core.domain.task

import com.unity3d.services.core.configuration.Configuration
import com.unity3d.services.core.misc.Utilities
import com.unity3d.services.core.properties.SdkProperties
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import junit.framework.Assert.assertNull
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class InitializeStateLoadCacheTest {
    // Injected into InitializeStateLoadCache constructor
    var dispatchers : TestSDKDispatchers = TestSDKDispatchers()

    @MockK
    lateinit var configMock: Configuration

    @InjectMockKs
    lateinit var initializeStateLoadCache: InitializeStateLoadCache

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
    }

    @Test
    fun doWork_localWebViewFileNull_taskSuccessWithNullResult() = runBlockingTest {
        mockkStatic(SdkProperties::class) {
            // given
            every { SdkProperties.getLocalWebViewFile() } returns null

            // when
            val stateLoadConfigFileResult = initializeStateLoadCache(InitializeStateLoadCache.Params(Configuration()))

            // then
            assertTrue(stateLoadConfigFileResult.isSuccess)
            assertNull(stateLoadConfigFileResult.getOrThrow())
        }
    }

    @Test
    fun doWork_localWebViewDataHashMismatch_taskSuccessWithNullResult() = runBlockingTest {
        mockkStatic(SdkProperties::class) {
            mockkStatic(Utilities::class) {
                // given
                every { SdkProperties.getLocalWebViewFile() } returns ""
                every { Utilities.readFileBytes(any()) } returns "testData".toByteArray()
                every { configMock.webViewHash } returns "testHash"

                // when
                val stateLoadConfigFileResult = initializeStateLoadCache(InitializeStateLoadCache.Params(configMock))

                // then
                assertTrue(stateLoadConfigFileResult.isSuccess)
                assertNull(stateLoadConfigFileResult.getOrThrow())
            }
        }
    }

    @Test
    fun doWork_localWebViewDataHashNull_taskSuccessWithNullResult() = runBlockingTest {
        mockkStatic(SdkProperties::class) {
            mockkStatic(Utilities::class) {
                // given
                every { SdkProperties.getLocalWebViewFile() } returns ""
                every { Utilities.readFileBytes(any()) } returns "testData".toByteArray()
                every { Utilities.Sha256(any<ByteArray>()) } returns null
                every { configMock.webViewHash } returns "testHash"

                // when
                val stateLoadConfigFileResult = initializeStateLoadCache(InitializeStateLoadCache.Params(configMock))

                // then
                assertTrue(stateLoadConfigFileResult.isSuccess)
                assertNull(stateLoadConfigFileResult.getOrThrow())
            }
        }
    }

    @Test
    fun doWork_localWebViewDataHashMatch_taskSuccessWithWebViewData() = runBlockingTest {
        mockkStatic(SdkProperties::class) {
            mockkStatic(Utilities::class) {
                // given
                every { SdkProperties.getLocalWebViewFile() } returns ""
                every { Utilities.readFileBytes(any()) } returns "testData".toByteArray()
                every { configMock.webViewHash } returns "ba477a0ac57e10dd90bb5bf0289c5990fe839c619b26fde7c2aac62f526d4113"

                // when
                val stateLoadConfigFileResult = initializeStateLoadCache(InitializeStateLoadCache.Params(configMock))

                // then
                assertTrue(stateLoadConfigFileResult.isSuccess)
                assertEquals("testData", stateLoadConfigFileResult.getOrNull())
            }
        }
    }

}