package com.unity3d.services.core.domain.task

import com.unity3d.services.core.configuration.Configuration
import com.unity3d.services.core.configuration.Experiments
import com.unity3d.services.core.configuration.ExperimentsReader
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain

import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before

@OptIn(ExperimentalCoroutinesApi::class)
class InitializeStateConfigTest {
    // Injected into InitializeStateConfig constructor
    var dispatchers: TestSDKDispatchers = TestSDKDispatchers()

    @MockK
    lateinit var initializeStateConfigWithLoaderMock: InitializeStateConfigWithLoader

    @MockK
    lateinit var configMock: Configuration

    @MockK
    lateinit var experimentsMock: Experiments

    @MockK
    lateinit var experimentsReaderMock: ExperimentsReader

    @InjectMockKs
    lateinit var initializeStateConfig: InitializeStateConfig

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        every { configMock.experiments } returns experimentsMock
        every { configMock.experimentsReader } returns experimentsReaderMock
        every { experimentsReaderMock.currentlyActiveExperiments } returns experimentsMock
        coEvery { initializeStateConfigWithLoaderMock(any()) } returns configMock
        Dispatchers.setMain(dispatchers.main)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

}