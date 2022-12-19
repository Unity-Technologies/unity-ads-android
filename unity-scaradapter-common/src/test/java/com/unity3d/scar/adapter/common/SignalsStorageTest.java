package com.unity3d.scar.adapter.common;

import com.unity3d.scar.adapter.common.signals.SignalsStorage;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class SignalsStorageTest {
	private static final String TEST_PLACEMENTID = "placementId";

	// We cannot access QueryInfo from the common module
	private Object objectMock = Mockito.mock(Object.class);

	@Test
	public void testSignalStorage() {
		SignalsStorage signalsStorage = new SignalsStorage();
		signalsStorage.put(TEST_PLACEMENTID, objectMock);
		Object storedInfo = signalsStorage.getQueryInfo(TEST_PLACEMENTID);
		Assert.assertEquals(storedInfo, objectMock);
	}

	@Test
	public void testSignalStorageDuplicatePlacements() {
		SignalsStorage signalsStorage = new SignalsStorage();
		signalsStorage.put(TEST_PLACEMENTID, objectMock);
		signalsStorage.put(TEST_PLACEMENTID, objectMock);
		Object queryInfo = signalsStorage.getQueryInfo(TEST_PLACEMENTID);
		Assert.assertEquals(queryInfo, objectMock);
	}
}
