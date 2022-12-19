package com.unity3d.services.core.domain.task

import com.unity3d.services.core.configuration.Configuration
import com.unity3d.services.core.configuration.IModuleConfiguration

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class InitializeStateInitModulesTest {

    val dispatchers: TestSDKDispatchers = TestSDKDispatchers()

    @MockK
    lateinit var moduleMock: IModuleConfiguration

    @MockK
    lateinit var configMock: Configuration

    @InjectMockKs
    lateinit var initializeStateInitModules: InitializeStateInitModules

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        every { configMock.getModuleConfiguration(any()) } returns moduleMock
        every { moduleMock.initModuleState(any()) } returns true
    }

    @Test
    fun doWork_emptyModuleConfigurationList_returnSuccessConfig() = runBlockingTest {
        // given
        val emptyModuleList: Array<String?> = emptyArray()
        every { configMock.moduleConfigurationList } returns emptyModuleList

        // when
        val result: Result<Configuration> =
            initializeStateInitModules(InitializeStateInitModules.Params(configMock))

        // then
        assertTrue(result.isSuccess)
        assertEquals(configMock, result.getOrThrow())
        coVerify(exactly = 0) { configMock.getModuleConfiguration(any()) }
    }

    @Test
    fun doWork_nullModuleConfigurationList_returnSuccessConfig() = runBlockingTest {
        // given
        every { configMock.moduleConfigurationList } returns null

        // when
        val result: Result<Configuration> =
            initializeStateInitModules(InitializeStateInitModules.Params(configMock))

        // then
        assertTrue(result.isSuccess)
        assertEquals(configMock, result.getOrThrow())
        coVerify(exactly = 0) { configMock.getModuleConfiguration(any()) }
    }

    @Test
    fun doWork_validModuleConfigurationList_returnSuccessConfig() = runBlockingTest {
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
        val result: Result<Configuration> =
            initializeStateInitModules(InitializeStateInitModules.Params(configMock))

        // then
        assertTrue(result.isSuccess)
        assertEquals(configMock, result.getOrThrow())
        coVerify(exactly = 5) { configMock.getModuleConfiguration(any()) }
    }

    @Test
    fun doWork_validModuleConfigurationListWithNullModuleConfiguration_returnSuccessConfig() =
        runBlockingTest {
            // given
           val moduleList = arrayOf(
                "com.unity3d.services.core.configuration.CoreModuleConfiguration",
            )
            every { configMock.moduleConfigurationList } returns moduleList
            every { configMock.getModuleConfiguration(any()) } returns null

            // when
            val result: Result<Configuration> =
                initializeStateInitModules(InitializeStateInitModules.Params(configMock))

            // then
            assertTrue(result.isSuccess)
            assertEquals(configMock, result.getOrThrow())
            coVerify(exactly = 1) { configMock.getModuleConfiguration(any()) }
        }

    @Test
    fun doWork_validModuleConfigurationListWithFalseReturningInitModuleState_returnFailure() =
        runBlockingTest {
            // given
            val exceptionMessage =
                "Unity Ads config server resolves to loopback address (due to ad blocker?)"
            val moduleList = arrayOf(
                "com.unity3d.services.core.configuration.CoreModuleConfiguration",
            )
            every { configMock.moduleConfigurationList } returns moduleList
            every { moduleMock.initModuleState(any()) } returns false

            // when
            val result: Result<Configuration> =
                initializeStateInitModules(InitializeStateInitModules.Params(configMock))

            // then
            assertTrue(result.isFailure)
            assertEquals(exceptionMessage, result.exceptionOrNull()?.message)
            coVerify(exactly = 1) { configMock.getModuleConfiguration(any()) }
        }

}