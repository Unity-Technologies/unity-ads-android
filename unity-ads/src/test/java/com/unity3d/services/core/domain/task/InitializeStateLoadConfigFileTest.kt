package com.unity3d.services.core.domain.task

import com.unity3d.services.core.configuration.Configuration
import com.unity3d.services.core.misc.Utilities
import com.unity3d.services.core.properties.SdkProperties
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class InitializeStateLoadConfigFileTest {
    // Injected into InitializeStateLoadConfigFile constructor
    var dispatchers : TestSDKDispatchers = TestSDKDispatchers()

    @MockK
    lateinit var configMock: Configuration

    @InjectMockKs
    lateinit var initializeStateLoadConfigFile: InitializeStateLoadConfigFile

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
    }

    @Test
    fun doWork_localFilePathNull_taskFailure() = runBlockingTest {
        mockkStatic(SdkProperties::class) {
            // given
            every { SdkProperties.getLocalConfigurationFilepath() } returns null

            // when
            val stateLoadConfigFileResult = initializeStateLoadConfigFile(InitializeStateLoadConfigFile.Params(Configuration()))

            // then
            assertTrue(stateLoadConfigFileResult.isFailure)
        }
    }

    @Test
    fun doWork_localFileCorrupted_taskSuccessWithProvidedConfig() = runBlockingTest {
        mockkStatic(SdkProperties::class) {
            mockkStatic(Utilities::class) {
                // given
                every { SdkProperties.getLocalConfigurationFilepath() } returns ""
                every { Utilities.readFileBytes(any()) } throws Exception()
                every { configMock.configUrl } returns "testUrl";

                // when
                val stateLoadConfigFileResult = initializeStateLoadConfigFile(InitializeStateLoadConfigFile.Params(configMock))

                // then
                assertTrue(stateLoadConfigFileResult.isSuccess)
                assertEquals(configMock.configUrl, stateLoadConfigFileResult.getOrNull()?.configUrl)
            }
        }
    }

    @Test
    fun doWork_localFileRead_taskSuccessWithLoadedConfig() = runBlockingTest {
        mockkStatic(SdkProperties::class) {
            mockkStatic(Utilities::class) {
                // given
                every { SdkProperties.getLocalConfigurationFilepath() } returns ""
                every { Utilities.readFileBytes(any()) } returns "{\"url\":\"testUrl\"}".toByteArray()

                // when
                val stateLoadConfigFileResult = initializeStateLoadConfigFile(InitializeStateLoadConfigFile.Params(Configuration()))

                // then
                assertTrue(stateLoadConfigFileResult.isSuccess)
                assertEquals("testUrl", stateLoadConfigFileResult.getOrNull()?.webViewUrl)
            }
        }
    }

}