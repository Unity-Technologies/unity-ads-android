package com.unity3d.services.core.measurements

import android.adservices.AdServicesState
import android.adservices.measurement.MeasurementManager
import android.content.Context
import android.net.Uri
import android.os.OutcomeReceiver
import android.os.ext.SdkExtensions
import android.view.InputEvent
import com.unity3d.services.ads.measurements.MeasurementsErrors
import com.unity3d.services.ads.measurements.MeasurementsEvents
import com.unity3d.services.ads.measurements.MeasurementsService
import com.unity3d.services.core.device.Device
import com.unity3d.services.core.domain.ISDKDispatchers
import com.unity3d.services.core.domain.task.TestSDKDispatchers
import com.unity3d.services.core.properties.ClientProperties
import com.unity3d.services.core.webview.WebViewEventCategory
import com.unity3d.services.core.webview.bridge.IEventSender
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class MeasurementsServiceTest {
    @MockK
    private lateinit var mockContext: Context
    @MockK
    private lateinit var mockEventSender: IEventSender
    @MockK
    private lateinit var mockMeasurementManager: MeasurementManager
    @MockK
    private lateinit var mockInputEvent: InputEvent

    private val dispatchers: ISDKDispatchers = TestSDKDispatchers()

    private lateinit var measurementsService: MeasurementsService

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        mockkStatic(ClientProperties::class)
        every { ClientProperties.getApplicationContext() } returns mockContext

        mockkStatic(Uri::class)
        every { Uri.parse(any()) } returns mockk()
    }

    @Test
    fun checkAvailability_measurementsManager_isNull() {
        mockkStatic(Device::class, SdkExtensions::class) {
            // given
            every { Device.getApiLevel() } returns 33
            every { SdkExtensions.getExtensionVersion(SdkExtensions.AD_SERVICES) } returns 4
            every { mockContext.getSystemService(MeasurementManager::class.java) } returns null
            measurementsService = MeasurementsService(mockContext, dispatchers, mockEventSender)

            // when
            measurementsService.checkAvailability()

            // then
            verify {
                mockEventSender.sendEvent(
                    WebViewEventCategory.MEASUREMENTS,
                    MeasurementsEvents.NOT_AVAILABLE,
                    MeasurementsErrors.ERROR_MANAGER_NULL
                )
            }
        }
    }

    @Test
    fun checkAvailability_api_below33() {
        mockkStatic(Device::class) {
            // given
            every { mockContext.getSystemService(MeasurementManager::class.java) } returns mockMeasurementManager
            every { Device.getApiLevel() } returns 32
            measurementsService = MeasurementsService(mockContext, dispatchers, mockEventSender)

            // when
            measurementsService.checkAvailability()

            // then
            verify {
                mockEventSender.sendEvent(
                    WebViewEventCategory.MEASUREMENTS,
                    MeasurementsEvents.NOT_AVAILABLE,
                    MeasurementsErrors.ERROR_API_BELOW_33
                )
            }
        }
    }

    @Test
    fun checkAvailability_sdkExtension_below4() {
        mockkStatic(Device::class, SdkExtensions::class) {
            // given
            every { mockContext.getSystemService(MeasurementManager::class.java) } returns mockMeasurementManager
            every { Device.getApiLevel() } returns 33
            every { SdkExtensions.getExtensionVersion(SdkExtensions.AD_SERVICES) } returns 3

            measurementsService = MeasurementsService(mockContext, dispatchers, mockEventSender)

            // when
            measurementsService.checkAvailability()

            // then
            verify {
                mockEventSender.sendEvent(
                    WebViewEventCategory.MEASUREMENTS,
                    MeasurementsEvents.NOT_AVAILABLE,
                    MeasurementsErrors.ERROR_EXTENSION_BELOW_4
                )
            }
        }
    }

    @Test
    fun checkAvailability_adServices_disabled() {
        mockkStatic(Device::class, SdkExtensions::class, AdServicesState::class) {
            // given
            every { mockContext.getSystemService(MeasurementManager::class.java) } returns mockMeasurementManager
            every { Device.getApiLevel() } returns 33
            every { SdkExtensions.getExtensionVersion(SdkExtensions.AD_SERVICES) } returns 4
            every { AdServicesState.isAdServicesStateEnabled() } returns false

            measurementsService = MeasurementsService(mockContext, dispatchers, mockEventSender)

            // when
            measurementsService.checkAvailability()

            // then
            verify {
                mockEventSender.sendEvent(
                    WebViewEventCategory.MEASUREMENTS,
                    MeasurementsEvents.NOT_AVAILABLE,
                    MeasurementsErrors.ERROR_AD_SERVICES_DISABLED
                )
            }
        }
    }

    @Test
    fun checkAvailability_success() {
        mockkStatic(Device::class, SdkExtensions::class, AdServicesState::class) {
            // given
            every { mockContext.getSystemService(MeasurementManager::class.java) } returns mockMeasurementManager
            every { Device.getApiLevel() } returns 33
            every { SdkExtensions.getExtensionVersion(SdkExtensions.AD_SERVICES) } returns 4
            every { AdServicesState.isAdServicesStateEnabled() } returns true

            measurementsService = MeasurementsService(mockContext, dispatchers, mockEventSender)

            every { mockMeasurementManager.getMeasurementApiStatus(any(), any()) } answers {
                val callback = secondArg<OutcomeReceiver<Int, Exception>>()
                callback.onResult(200)
            }

            // when
            measurementsService.checkAvailability()

            // then
            verify {
                mockEventSender.sendEvent(
                    WebViewEventCategory.MEASUREMENTS,
                    MeasurementsEvents.AVAILABLE,
                    200
                )
            }
        }
    }

    @Test
    fun registerView_callsInvokeCallback() {
        mockkStatic(Device::class, SdkExtensions::class) {
            // given
            every { mockContext.getSystemService(MeasurementManager::class.java) } returns mockMeasurementManager
            every { Device.getApiLevel() } returns 33
            every { SdkExtensions.getExtensionVersion(SdkExtensions.AD_SERVICES) } returns 4

            measurementsService = MeasurementsService(mockContext, dispatchers, mockEventSender)

            every { mockMeasurementManager.registerSource(any(), any(), any(), any()) } answers {
                val callback = lastArg<OutcomeReceiver<Int, Exception>>()
                callback.onResult(200)
            }

            every { Uri.parse(any()) } returns mockk()

            // when
            measurementsService.registerView("https://example.com")

            // then
            verify {
                mockEventSender.sendEvent(
                    WebViewEventCategory.MEASUREMENTS,
                    MeasurementsEvents.VIEW_SUCCESSFUL
                )
            }
        }
    }

    @Test
    fun registerClick_callsInvokeCallback() {
        mockkStatic(Device::class, SdkExtensions::class) {
            // given
            every { mockContext.getSystemService(MeasurementManager::class.java) } returns mockMeasurementManager
            every { Device.getApiLevel() } returns 33
            every { SdkExtensions.getExtensionVersion(SdkExtensions.AD_SERVICES) } returns 4

            measurementsService = MeasurementsService(mockContext, dispatchers, mockEventSender)

            every { mockMeasurementManager.registerSource(any(), any(), any(), any()) } answers {
                val callback = lastArg<OutcomeReceiver<Int, Exception>>()
                callback.onResult(200)
            }

            every { Uri.parse(any()) } returns mockk()

            // when
            measurementsService.registerClick("https://example.com", mockInputEvent)

            // then
            verify {
                mockEventSender.sendEvent(
                    WebViewEventCategory.MEASUREMENTS,
                    MeasurementsEvents.CLICK_SUCCESSFUL
                )
            }
        }
    }
}