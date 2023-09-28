package com.unity3d.ads.test.instrumentation.services.banners;

import com.unity3d.ads.UnityAds;
import com.unity3d.services.banners.BannerErrorCode;
import com.unity3d.services.banners.BannerErrorInfo;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BannerErrorInfoTest {

	private String errorMessage = "BANNER ERROR MSG";

	@Test
	public void testConvertNativeBannerErrorToLoadError() {
		BannerErrorInfo nativeError = new BannerErrorInfo(errorMessage, BannerErrorCode.NATIVE_ERROR);
		UnityAds.UnityAdsLoadError loadError = nativeError.toLoadError();
		assertEquals(UnityAds.UnityAdsLoadError.INVALID_ARGUMENT, loadError);
	}

	@Test
	public void testConvertNoFillBannerErrorToLoadError() {
		BannerErrorInfo noFillError = new BannerErrorInfo(errorMessage, BannerErrorCode.NO_FILL);
		UnityAds.UnityAdsLoadError loadError = noFillError.toLoadError();
		assertEquals(UnityAds.UnityAdsLoadError.NO_FILL, loadError);
	}

	@Test
	public void testConvertWebViewBannerErrorToLoadError() {
		BannerErrorInfo webViewError = new BannerErrorInfo(errorMessage, BannerErrorCode.WEBVIEW_ERROR);
		UnityAds.UnityAdsLoadError loadError = webViewError.toLoadError();
		assertEquals(UnityAds.UnityAdsLoadError.INTERNAL_ERROR, loadError);
	}

	@Test
	public void testConvertUnknownBannerErrorToLoadError() {
		BannerErrorInfo unknownError = new BannerErrorInfo(errorMessage, BannerErrorCode.UNKNOWN);
		UnityAds.UnityAdsLoadError loadError = unknownError.toLoadError();
		assertEquals(UnityAds.UnityAdsLoadError.INTERNAL_ERROR, loadError);
	}
}
