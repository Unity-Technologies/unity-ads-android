package com.unity3d.services.core.domain.task

import com.unity3d.services.core.properties.SdkProperties
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ConfigFileFromLocalStorageTest {
    // Injected into ConfigFileFromLocalStorage constructor
    var dispatchers: TestSDKDispatchers = TestSDKDispatchers()

    @InjectMockKs
    lateinit var configFileFromLocalStorage: ConfigFileFromLocalStorage

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
    fun doWork_localFilePathNull_taskFailure() = runTest {
        mockkStatic(SdkProperties::class) {
            // given
            every { SdkProperties.getLocalConfigurationFilepath() } returns null

            // when
            val stateLoadConfigFileResult =
                runCatching { configFileFromLocalStorage(ConfigFileFromLocalStorage.Params()) }

            // then
            assertTrue(stateLoadConfigFileResult.isFailure)
        }
    }

    @Test
    fun doWork_fileReadTextFailing_taskFailure() = runTest {
        mockkStatic(SdkProperties::class) {
            // given
            every { SdkProperties.getLocalConfigurationFilepath() } returns ""

            // when
            val stateLoadConfigFileResult =
                runCatching { configFileFromLocalStorage(ConfigFileFromLocalStorage.Params()) }

            // then
            assertTrue(stateLoadConfigFileResult.isFailure)
        }
    }

    @Test
    fun doWork_localFileRead_taskSuccessWithLoadedConfig() = runTest {
        mockkStatic(SdkProperties::class) {
            // given
            val temporaryFile = File.createTempFile(UUID.randomUUID().toString(), "json")
            temporaryFile.deleteOnExit()
            temporaryFile.writeText("{\"url\":\"testUrl\"}")
            every { SdkProperties.getLocalConfigurationFilepath() } returns temporaryFile.absolutePath

            // when
            val stateLoadConfigFileResult =
                runCatching { configFileFromLocalStorage(ConfigFileFromLocalStorage.Params()) }

            // then
            assertTrue(stateLoadConfigFileResult.isSuccess)
            assertEquals("testUrl", stateLoadConfigFileResult.getOrThrow().webViewUrl)
        }
    }

}
