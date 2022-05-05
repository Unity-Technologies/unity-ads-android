package com.unity3d.scar.adapter.common;

import com.unity3d.scar.adapter.common.scarads.ScarAdMetadata;

import org.junit.Assert;
import org.junit.Test;

public class GMAAdsErrorTest {
	private static final String TEST_PLACEMENT = "video";
	private static final String TEST_QUERYID = "query";
	private static final String TEST_MESSAGE = "error message";

	@Test
	public void testAdapterCreationError() {
		String errorMessage = "Scar version unsupported";
		GMAAdsError gmaAdsError = GMAAdsError.AdapterCreationError(errorMessage);
		validateGmaAdsError(gmaAdsError, GMAEvent.SCAR_UNSUPPORTED, errorMessage);
	}

	@Test
	public void testNoAdsError() {
		String formattedErrorMessage = String.format("Could not find ad for placement '", TEST_PLACEMENT, "'");
		GMAAdsError gmaAdsError = GMAAdsError.NoAdsError(TEST_PLACEMENT, TEST_QUERYID, formattedErrorMessage);
		validateGmaAdsError(gmaAdsError, GMAEvent.NO_AD_ERROR, formattedErrorMessage, TEST_PLACEMENT, TEST_QUERYID, formattedErrorMessage);
	}

	@Test
	public void testNoAdLoadedError() {
		ScarAdMetadata scarAdMetadata = new ScarAdMetadata(TEST_PLACEMENT, TEST_QUERYID);
		GMAAdsError gmaAdsError = GMAAdsError.AdNotLoadedError(scarAdMetadata);
		String formattedErrorMessage = String.format(GMAAdsError.AD_NOT_LOADED_MESSAGE, TEST_PLACEMENT);
		validateGmaAdsError(gmaAdsError, GMAEvent.AD_NOT_LOADED_ERROR, formattedErrorMessage, TEST_PLACEMENT, TEST_QUERYID, formattedErrorMessage);
	}

	@Test
	public void testInternalShowError() {
		ScarAdMetadata scarAdMetadata = new ScarAdMetadata(TEST_PLACEMENT, TEST_QUERYID);
		String errorMessage = "Scar Adapter object is null";
		GMAAdsError gmaAdsError = GMAAdsError.InternalShowError(scarAdMetadata, errorMessage);
		validateGmaAdsError(gmaAdsError, GMAEvent.INTERNAL_SHOW_ERROR, errorMessage, TEST_PLACEMENT, TEST_QUERYID, errorMessage);
	}

	@Test
	public void testQueryNotFoundError() {
		ScarAdMetadata scarAdMetadata = new ScarAdMetadata(TEST_PLACEMENT, TEST_QUERYID);
		GMAAdsError gmaAdsError = GMAAdsError.QueryNotFoundError(scarAdMetadata);
		String formattedErrorMessage = String.format(GMAAdsError.MISSING_QUERYINFO_MESSAGE, TEST_PLACEMENT);
		validateGmaAdsError(gmaAdsError, GMAEvent.QUERY_NOT_FOUND_ERROR, formattedErrorMessage, TEST_PLACEMENT, TEST_QUERYID, formattedErrorMessage);
	}

	@Test
	public void testInternalLoadError() {
		ScarAdMetadata scarAdMetadata = new ScarAdMetadata(TEST_PLACEMENT, TEST_QUERYID);
		String errorMessage = "Scar Adapter object is null";
		GMAAdsError gmaAdsError = GMAAdsError.InternalLoadError(scarAdMetadata, errorMessage);
		validateGmaAdsError(gmaAdsError, GMAEvent.INTERNAL_LOAD_ERROR, errorMessage, TEST_PLACEMENT, TEST_QUERYID, errorMessage);
	}

	@Test
	public void testAdError() {
		GMAAdsError gmaAdsError = GMAAdsError.NoAdsError(TEST_PLACEMENT, TEST_QUERYID, TEST_MESSAGE);
		validateGmaAdsError(gmaAdsError, GMAEvent.NO_AD_ERROR, TEST_MESSAGE, TEST_PLACEMENT, TEST_QUERYID, TEST_MESSAGE);
	}

	@Test
	public void testSignalsError() {
		GMAAdsError gmaAdsError = GMAAdsError.InternalSignalsError(TEST_MESSAGE);
		validateGmaAdsError(gmaAdsError, GMAEvent.INTERNAL_SIGNALS_ERROR, TEST_MESSAGE, TEST_MESSAGE);
	}

	private void validateGmaAdsError(GMAAdsError gmaAdsError, Enum eventCategory, String description, Object... errorArguments) {
		Assert.assertEquals("GMA", gmaAdsError.getDomain());
		Assert.assertEquals(eventCategory, gmaAdsError.getErrorCategory());
		Assert.assertArrayEquals(errorArguments, gmaAdsError.getErrorArguments());
		Assert.assertEquals(description, gmaAdsError.getDescription());
	}
}
