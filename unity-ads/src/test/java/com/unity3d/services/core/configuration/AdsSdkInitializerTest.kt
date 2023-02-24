package com.unity3d.services.core.configuration

import android.content.Context
import com.unity3d.services.core.properties.ClientProperties
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class AdsSdkInitializerTest {
    @MockK
    lateinit var contextMock: Context

    @MockK
    lateinit var applicationContextMock: Context

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { contextMock.applicationContext } returns applicationContextMock
    }

    @Test
    fun create_validContextPassed_clientContextSetProperly() {
        mockkStatic(ClientProperties::class) {
            // given
            every { ClientProperties.setApplicationContext(any()) } returns Unit

            // when
            val adsSdkInitializer = AdsSdkInitializer()
            adsSdkInitializer.create(contextMock)

            // then
            verify(exactly = 1) { ClientProperties.setApplicationContext(applicationContextMock) }
        }
    }
}