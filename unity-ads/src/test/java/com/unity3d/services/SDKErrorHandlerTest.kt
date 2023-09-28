package com.unity3d.services

import com.unity3d.services.core.domain.ISDKDispatchers
import com.unity3d.services.core.request.metrics.Metric
import com.unity3d.services.core.request.metrics.SDKMetricsSender
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import kotlin.coroutines.CoroutineContext
import kotlin.test.assertEquals

class SDKErrorHandlerTest {
    @MockK
    private lateinit var dispatchers: ISDKDispatchers

    @MockK
    private lateinit var sdkMetricsSender: SDKMetricsSender

    @MockK
    private lateinit var coroutineContext: CoroutineContext

    @MockK
    private lateinit var exceptionMock: Exception

    @InjectMockKs
    private lateinit var sdkErrorHandler: SDKErrorHandler

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
    }

    @Test
    fun handleException_exceptionProvided_metricSent() {
        // given
        val exception = RuntimeException()

        // when
        sdkErrorHandler.handleException(coroutineContext, exception)

        // then
        verify { sdkMetricsSender.sendMetric(any()) }
    }

    @Test
    fun handleException_exceptionProvidedWithStackTraceNullElement_metricSent() {
        // given
        val capturedMetric = slot<Metric>()
        every { exceptionMock.stackTrace } returns arrayOf(null)
        every { sdkMetricsSender.sendMetric(capture(capturedMetric)) } returns Unit

        // when
        sdkErrorHandler.handleException(coroutineContext, exceptionMock)

        // then
        verify { sdkMetricsSender.sendMetric(any()) }
        assertEquals("native_exception", capturedMetric.captured.name)
        assertEquals("unknown_0", capturedMetric.captured.value)
    }

    @Test
    fun handleException_exceptionProvidedWithStackTraceButNullFileName_metricSent() {
        // given
        val capturedMetric = slot<Metric>()
        val mockedStackTraceElement: StackTraceElement = mockk(relaxed = true)
        every { mockedStackTraceElement.fileName } returns null
        every { exceptionMock.stackTrace } returns arrayOf(mockedStackTraceElement)
        every { sdkMetricsSender.sendMetric(capture(capturedMetric)) } returns Unit

        // when
        sdkErrorHandler.handleException(coroutineContext, exceptionMock)

        // then
        verify { sdkMetricsSender.sendMetric(any()) }
        assertEquals("native_exception", capturedMetric.captured.name)
        assertEquals("unknown_0", capturedMetric.captured.value)
    }
}