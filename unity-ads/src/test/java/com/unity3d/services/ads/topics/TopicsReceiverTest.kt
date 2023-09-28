package com.unity3d.services.ads.topics

import android.adservices.topics.GetTopicsResponse
import android.adservices.topics.Topic
import com.unity3d.services.core.webview.WebViewEventCategory
import com.unity3d.services.core.webview.bridge.IEventSender
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.json.JSONArray
import org.junit.Test
import kotlin.test.assertEquals

class TopicsReceiverTest {
    @Test
    fun onResult_success_sendEvent() {
        // given
        val eventSenderMock: IEventSender = mockk()
        every { eventSenderMock.sendEvent(WebViewEventCategory.TOPICS, TopicsEvents.TOPICS_AVAILABLE, allAny()) } returns true

        val receiver = TopicsReceiver(eventSenderMock)
        val topicsresponse: GetTopicsResponse = mockk()

        val topic1: Topic = mockk()
        every { topic1.taxonomyVersion } returns 1
        every { topic1.modelVersion } returns 11
        every { topic1.topicId } returns 111

        val topic2: Topic = mockk()
        every { topic2.taxonomyVersion } returns 2
        every { topic2.modelVersion } returns 22
        every { topic2.topicId } returns 222

        val topic3: Topic = mockk()
        every { topic3.taxonomyVersion } returns 3
        every { topic3.modelVersion } returns 33
        every { topic3.topicId } returns 333

        every { topicsresponse.topics } returns listOf(topic1, topic2, topic3)

        // when
        receiver.onResult(topicsresponse)

        // then
        val resultArray = JSONArray()
        resultArray.put(receiver.formatTopic(topic1))
        resultArray.put(receiver.formatTopic(topic2))
        resultArray.put(receiver.formatTopic(topic3))
        val expectedResult = resultArray.toString()

        verify { eventSenderMock.sendEvent(WebViewEventCategory.TOPICS, TopicsEvents.TOPICS_AVAILABLE, expectedResult) }
    }

    @Test
    fun onError_failure_sendEvent() {
        // given
        val exception: Exception = mockk()
        val exceptionDescription = "test exception description"
        every { exception.toString() } returns exceptionDescription
        val eventSenderMock: IEventSender = mockk()
        every { eventSenderMock.sendEvent(WebViewEventCategory.TOPICS, TopicsEvents.NOT_AVAILABLE, TopicsErrors.ERROR_EXCEPTION, allAny()) } returns true
        val receiver = TopicsReceiver(eventSenderMock)

        // when
        receiver.onError(exception)

        // then
        verify { eventSenderMock.sendEvent(WebViewEventCategory.TOPICS, TopicsEvents.NOT_AVAILABLE, TopicsErrors.ERROR_EXCEPTION, exceptionDescription)}
    }

    @Test
    fun formatTopic_topicProvided_properJsonResult() {
        // given
        val receiver = TopicsReceiver(mockk())
        val topic: Topic = mockk()
        every { topic.taxonomyVersion } returns 1
        every { topic.modelVersion } returns 2
        every { topic.topicId } returns 3

        // when
        val result = receiver.formatTopic(topic)

        // then
        assertEquals(result.getLong("taxonomyVersion"), 1)
        assertEquals(result.getLong("modelVersion"), 2)
        assertEquals(result.getInt("topicId"), 3)
    }
}
