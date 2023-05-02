package com.unity3d.services.banners

import android.content.Context
import com.unity3d.services.core.misc.ViewUtilities
import kotlin.math.roundToInt

/**
 * Banner sizes supported by Unity Ads.
 *
 * Custom sizes can be created by using the [UnityBannerSize] constructor.
 *
 * @see <a href="https://docs.unity.com/demand-side-partners/en/manual/BannerAds">Unity Banner Ads</a>
 */
class UnityBannerSize(val width: Int, val height: Int) {
    companion object {
        val leaderboard = UnityBannerSize(728, 90)
        val iabStandard = UnityBannerSize(468, 60)
        val standard = UnityBannerSize(320, 50)

        /**
        * Returns the largest banner size possible for the current display width.
         *
        * @param context The context used to retrieve the display width.
        */
        @JvmStatic
        fun getDynamicSize(context: Context): UnityBannerSize {
            val screenWidth = ViewUtilities.dpFromPx(
                context,
                context.resources.displayMetrics.widthPixels.toFloat()
            ).roundToInt()

            return when {
                screenWidth >= leaderboard.width -> leaderboard
                screenWidth >= iabStandard.width -> iabStandard
                else -> standard
            }
        }
    }
}
