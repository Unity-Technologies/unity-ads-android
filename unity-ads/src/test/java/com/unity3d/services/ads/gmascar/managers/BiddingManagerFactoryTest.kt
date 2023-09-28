package com.unity3d.services.ads.gmascar.managers

import com.unity3d.ads.IUnityAdsTokenListener
import com.unity3d.services.ads.gmascar.GMA
import com.unity3d.services.core.configuration.IExperiments
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class BiddingManagerFactoryTest {

    private lateinit var biddingManagerFactory: BiddingManagerFactory

    @MockK
    private lateinit var experiments: IExperiments

    @MockK
    private lateinit var gma: GMA

    @BeforeTest
    fun setup() {
        biddingManagerFactory = BiddingManagerFactory.getInstance()

        MockKAnnotations.init(this, relaxed = true)

        mockkStatic(GMA::class)
        every { GMA.getInstance() } returns gma
    }

    @AfterTest
    fun tearDown() {
        unmockkStatic(GMA::class)
    }

    @Test
    fun createManagerScarSupportedNullExperiments() {
        every { gma.hasSCARBiddingSupport() } returns true
        val manager = biddingManagerFactory.createManager(IUnityAdsTokenListener { }, null)
        assertThat(manager, instanceOf(BiddingEagerManager::class.java))
    }

    @Test
    fun createManagerScarUnsupportedNullExperiments() {
        every { gma.hasSCARBiddingSupport() } returns false
        val manager = biddingManagerFactory.createManager(IUnityAdsTokenListener { }, null)
        assertThat(manager, instanceOf(BiddingDisabledManager::class.java))
    }

    @Test
    fun createManagerScarUnsupportedValidExperiments() {
        every { gma.hasSCARBiddingSupport() } returns false
        val manager = biddingManagerFactory.createManager(IUnityAdsTokenListener { }, experiments)
        assertThat(manager, instanceOf(BiddingDisabledManager::class.java))
    }

    @Test
    fun createManagerScarSupportedExperimentTrue() {
        every { gma.hasSCARBiddingSupport() } returns true
        every { experiments.isScarBannerHbEnabled } returns true

        val manager = biddingManagerFactory.createManager(IUnityAdsTokenListener { }, experiments)
        assertThat(manager, instanceOf(BiddingEagerManager::class.java))
    }

    @Test
    fun createManagerScarSupportedExperimentFalse() {
        every { gma.hasSCARBiddingSupport() } returns true
        every { experiments.isScarBannerHbEnabled } returns false

        val manager = biddingManagerFactory.createManager(IUnityAdsTokenListener { }, experiments)
        assertThat(manager, instanceOf(BiddingEagerManager::class.java))
    }
}