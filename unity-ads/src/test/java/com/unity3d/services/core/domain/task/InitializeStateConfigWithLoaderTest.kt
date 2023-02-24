package com.unity3d.services.core.domain.task

import com.unity3d.services.ads.token.TokenStorage
import com.unity3d.services.core.configuration.*
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InitializeStateConfigWithLoaderTest {
    // Injected into InitializeStateConfigWithLoader constructor
    var dispatcher = TestCoroutineDispatcher()

    var dispatchers = TestSDKDispatchers(dispatcher, dispatcher, dispatcher)

    private val testScope = TestCoroutineScope(dispatcher)

    @MockK
    lateinit var configMock: Configuration

    @MockK
    lateinit var initializeStateNetworkError: InitializeStateNetworkError

    @InjectMockKs
    lateinit var initializeStateConfigWithLoader: InitializeStateConfigWithLoader

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        every { configMock.maxRetries } returns 1
        every { configMock.retryScalingFactor } returns 1.0
        every { configMock.retryDelay } returns 1
        every { configMock.unifiedAuctionToken } returns ""
    }

    @Test
    fun doWork_loadConfigurationSucceed_successWithConfig() = runBlockingTest {
        mockkStatic(TokenStorage::class) {
            mockkConstructor(ConfigurationLoader::class, ConfigurationReader::class) {
                // given
                every { anyConstructed<ConfigurationLoader>().loadConfiguration(any()) } answers {
                    firstArg<IConfigurationLoaderListener>().onSuccess(configMock)
                }
                every { anyConstructed<ConfigurationReader>().currentConfiguration } returns configMock
                every { TokenStorage.getInstance().setInitToken(any()) } returns Unit

                // when
                val configWithLoaderResult = initializeStateConfigWithLoader(
                    InitializeStateConfigWithLoader.Params(configMock)
                )

                // then
                Assert.assertTrue(configWithLoaderResult.isSuccess)
                Assert.assertEquals(configMock, configWithLoaderResult.getOrNull())
                verify(exactly = 1) { TokenStorage.getInstance().setInitToken(configMock.unifiedAuctionToken) }
                coVerify(exactly = 0) { initializeStateNetworkError(InitializeStateNetworkError.Params(configMock))}
            }
        }
    }

    @Test
    fun doWork_loadConfigurationThrows_failsWithRetries() = testScope.runBlockingTest {
        mockkStatic(TokenStorage::class) {
            mockkConstructor(ConfigurationLoader::class, ConfigurationReader::class) {
                // given
                every { anyConstructed<ConfigurationLoader>().loadConfiguration(any()) } throws Exception()
                every { anyConstructed<ConfigurationReader>().currentConfiguration } returns configMock

                every { configMock.maxRetries } returns 5

                // when
                val configWithLoaderResult = initializeStateConfigWithLoader(
                    InitializeStateConfigWithLoader.Params(configMock)
                )

                // then
                Assert.assertTrue(configWithLoaderResult.isFailure)
                coVerify(exactly = 1) { initializeStateNetworkError(InitializeStateNetworkError.Params(configMock))}
                verify(exactly = 5) { anyConstructed<ConfigurationLoader>().loadConfiguration(any()) }
                verify(exactly = 0) { TokenStorage.getInstance().setInitToken(configMock.unifiedAuctionToken) }
            }
        }
    }

    @Test
    fun doWork_loadConfigurationError_failsWithoutRetry() = testScope.runBlockingTest {
        mockkStatic(TokenStorage::class) {
            mockkConstructor(ConfigurationLoader::class, ConfigurationReader::class) {
                // given
                every { anyConstructed<ConfigurationLoader>().loadConfiguration(any()) } answers {
                    firstArg<IConfigurationLoaderListener>().onError("")
                }
                every { anyConstructed<ConfigurationReader>().currentConfiguration } returns configMock
                every { configMock.maxRetries } returns 5

                // when
                val configWithLoaderResult = initializeStateConfigWithLoader(
                    InitializeStateConfigWithLoader.Params(configMock)
                )

                // then
                Assert.assertTrue(configWithLoaderResult.isFailure)
                coVerify(exactly = 1) { initializeStateNetworkError(InitializeStateNetworkError.Params(configMock))}
                verify(exactly = 1) { anyConstructed<ConfigurationLoader>().loadConfiguration(any()) }
                verify(exactly = 0) { TokenStorage.getInstance().setInitToken(configMock.unifiedAuctionToken) }
            }
        }
    }
}