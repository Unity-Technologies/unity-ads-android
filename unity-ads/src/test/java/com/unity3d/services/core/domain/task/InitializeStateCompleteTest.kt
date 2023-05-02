package com.unity3d.services.core.domain.task

import com.unity3d.services.core.configuration.Configuration
import com.unity3d.services.core.configuration.IModuleConfiguration
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class InitializeStateCompleteTest {
    var dispatchers = TestSDKDispatchers()

    @MockK
    lateinit var moduleConfigurationMock: IModuleConfiguration

    @MockK
    lateinit var configMock: Configuration

    @InjectMockKs
    lateinit var initializeStateComplete: InitializeStateComplete

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { configMock.moduleConfigurationList } returns arrayOf(String::class.java)
        Dispatchers.setMain(dispatchers.main)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun doWork_moduleConfigurationNull_Success() = runTest {
        // given
        every { configMock.getModuleConfiguration(any()) } returns null

        // when
        val result = runCatching { initializeStateComplete(InitializeStateComplete.Params(configMock)) }

        // then
        assertTrue(result.isSuccess)
    }

    @Test
    fun doWork_moduleConfigurationCompletes_CalledModuleConfigSuccess() = runTest {
        // given
        every { configMock.getModuleConfiguration(any()) } returns moduleConfigurationMock
        every { moduleConfigurationMock.initCompleteState(any()) } returns true

        // when
        val result = runCatching { initializeStateComplete(InitializeStateComplete.Params(configMock)) }

        // then
        assertTrue(result.isSuccess)
        verify(exactly = 1) { moduleConfigurationMock.initCompleteState(any()) }
    }
}