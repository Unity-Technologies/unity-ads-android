package com.unity3d.services.core.domain

import com.unity3d.services.core.configuration.Configuration
import com.unity3d.services.core.configuration.ErrorState
import com.unity3d.services.core.domain.task.InitializationException
import com.unity3d.services.core.domain.task.InitializeStateCreateWithRemote
import io.mockk.mockk
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsInstanceOf.instanceOf
import org.junit.Test
import java.lang.IllegalArgumentException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ResultExtensionsKtTest {

    @Test
    fun getCustomExceptionOrNull_correctExceptionInside_returnsCustomException() {
        // given
        val configMock: Configuration = mockk()
        val result: Result<Nothing> = Result.failure(
           InitializationException(
                ErrorState.CreateWebview,
                Exception("TEST"),
                configMock
            )
        )

        // when
        val exception: InitializationException =
            result.getCustomExceptionOrNull() ?: throw Exception("FAILURE")

        // then
        assertEquals(ErrorState.CreateWebview, exception.errorState)
        assertEquals("TEST", exception.originalException.message)
        assertEquals(configMock, exception.config)
    }

    @Test
    fun getCustomExceptionOrNull_incorrectExceptionInside_returnsNull() {
        // given
        val result: Result<Nothing> = Result.failure(Exception("WRONG"))

        // when
        val exception =
            result.getCustomExceptionOrNull<InitializationException>()

        // then
        assertNull(exception)
    }

    @Test
    fun getCustomExceptionOrThrow_correctExceptionInside_returnsCustomException() {
        // given
        val configMock: Configuration = mockk()
        val result: Result<Nothing> = Result.failure(
            InitializationException(
                ErrorState.CreateWebview,
                Exception("TEST"),
                configMock
            )
        )

        // when
        val exception: InitializationException =
            result.getCustomExceptionOrThrow()

        // then
        assertEquals(ErrorState.CreateWebview, exception.errorState)
        assertEquals("TEST", exception.originalException.message)
        assertEquals(configMock, exception.config)
    }

    @Test
    fun getCustomExceptionOrThrow_incorrectExceptionInside_returnsDefaultException() {
        // given
        val result: Result<Nothing> = Result.failure(Exception("WRONG"))

        // when
        val exception =
            assertFailsWith<IllegalArgumentException> {
                result.getCustomExceptionOrThrow<InitializationException>()
            }

        // then
        assertThat(exception, instanceOf(IllegalArgumentException::class.java))
    }

}