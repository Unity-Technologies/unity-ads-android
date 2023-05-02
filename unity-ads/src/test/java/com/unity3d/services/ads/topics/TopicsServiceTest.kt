package com.unity3d.services.ads.topics

import android.adservices.AdServicesState
import android.adservices.topics.GetTopicsRequest
import android.adservices.topics.TopicsManager
import android.content.Context
import android.os.ext.SdkExtensions
import com.unity3d.services.core.device.Device
import com.unity3d.services.core.domain.ISDKDispatchers
import com.unity3d.services.core.domain.task.TestSDKDispatchers
import com.unity3d.services.core.properties.ClientProperties
import com.unity3d.services.core.webview.WebViewEventCategory
import com.unity3d.services.core.webview.bridge.IEventSender
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class TopicsServiceTest {
    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var eventSender: IEventSender

    private val dispatchers: ISDKDispatchers = TestSDKDispatchers()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        mockkStatic(ClientProperties::class)
        every { ClientProperties.getApplicationContext() } returns context
    }

    @Test
    fun checkAvailability_error_topicsManagerNull() {
        mockkStatic(Device::class, SdkExtensions::class) {
            // given
            every { context.getSystemService(TopicsManager::class.java) } returns null
            every { Device.getApiLevel() } returns 33
            every { SdkExtensions.getExtensionVersion(SdkExtensions.AD_SERVICES) } returns 4
            val service = TopicsService(context, dispatchers, eventSender)

            // when
            val result = service.checkAvailability()

            // then
            assertEquals(TopicsStatus.ERROR_TOPICSMANAGER_NULL, result)
        }
    }

    @Test
    fun checkAvailability_error_apiBelow33() {
        mockkStatic(Device::class) {
            // given
            val topicsManager: TopicsManager = mockk()
            every { context.getSystemService(TopicsManager::class.java) } returns topicsManager
            every { Device.getApiLevel() } returns 32
            val service = TopicsService(context, dispatchers, eventSender)

            // when
            val result = service.checkAvailability()

            // then
            assertEquals(TopicsStatus.ERROR_API_BELOW_33, result)
        }
    }

    @Test
    fun checkAvailability_error_extensionBelow4() {
        mockkStatic(Device::class, SdkExtensions::class) {
            // given
            val topicsManager: TopicsManager = mockk()
            every { context.getSystemService(TopicsManager::class.java) } returns topicsManager
            every { Device.getApiLevel() } returns 33
            every { SdkExtensions.getExtensionVersion(SdkExtensions.AD_SERVICES) } returns 3
            val service = TopicsService(context, dispatchers, eventSender)

            // when
            val result = service.checkAvailability()

            // then
            assertEquals(TopicsStatus.ERROR_EXTENSION_BELOW_4, result)
        }
    }

    @Test
    fun checkAvailability_error_adServicesDisabled() {
        mockkStatic(Device::class, SdkExtensions::class, AdServicesState::class) {
            // given
            val topicsManager: TopicsManager = mockk()
            every { context.getSystemService(TopicsManager::class.java) } returns topicsManager
            every { Device.getApiLevel() } returns 34
            every { SdkExtensions.getExtensionVersion(SdkExtensions.AD_SERVICES) } returns 4
            every { AdServicesState.isAdServicesStateEnabled() } returns false
            val service = TopicsService(context, dispatchers, eventSender)

            // when
            val result = service.checkAvailability()

            // then
            assertEquals(TopicsStatus.ERROR_AD_SERVICES_DISABLED, result)
        }
    }

    @Test
    fun checkAvailability_success_topicsAvailable() {
        mockkStatic(Device::class, SdkExtensions::class, AdServicesState::class) {
            // given
            val topicsManager: TopicsManager = mockk()
            every { context.getSystemService(TopicsManager::class.java) } returns topicsManager
            every { Device.getApiLevel() } returns 34
            every { SdkExtensions.getExtensionVersion(SdkExtensions.AD_SERVICES) } returns 4
            every { AdServicesState.isAdServicesStateEnabled() } returns true
            val service = TopicsService(context, dispatchers, eventSender)

            // when
            val result = service.checkAvailability()

            // then
            assertEquals(TopicsStatus.TOPICS_AVAILABLE, result)
        }
    }

    @Test
    fun getTopics_success_getTopics() {
        mockkStatic(Device::class, SdkExtensions::class) {
            // given
            val topicsManager: TopicsManager = mockk()
            every { topicsManager.getTopics(any(), any(), any()) } returns Unit
            every { context.getSystemService(TopicsManager::class.java) } returns topicsManager
            every { Device.getApiLevel() } returns 33
            every { SdkExtensions.getExtensionVersion(SdkExtensions.AD_SERVICES) } returns 4
            val service = TopicsService(context, dispatchers, eventSender)
            val topicsRequest: GetTopicsRequest = mockk()
            val builder: GetTopicsRequest.Builder = mockk()
            every { builder.setAdsSdkName(any()) } returns builder
            every { builder.setShouldRecordObservation(any()) } returns builder
            every { builder.build() } returns topicsRequest
            mockkConstructor(GetTopicsRequest.Builder::class)
            every { anyConstructed<GetTopicsRequest.Builder>().setAdsSdkName("UnityAds") } returns mockk {
                every { setShouldRecordObservation(false) } returns mockk {
                    every { build() } returns topicsRequest
                }
            }

            // when
            service.getTopics("UnityAds", false)

            // then
            verify { topicsManager.getTopics(topicsRequest, any(), any()) }
        }
    }

    @Test
    fun getTopics_failure_sendEvent() {
        mockkStatic(Device::class, SdkExtensions::class) {
            // given
            val topicsManager: TopicsManager = mockk()
            every { topicsManager.getTopics(any(), any(), any()) } throws Exception("test error")
            every { context.getSystemService(TopicsManager::class.java) } returns topicsManager
            every { Device.getApiLevel() } returns 33
            every { SdkExtensions.getExtensionVersion(SdkExtensions.AD_SERVICES) } returns 4
            every {
                eventSender.sendEvent(
                    WebViewEventCategory.TOPICS,
                    TopicsEvents.NOT_AVAILABLE,
                    TopicsErrors.ERROR_EXCEPTION,
                    any()
                )
            } returns true
            val service = TopicsService(context, dispatchers, eventSender)
            val topicsRequest: GetTopicsRequest = mockk()
            val builder: GetTopicsRequest.Builder = mockk()
            every { builder.setAdsSdkName(any()) } returns builder
            every { builder.setShouldRecordObservation(any()) } returns builder
            every { builder.build() } returns topicsRequest
            mockkConstructor(GetTopicsRequest.Builder::class)
            every { anyConstructed<GetTopicsRequest.Builder>().setAdsSdkName("UnityAds") } returns mockk {
                every { setShouldRecordObservation(false) } returns mockk {
                    every { build() } returns topicsRequest
                }
            }

            // when
            service.getTopics("UnityAds", false)

            // then
            verify {
                eventSender.sendEvent(
                    WebViewEventCategory.TOPICS,
                    TopicsEvents.NOT_AVAILABLE,
                    TopicsErrors.ERROR_EXCEPTION,
                    any()
                )
            }
        }
    }
}
