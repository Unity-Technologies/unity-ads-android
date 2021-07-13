package com.unity3d.scar.adapter.v2000.signals;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SignalsStorageTest {
	@Mock
	QueryInfoMetadata _queryInfoMetadaMock;

	@Test
	public void testSignalStorage() {
		SignalsStorage signalsStorage = new SignalsStorage();
		signalsStorage.put("video", _queryInfoMetadaMock);
		QueryInfoMetadata storedMetadata = signalsStorage.getQueryInfoMetadata("video");
		Assert.assertEquals(_queryInfoMetadaMock, storedMetadata);
	}
}
