package com.unity3d.scar.adapter.v2000.signals;

import com.google.android.gms.ads.query.QueryInfo;
import com.unity3d.scar.adapter.common.signals.ISignalCallbackListener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class QueryInfoCallbackTest {
	@Mock
	ISignalCallbackListener _signalCallbackListenerMock;

	@Mock
	QueryInfo _queryInfoMock;

	QueryInfoCallback queryInfoCallback;

	private String TEST_PLACEMENT_ID = "video";
	private String TEST_QUERY = "ABC123";
	private String TEST_ERROR_MESSAGE = "ERROR: Signal collection failed";

	@Before
	public void setup() {
		queryInfoCallback = new QueryInfoCallback(TEST_PLACEMENT_ID, _signalCallbackListenerMock);
	}

	@Test
	public void testQueryInfoCallbackSuccess() {
		Mockito.when(_queryInfoMock.getQuery()).thenReturn(TEST_QUERY);
		queryInfoCallback.onSuccess(_queryInfoMock);
		Mockito.verify(_signalCallbackListenerMock, Mockito.times(1)).onSuccess(TEST_PLACEMENT_ID, TEST_QUERY, _queryInfoMock);
	}

	@Test
	public void testQueryInfoCallbackError() {
		queryInfoCallback.onFailure(TEST_ERROR_MESSAGE);
		Mockito.verify(_signalCallbackListenerMock, Mockito.times(1)).onFailure(TEST_ERROR_MESSAGE);
	}
}
