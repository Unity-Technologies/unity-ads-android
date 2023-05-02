package com.unity3d.services.core.network.mapper

import com.unity3d.services.core.network.model.BodyType
import com.unity3d.services.core.network.model.HttpRequest
import com.unity3d.services.core.network.model.RequestType
import okhttp3.RequestBody
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HttpRequestToOkHttpRequestKtTest {
    @Test
    fun toOkHttpRequest_defaultGet_mapsToOkHttpRequest() {
        // given
        val httpRequest = HttpRequest(
            baseURL = "https://www.google.com",
            path = "/test",
        )

        // when
        val okHttpRequest = httpRequest.toOkHttpRequest()

        // then
        assertEquals("https://www.google.com/test", okHttpRequest.url().toString())
        assertEquals("GET", okHttpRequest.method())
        assertEquals(0, okHttpRequest.headers().size())
        assertEquals(null, okHttpRequest.body())
    }

    @Test
    fun toOkHttpRequest_defaultGet_extraSlash_mapsToOkHttpRequest_removesExtraSlash() {
        // given
        val httpRequest = HttpRequest(
            baseURL = "https://www.google.com",
            path = "/test/",
        )

        // when
        val okHttpRequest = httpRequest.toOkHttpRequest()

        // then
        assertEquals("https://www.google.com/test", okHttpRequest.url().toString())
        assertEquals("GET", okHttpRequest.method())
        assertEquals(0, okHttpRequest.headers().size())
        assertEquals(null, okHttpRequest.body())
    }

    @Test
    fun toOkHttpRequest_defaultGet_mapsToOkHttpRequest_noExtraSlash() {
        // given
        val httpRequest = HttpRequest(
            baseURL = "https://www.google.com",
        )

        // when
        val okHttpRequest = httpRequest.toOkHttpRequest()

        // then
        assertEquals("https://www.google.com/", okHttpRequest.url().toString())
        assertEquals("GET", okHttpRequest.method())
        assertEquals(0, okHttpRequest.headers().size())
        assertEquals(null, okHttpRequest.body())
    }

    @Test
    fun toOkHttpRequest_defaultPost_mapsToOkHttpRequest() {
        // given
        val httpRequest = HttpRequest(
            baseURL = "https://www.google.com",
            path = "/test",
            method = RequestType.POST,
            body = "test",
            bodyType = BodyType.STRING,
        )

        // when
        val okHttpRequest = httpRequest.toOkHttpRequest()

        // then
        assertEquals("https://www.google.com/test", okHttpRequest.url().toString())
        assertEquals("POST", okHttpRequest.method())
        assertEquals(0, okHttpRequest.headers().size())
        assertEquals("test".toByteArray().size.toLong(), okHttpRequest.body()?.contentLength())
    }

    @Test
    fun toOkHttpRequest_defaultPost_bodyByteArray_mapsToOkHttpRequest() {
        // given
        val httpRequest = HttpRequest(
            baseURL = "https://www.google.com",
            path = "/test",
            method = RequestType.POST,
            body = "test".toByteArray(),
            bodyType = BodyType.UNKNOWN,
        )

        // when
        val okHttpRequest = httpRequest.toOkHttpRequest()

        // then
        assertEquals("https://www.google.com/test", okHttpRequest.url().toString())
        assertEquals("POST", okHttpRequest.method())
        assertEquals(0, okHttpRequest.headers().size())
        assertEquals("test".toByteArray().size.toLong(), okHttpRequest.body()?.contentLength())
    }

    @Test
    fun toOkHttpRequest_defaultPost_bodyUnsupportedType_mapsToOkHttpRequest() {
        // given
        val httpRequest = HttpRequest(
            baseURL = "https://www.google.com",
            path = "/test",
            method = RequestType.POST,
            body = 1,
            bodyType = BodyType.UNKNOWN,
        )

        // when
        val okHttpRequest = httpRequest.toOkHttpRequest()

        // then
        assertEquals("https://www.google.com/test", okHttpRequest.url().toString())
        assertEquals("POST", okHttpRequest.method())
        assertEquals(0, okHttpRequest.headers().size())
        assertEquals(0, okHttpRequest.body()?.contentLength())
    }

}