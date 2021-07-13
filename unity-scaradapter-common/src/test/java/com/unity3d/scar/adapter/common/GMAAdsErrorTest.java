package com.unity3d.scar.adapter.common;

import com.unity3d.scar.adapter.common.scarads.ScarAdMetadata;

import org.junit.Assert;
import org.junit.Test;

public class GMAAdsErrorTest {
	private static final String TEST_PLACEMENT = "video";
	private static final String TEST_QUERYID = "query";
	private static final String TEST_MESSAGE = "error message";

	@Test
	public void testInternalShowError() {
		ScarAdMetadata scarAdMetadata = new ScarAdMetadata(TEST_PLACEMENT, TEST_QUERYID);
		GMAAdsError gmaAdsError = GMAAdsError.InternalShowError(scarAdMetadata);
		String formattedErrorMessage = String.format(GMAAdsError.INTERNAL_SHOW_MESSAGE_NOT_LOADED, TEST_PLACEMENT);
		validateGmaAdsError(gmaAdsError, GMAEvent.INTERNAL_SHOW_ERROR, formattedErrorMessage, TEST_PLACEMENT, TEST_QUERYID, formattedErrorMessage);
	}

	@Test
	public void testInternalLoadError() {
		ScarAdMetadata scarAdMetadata = new ScarAdMetadata(TEST_PLACEMENT, TEST_QUERYID);
		GMAAdsError gmaAdsError = GMAAdsError.InternalLoadError(scarAdMetadata);
		String formattedErrorMessage = String.format(GMAAdsError.INTERNAL_LOAD_MESSAGE_MISSING_QUERYINFO, TEST_PLACEMENT);
		validateGmaAdsError(gmaAdsError, GMAEvent.INTERNAL_LOAD_ERROR, formattedErrorMessage, TEST_PLACEMENT, TEST_QUERYID, formattedErrorMessage);
	}

	@Test
	public void testAdError() {
		GMAAdsError gmaAdsError = GMAAdsError.NoAdsError(TEST_PLACEMENT, TEST_QUERYID, TEST_MESSAGE);
		validateGmaAdsError(gmaAdsError, GMAEvent.NO_AD_ERROR, TEST_MESSAGE, TEST_PLACEMENT, TEST_QUERYID, TEST_MESSAGE);
	}

	@Test
	public void testSignalsError() {
		GMAAdsError gmaAdsError = GMAAdsError.InternalSignalsError(TEST_MESSAGE);
		validateGmaAdsError(gmaAdsError, GMAEvent.SIGNALS_ERROR, TEST_MESSAGE, TEST_MESSAGE);
	}

	private void validateGmaAdsError(GMAAdsError gmaAdsError, Enum eventCategory, String description, Object... errorArguments) {
		Assert.assertEquals("GMA", gmaAdsError.getDomain());
		Assert.assertEquals(eventCategory, gmaAdsError.getErrorCategory());
		Assert.assertArrayEquals(errorArguments, gmaAdsError.getErrorArguments());
		Assert.assertEquals(description, gmaAdsError.getDescription());
	}
}
