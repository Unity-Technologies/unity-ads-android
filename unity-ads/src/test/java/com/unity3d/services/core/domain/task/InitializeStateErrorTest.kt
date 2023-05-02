package com.unity3d.services.core.domain.task

import com.unity3d.services.ads.configuration.AdsModuleConfiguration
import com.unity3d.services.analytics.core.configuration.AnalyticsModuleConfiguration
import com.unity3d.services.banners.configuration.BannersModuleConfiguration
import com.unity3d.services.core.configuration.Configuration
import com.unity3d.services.core.configuration.CoreModuleConfiguration
import com.unity3d.services.core.configuration.ErrorState
import com.unity3d.services.core.configuration.IModuleConfiguration
import com.unity3d.services.core.log.DeviceLog
import com.unity3d.services.store.core.configuration.StoreModuleConfiguration
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class InitializeStateErrorTest {

    private val errorState: ErrorState = ErrorState.InitModules

    private val defaultExceptionMessage: String = "Error occurred"

    private val defaultException: Exception = Exception(defaultExceptionMessage)

    private val defaultDeviceLog: String = "Unity Ads init: halting init in init_modules: Error occurred"

    var dispatchers = TestSDKDispatchers()

    @MockK
    lateinit var moduleMock: IModuleConfiguration

    @MockK
    lateinit var configMock: Configuration

    @InjectMockKs
    lateinit var initializeStateError: InitializeStateError

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        every { configMock.getModuleConfiguration(any()) } returns moduleMock
        every { moduleMock.initErrorState(any(), any(), any()) } returns true
        Dispatchers.setMain(dispatchers.main)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun doWork_emptyModuleConfigurationList_returnSuccess() = runTest {
        mockkStatic(DeviceLog::class) {
            // given
            val emptyModuleList: Array<Class<Any>?> = emptyArray()
            every { configMock.moduleConfigurationList } returns emptyModuleList
            every { DeviceLog.error(any()) } returns Unit

            // when
            val result = runCatching {
                initializeStateError(
                    InitializeStateError.Params(
                        errorState,
                        defaultException,
                        configMock
                    )
                )
            }

            // then
            assertTrue(result.isSuccess)
            coVerify(exactly = 0) { configMock.getModuleConfiguration(any()) }
            verify(exactly = 1) { DeviceLog.error(defaultDeviceLog) }
        }
    }

    @Test
    fun doWork_nullModuleConfigurationList_returnSuccess() = runTest {
        mockkStatic(DeviceLog::class) {
            // given
            every { configMock.moduleConfigurationList } returns null

            // when
            val result = runCatching {
                initializeStateError(
                    InitializeStateError.Params(
                        errorState,
                        defaultException,
                        configMock
                    )
                )
            }

            // then
            assertTrue(result.isSuccess)
            coVerify(exactly = 0) { configMock.getModuleConfiguration(any()) }
            verify(exactly = 1) { DeviceLog.error(defaultDeviceLog) }
        }
    }

    @Test
    fun doWork_validModuleConfigurationList_returnSuccess() = runTest {
        mockkStatic(DeviceLog::class) {
            // given
            val moduleList = arrayOf(
                CoreModuleConfiguration::class.java,
                AdsModuleConfiguration::class.java,
                AnalyticsModuleConfiguration::class.java,
                BannersModuleConfiguration::class.java,
                StoreModuleConfiguration::class.java
            )

            every { configMock.moduleConfigurationList } returns moduleList

            // when
            val result = runCatching {
                initializeStateError(
                    InitializeStateError.Params(
                        errorState,
                        defaultException,
                        configMock
                    )
                )
            }

            // then
            assertTrue(result.isSuccess)
            coVerify(exactly = 5) { configMock.getModuleConfiguration(any()) }
            coVerify(exactly = 5) { moduleMock.initErrorState(configMock, errorState, defaultExceptionMessage) }
            verify(exactly = 1) { DeviceLog.error(defaultDeviceLog) }
        }
    }

    @Test
    fun doWork_validModuleConfigurationListWithNullModuleConfiguration_returnSuccess() =
        runTest {
            mockkStatic(DeviceLog::class) {
                // given
                val moduleList = arrayOf(
                    CoreModuleConfiguration::class.java,
                )
                every { configMock.moduleConfigurationList } returns moduleList
                every { configMock.getModuleConfiguration(any()) } returns null

                // when
                val result = runCatching {
                    initializeStateError(
                        InitializeStateError.Params(
                            errorState,
                            defaultException,
                            configMock
                        )
                    )
                }

                // then
                assertTrue(result.isSuccess)
                coVerify(exactly = 1) { configMock.getModuleConfiguration(any()) }
                coVerify(exactly = 0) { moduleMock.initErrorState(configMock, errorState, defaultExceptionMessage) }
                verify(exactly = 1) { DeviceLog.error(defaultDeviceLog) }
            }
        }

}