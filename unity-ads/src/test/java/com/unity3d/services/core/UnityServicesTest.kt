package com.unity3d.services.core

import android.content.Context
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.UnityAds
import com.unity3d.services.UnityServices
import com.unity3d.services.core.configuration.InitializeEventsMetricSender
import com.unity3d.services.core.device.Device
import com.unity3d.services.core.log.DeviceLog
import com.unity3d.services.core.properties.ClientProperties
import com.unity3d.services.core.properties.SdkProperties
import com.unity3d.services.core.request.metrics.InitMetric
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test

class UnityServicesTest {

    @MockK
    lateinit var contextMock: Context

    @MockK
    lateinit var initializationListenerMock: IUnityAdsInitializationListener

    @MockK
    lateinit var initializeEventsSenderMock: InitializeEventsMetricSender

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
    }

    @Test
    fun doWork_callInitializeWithSameParams_sendMetricAndContinue() {
        val gameId = "12345"
        val testMode = true

        val expected = "Unity Ads SDK initialize has already been called with the same Game ID: $gameId and Test Mode: $testMode values. Responding with first initialization result."

        mockkStatic(DeviceLog::class, SdkProperties::class, ClientProperties::class, InitializeEventsMetricSender::class) {
            every { SdkProperties.getCurrentInitializationState() } returns SdkProperties.InitializationState.INITIALIZING
            every { SdkProperties.isTestMode() } returns testMode
            every { ClientProperties.getGameId() } returns gameId
            every { InitializeEventsMetricSender.getInstance() } returns initializeEventsSenderMock

            UnityServices.initialize(contextMock, gameId, testMode, initializationListenerMock)

            verify(exactly = 1) { initializeEventsSenderMock.sendMetric(InitMetric.newInitSameParams()) }
            verify(exactly = 1) { DeviceLog.warning(expected) }
        }
    }

    @Test
    fun doWork_callInitializeWithDiffParams_sendErrorAndMetric() {
        val previousGameId = "12345"
        val previousTestMode = true
        val gameId = "678910"
        val testMode = false

        val params = buildString {
            appendLine("different parameters: ")
            appendLine(createExpectedParametersString("Game ID", previousGameId, gameId))
            appendLine(createExpectedParametersString("Test Mode", previousTestMode, testMode))
        }

        val expected = "Unity Ads SDK initialize has already been called with $params Responding with first initialization result."

        mockkStatic(DeviceLog::class, SdkProperties::class, ClientProperties::class, InitializeEventsMetricSender::class) {
            every { SdkProperties.getCurrentInitializationState() } returns SdkProperties.InitializationState.INITIALIZING
            every { SdkProperties.isTestMode() } returns previousTestMode
            every { ClientProperties.getGameId() } returns previousGameId
            every { InitializeEventsMetricSender.getInstance() } returns initializeEventsSenderMock

            UnityServices.initialize(contextMock, gameId, testMode, initializationListenerMock)

            verify(exactly = 1) { initializeEventsSenderMock.sendMetric(InitMetric.newInitDiffParams()) }
            verify(exactly = 1) { DeviceLog.warning(expected) }
        }
    }


    @Test
    fun doWork_callInitializeFirstTime_noErrorOrMetric() {
        val gameId = "12345"
        val testMode = true

        mockkStatic(DeviceLog::class, SdkProperties::class, Device::class, InitializeEventsMetricSender::class) {
            every { SdkProperties.getCurrentInitializationState() } returns SdkProperties.InitializationState.NOT_INITIALIZED
            every { Device.getElapsedRealtime() } returns 100L
            every { InitializeEventsMetricSender.getInstance() } returns initializeEventsSenderMock

            UnityServices.initialize(contextMock, gameId, testMode, initializationListenerMock)

            verify(exactly = 0) { initializeEventsSenderMock.sendMetric(any()) }
            verify(exactly = 0) { DeviceLog.warning(any()) }
        }
    }

    @Test
    fun initialize_callWithNonNumberGameId_errorListenerTriggered() {
        mockkStatic(Device::class, DeviceLog::class) {
            // given
            val gameId = "invalidGameId"

            // when
            UnityServices.initialize(contextMock, gameId, true, initializationListenerMock)

            // then
            verify(exactly = 1) {
                initializationListenerMock.onInitializationFailed(
                    UnityAds.UnityAdsInitializationError.INVALID_ARGUMENT,
                    any()
                )
            }
        }
    }

    private fun createExpectedParametersString(fieldName: String, current: Any?, received: Any?): String =
        "- $fieldName Current: $current | Received: $received"

}
