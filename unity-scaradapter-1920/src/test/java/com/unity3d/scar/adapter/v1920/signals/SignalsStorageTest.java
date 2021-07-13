package com.unity3d.scar.adapter.v1920.signals;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

public class SignalsStorageTest {
	private static final String TEST_PLACEMENTID = "placementId";
	private static final String TEST_PLACEMENTID2 = "placementId2";

	private QueryInfoMetadata queryInfoMetadataMock = Mockito.mock(QueryInfoMetadata.class);

	@Test
	public void testSignalStorage() {
		SignalsStorage signalsStorage = new SignalsStorage();
		signalsStorage.put(TEST_PLACEMENTID, queryInfoMetadataMock);
		QueryInfoMetadata queryInfoMetadata = signalsStorage.getQueryInfoMetadata(TEST_PLACEMENTID);
		Assert.assertEquals(queryInfoMetadata, queryInfoMetadataMock);
	}

	@Test
	public void testSignalStorageMultiplePlacements() {
		SignalsStorage signalsStorage = new SignalsStorage();
		signalsStorage.put(TEST_PLACEMENTID, queryInfoMetadataMock);
		signalsStorage.put(TEST_PLACEMENTID2, queryInfoMetadataMock);
		Map<String, QueryInfoMetadata> expectedStorageContent = new HashMap<String, QueryInfoMetadata>() {
			{
				put(TEST_PLACEMENTID, queryInfoMetadataMock);
				put(TEST_PLACEMENTID2, queryInfoMetadataMock);
			}
		};
		Map<String, QueryInfoMetadata> queryInfoMetadataMap = signalsStorage.getPlacementQueryInfoMap();
		Assert.assertEquals(expectedStorageContent, queryInfoMetadataMap);
	}

	@Test
	public void testSignalStorageDuplicatePlacements() {
		SignalsStorage signalsStorage = new SignalsStorage();
		signalsStorage.put(TEST_PLACEMENTID, queryInfoMetadataMock);
		signalsStorage.put(TEST_PLACEMENTID, queryInfoMetadataMock);
		QueryInfoMetadata queryInfoMetadata = signalsStorage.getQueryInfoMetadata(TEST_PLACEMENTID);
		Assert.assertEquals(queryInfoMetadata, queryInfoMetadataMock);
	}
}
