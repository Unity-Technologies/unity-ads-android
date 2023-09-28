package com.unity3d.services.ads.topics

import android.adservices.topics.GetTopicsResponse
import android.adservices.topics.Topic
import android.annotation.SuppressLint
import android.os.OutcomeReceiver
import com.unity3d.services.core.log.DeviceLog
import com.unity3d.services.core.webview.WebViewEventCategory
import com.unity3d.services.core.webview.bridge.IEventSender
import org.json.JSONArray
import org.json.JSONObject

@SuppressLint("NewApi", "MissingPermission")
class TopicsReceiver(private val eventSender: IEventSender) : OutcomeReceiver<GetTopicsResponse, Exception> {
    override fun onResult(result: GetTopicsResponse) {
        val resultArray = JSONArray()
        result.topics.forEach {
            resultArray.put(formatTopic(it))
        }
        eventSender.sendEvent(WebViewEventCategory.TOPICS, TopicsEvents.TOPICS_AVAILABLE, resultArray.toString())
    }

    override fun onError(error: Exception) {
        DeviceLog.debug("GetTopics exception: $error")
        eventSender.sendEvent(WebViewEventCategory.TOPICS, TopicsEvents.NOT_AVAILABLE, TopicsErrors.ERROR_EXCEPTION, error.toString())
    }

    fun formatTopic(topic: Topic): JSONObject {
        val resultObject = JSONObject()
        resultObject.put("taxonomyVersion", topic.taxonomyVersion)
        resultObject.put("modelVersion", topic.modelVersion)
        resultObject.put("topicId", topic.topicId)
        return resultObject
    }
}
