package com.unity3d.services.core.misc;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

@RunWith(MockitoJUnitRunner.class)
public class JsonStorageAggregatorTest {
	private static final String PUBLIC_KEY = "publicKey";
	private static final String PUBLIC_DATA = "publicData";
	private static final String PRIVATE_KEY = "privateKey";
	private static final String PRIVATE_DATA = "privateData";

	@Mock
	private IJsonStorageReader jsonStoragePublicMock;

	@Mock
	private IJsonStorageReader jsonStoragePrivateMock;

	@Before
	public void setup() {
		Mockito.when(jsonStoragePublicMock.get(PUBLIC_KEY)).thenReturn(PUBLIC_DATA);
		Mockito.when(jsonStoragePrivateMock.get(PRIVATE_KEY)).thenReturn(PRIVATE_DATA);
	}

	@Test
	public void testJsonStorageAggregatorWithNullStorage() {
		JsonStorageAggregator jsonStorageAggregator = new JsonStorageAggregator(Collections.singletonList((IJsonStorageReader) null));
		Assert.assertNull(jsonStorageAggregator.get(PUBLIC_KEY));
		Assert.assertNull(jsonStorageAggregator.get(PRIVATE_KEY));
	}

	@Test
	public void testJsonStorageAggregatorWithSingleStorage() {
		JsonStorageAggregator jsonStorageAggregator = new JsonStorageAggregator(Collections.singletonList(jsonStoragePublicMock));
		Assert.assertEquals(PUBLIC_DATA, jsonStorageAggregator.get(PUBLIC_KEY));
		Assert.assertNull(jsonStorageAggregator.get(PRIVATE_KEY));
	}

	@Test
	public void testJsonStorageAggregatorWithAllStorage() {
		JsonStorageAggregator jsonStorageAggregator = new JsonStorageAggregator(Arrays.asList(jsonStoragePublicMock, jsonStoragePrivateMock));
		Assert.assertEquals(PUBLIC_DATA, jsonStorageAggregator.get(PUBLIC_KEY));
		Assert.assertEquals(PRIVATE_DATA, jsonStorageAggregator.get(PRIVATE_KEY));
	}
}
