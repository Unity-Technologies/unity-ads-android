package com.unity3d.services.banners

import android.content.Context
import com.unity3d.services.core.misc.ViewUtilities
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class UnityBannerSizeTest {
    @MockK
    lateinit var contextMock: Context

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { contextMock.resources.displayMetrics } returns mockk()
    }

    @Test
    fun getDynamicSize_displayWidthGt728_returnsLeaderboardBannerSize() {
        mockkStatic(ViewUtilities::class) {
            // given
            every { ViewUtilities.dpFromPx(any(), any()) } returns 729f

            // when
            val bannerSize = UnityBannerSize.getDynamicSize(contextMock)

            // then
            assertEquals(bannerSize, UnityBannerSize.leaderboard)
        }
    }

    @Test
    fun getDynamicSize_displayWidthGt468_returnsIabStandardBannerSize() {
        mockkStatic(ViewUtilities::class) {
            // given
            every { ViewUtilities.dpFromPx(any(), any()) } returns 469f

            // when
            val bannerSize = UnityBannerSize.getDynamicSize(contextMock)

            // then
            assertEquals(bannerSize, UnityBannerSize.iabStandard)
        }
    }

    @Test
    fun getDynamicSize_displayWidthLt468_returnsStandardBannerSize() {
        mockkStatic(ViewUtilities::class) {
            // given
            every { ViewUtilities.dpFromPx(any(), any()) } returns 467f

            // when
            val bannerSize = UnityBannerSize.getDynamicSize(contextMock)

            // then
            assertEquals(bannerSize, UnityBannerSize.standard)
        }
    }
}