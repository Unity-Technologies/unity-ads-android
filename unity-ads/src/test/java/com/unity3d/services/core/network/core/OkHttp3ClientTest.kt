package com.unity3d.services.core.network.core

import com.unity3d.services.core.domain.task.TestSDKDispatchers
import com.unity3d.services.core.network.model.BodyType
import com.unity3d.services.core.network.model.HttpRequest
import com.unity3d.services.core.network.model.RequestType
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.InjectMockKs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
class OkHttp3ClientTest {
    private val dispatchers = TestSDKDispatchers()

    private lateinit var mockWebServer: MockWebServer
    private lateinit var okHttpClient: OkHttpClient

    @InjectMockKs
    private lateinit var okHttp3Client: OkHttp3Client

    @Before
    fun setUp() {
        okHttpClient = OkHttpClient()
        MockKAnnotations.init(this)
        Dispatchers.setMain(dispatchers.main)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun execute_getRequest_returnsStatusCode200() = runTest {
        // given
        mockWebServer = MockWebServer()
        mockWebServer.enqueue(MockResponse().setResponseCode(200))
        mockWebServer.start()

        val httpRequest = HttpRequest(
            baseURL = mockWebServer.url("/test").toString(),
            connectTimeout = 1000,
            readTimeout = 1000
        )

        // when
        val response = okHttp3Client.execute(httpRequest)

        // then
        assertEquals(200, response.statusCode)
    }

    @Test
    fun execute_postRequest_returnsStatusCode200() = runTest {
        // given
        mockWebServer = MockWebServer()
        mockWebServer.enqueue(MockResponse().setResponseCode(200))
        mockWebServer.start()

        val httpRequest = HttpRequest(
            baseURL = mockWebServer.url("/test").toString(),
            connectTimeout = 1000,
            readTimeout = 1000,
            method = RequestType.POST,
            bodyType = BodyType.STRING,
            body = "test"
        )

        // when
        val response = okHttp3Client.execute(httpRequest)

        // then
        val request = mockWebServer.takeRequest()

        assertEquals("test", request.body.readUtf8())
        assertEquals(200, response.statusCode)
    }

    @Test
    fun execute_readTimeout_throwsError() = runTest {
        // given
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val httpRequest = HttpRequest(
            baseURL = mockWebServer.url("/test").toString(),
            connectTimeout = 1000,
            readTimeout = 1000
        )

        // then
        assertFailsWith<IOException> { okHttp3Client.execute(httpRequest) }
    }
}