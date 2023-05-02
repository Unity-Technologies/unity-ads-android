package com.unity3d.services.core.domain.task

import com.unity3d.services.ads.token.TokenStorage
import com.unity3d.services.core.configuration.*
import com.unity3d.services.core.extensions.AbortRetryException
import com.unity3d.services.core.request.metrics.SDKMetricsSender
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InitializeStateConfigWithLoaderTest {
    var dispatchers = TestSDKDispatchers()

    @MockK
    lateinit var sdkMetricsSender: SDKMetricsSender

    @MockK
    lateinit var configMock: Configuration

    @MockK
    lateinit var initializeStateNetworkError: InitializeStateNetworkError

    @MockK
    lateinit var tokenStorage: TokenStorage

    @InjectMockKs
    lateinit var initializeStateConfigWithLoader: InitializeStateConfigWithLoader

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        every { configMock.maxRetries } returns 1
        every { configMock.retryScalingFactor } returns 1.0
        every { configMock.retryDelay } returns 1
        every { configMock.unifiedAuctionToken } returns ""
        every { configMock.configUrl } returns "http://unity3d.com"
        Dispatchers.setMain(dispatchers.main)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun doWork_loadConfigurationSucceed_successWithConfig() = runTest {
        mockkConstructor(ConfigurationLoader::class, ConfigurationReader::class) {
            // given
            every { anyConstructed<ConfigurationLoader>().loadConfiguration(any()) } answers {
                firstArg<IConfigurationLoaderListener>().onSuccess(configMock)
            }
            every { anyConstructed<ConfigurationReader>().currentConfiguration } returns configMock
            every { tokenStorage.setInitToken(any()) } returns Unit

            // when
            val configWithLoaderResult = runCatching {
                initializeStateConfigWithLoader(
                    InitializeStateConfigWithLoader.Params(configMock)
                )
            }

            // then
            Assert.assertTrue(configWithLoaderResult.isSuccess)
            Assert.assertEquals(configMock, configWithLoaderResult.getOrNull())
            verify(exactly = 1) { tokenStorage.setInitToken(configMock.unifiedAuctionToken) }
            coVerify(exactly = 0) { initializeStateNetworkError(InitializeStateNetworkError.Params(configMock)) }
        }
    }

    @Test
    fun doWork_loadConfigurationThrows_failsWithRetries() = runTest {
        mockkConstructor(ConfigurationLoader::class, ConfigurationReader::class) {
            // given
            every { anyConstructed<ConfigurationLoader>().loadConfiguration(any()) } throws Exception()
            every { anyConstructed<ConfigurationReader>().currentConfiguration } returns configMock

            every { configMock.maxRetries } returns 5

            // when
            val configWithLoaderResult = runCatching {
                initializeStateConfigWithLoader(
                    InitializeStateConfigWithLoader.Params(configMock)
                )
            }

            // then
            Assert.assertTrue(configWithLoaderResult.isFailure)
            coVerify(exactly = 1) { initializeStateNetworkError.doWork(InitializeStateNetworkError.Params(configMock)) }
            verify(exactly = 5) { anyConstructed<ConfigurationLoader>().loadConfiguration(any()) }
            verify(exactly = 0) { tokenStorage.setInitToken(configMock.unifiedAuctionToken) }
        }
    }

    @Test
    fun doWork_loadConfigurationError_failsWithoutRetry() = runTest {
        mockkConstructor(PrivacyConfigurationLoader::class, ConfigurationReader::class) {
            // given
            every { anyConstructed<PrivacyConfigurationLoader>().loadConfiguration(any()) } answers {
                firstArg<IConfigurationLoaderListener>().onError("")
            }
            every { anyConstructed<ConfigurationReader>().currentConfiguration } returns configMock
            every { configMock.maxRetries } returns 5

            // when
            val configWithLoaderResult = runCatching {
                initializeStateConfigWithLoader(
                    InitializeStateConfigWithLoader.Params(configMock)
                )
            }

            // then
            Assert.assertTrue(configWithLoaderResult.isFailure)
            coVerify(exactly = 0) { initializeStateNetworkError.doWork(InitializeStateNetworkError.Params(configMock)) }
            verify(exactly = 1) { anyConstructed<PrivacyConfigurationLoader>().loadConfiguration(any()) }
            verify(exactly = 0) { tokenStorage.setInitToken(configMock.unifiedAuctionToken) }
        }
    }

    @Test
    fun doWork_loadConfigurationPrivacyAbort_failsWithoutRetry() = runTest {
        mockkConstructor(ConfigurationLoader::class, ConfigurationReader::class, PrivacyConfigurationLoader::class) {
            // given
            val abortResultException = AbortRetryException("GameID locked")
            every { anyConstructed<PrivacyConfigurationLoader>().loadConfiguration(any()) } throws abortResultException
            every { anyConstructed<ConfigurationReader>().currentConfiguration } returns configMock
            every { configMock.maxRetries } returns 5

            // when
            val configWithLoaderResult = runCatching {
                initializeStateConfigWithLoader(
                    InitializeStateConfigWithLoader.Params(configMock)
                )
            }

            // then
            Assert.assertTrue(configWithLoaderResult.isFailure)
            val initException = configWithLoaderResult.exceptionOrNull() as InitializationException
            Assert.assertEquals(abortResultException, initException.originalException)
        }
    }
}
