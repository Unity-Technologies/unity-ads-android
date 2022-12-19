package com.unity3d.services.core.domain.task

import com.unity3d.services.core.configuration.Configuration
import com.unity3d.services.core.configuration.ErrorState
import com.unity3d.services.core.configuration.IModuleConfiguration
import com.unity3d.services.core.log.DeviceLog
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class InitializeStateErrorTest {

    private val errorState: ErrorState = ErrorState.InitModules

    private val defaultExceptionMessage: String = "Error occurred"

    private val defaultException: Exception = Exception(defaultExceptionMessage)

    private val defaultDeviceLog: String = "Unity Ads init: halting init in init_modules: Error occurred"

    private val dispatchers: TestSDKDispatchers = TestSDKDispatchers()

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
    }

    @Test
    fun doWork_emptyModuleConfigurationList_returnSuccess() = runBlockingTest {
        mockkStatic(DeviceLog::class) {
            // given
            val emptyModuleList: Array<String?> = emptyArray()
            every { configMock.moduleConfigurationList } returns emptyModuleList
            every { DeviceLog.error(any()) } returns Unit

            // when
            val result: Result<Unit> =
                initializeStateError(InitializeStateError.Params(errorState, defaultException, configMock))

            // then
            assertTrue(result.isSuccess)
            coVerify(exactly = 0) { configMock.getModuleConfiguration(any()) }
            verify(exactly = 1) { DeviceLog.error(defaultDeviceLog) }
        }
    }

    @Test
    fun doWork_nullModuleConfigurationList_returnSuccess() = runBlockingTest {
        mockkStatic(DeviceLog::class) {
            // given
            every { configMock.moduleConfigurationList } returns null

            // when
            val result: Result<Unit> =
                initializeStateError(InitializeStateError.Params(errorState, defaultException, configMock))

            // then
            assertTrue(result.isSuccess)
            coVerify(exactly = 0) { configMock.getModuleConfiguration(any()) }
            verify(exactly = 1) { DeviceLog.error(defaultDeviceLog) }
        }
    }

    @Test
    fun doWork_validModuleConfigurationList_returnSuccess() = runBlockingTest {
        mockkStatic(DeviceLog::class) {
            // given
            val moduleList = arrayOf(
                "com.unity3d.services.core.configuration.CoreModuleConfiguration",
                "com.unity3d.services.ads.configuration.AdsModuleConfiguration",
                "com.unity3d.services.analytics.core.configuration.AnalyticsModuleConfiguration",
                "com.unity3d.services.banners.configuration.BannersModuleConfiguration",
                "com.unity3d.services.store.core.configuration.StoreModuleConfiguration"
            )

            every { configMock.moduleConfigurationList } returns moduleList

            // when
            val result: Result<Unit> =
                initializeStateError(InitializeStateError.Params(errorState, defaultException, configMock))

            // then
            assertTrue(result.isSuccess)
            coVerify(exactly = 5) { configMock.getModuleConfiguration(any()) }
            coVerify(exactly = 5) { moduleMock.initErrorState(configMock, errorState, defaultExceptionMessage) }
            verify(exactly = 1) { DeviceLog.error(defaultDeviceLog) }
        }
    }

    @Test
    fun doWork_validModuleConfigurationListWithNullModuleConfiguration_returnSuccess() =
        runBlockingTest {
            mockkStatic(DeviceLog::class) {
                // given
                val moduleList = arrayOf(
                    "com.unity3d.services.core.configuration.CoreModuleConfiguration",
                )
                every { configMock.moduleConfigurationList } returns moduleList
                every { configMock.getModuleConfiguration(any()) } returns null

                // when
                val result: Result<Unit> =
                    initializeStateError(InitializeStateError.Params(errorState, defaultException, configMock))

                // then
                assertTrue(result.isSuccess)
                coVerify(exactly = 1) { configMock.getModuleConfiguration(any()) }
                coVerify(exactly = 0) { moduleMock.initErrorState(configMock, errorState, defaultExceptionMessage) }
                verify(exactly = 1) { DeviceLog.error(defaultDeviceLog) }
            }
        }

}