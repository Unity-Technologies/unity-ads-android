package com.unity3d.services.core.domain.task

import com.unity3d.services.core.configuration.Configuration
import com.unity3d.services.core.configuration.Experiments
import com.unity3d.services.core.configuration.ExperimentsReader
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InitializeStateConfigTest {
    // Injected into InitializeStateConfig constructor
    var dispatchers : TestSDKDispatchers = TestSDKDispatchers()

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
        coEvery {initializeStateConfigWithLoaderMock(any())} returns Result.success(configMock)
    }

}