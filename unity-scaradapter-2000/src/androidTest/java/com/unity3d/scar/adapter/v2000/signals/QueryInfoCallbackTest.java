package com.unity3d.scar.adapter.v2000.signals;

import com.google.android.gms.ads.query.QueryInfo;
import com.unity3d.scar.adapter.common.DispatchGroup;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class QueryInfoCallbackTest {
	@Mock
	QueryInfoMetadata _queryInfoMetadataMock;

	@Mock
	DispatchGroup _dispatchGroupMock;

	@Mock
	QueryInfo _queryInfoMock;

	@Test
	public void testQueryInfoCallbackSuccess() {
		QueryInfoCallback queryInfoCallback = new QueryInfoCallback(_queryInfoMetadataMock, _dispatchGroupMock);
		queryInfoCallback.onSuccess(_queryInfoMock);
		Mockito.verify(_dispatchGroupMock, Mockito.times(1)).leave();
		Mockito.verify(_queryInfoMetadataMock, Mockito.times(1)).setQueryInfo(_queryInfoMock);
	}

	@Test
	public void testQueryInfoCallbackError() {
		QueryInfoCallback queryInfoCallback = new QueryInfoCallback(_queryInfoMetadataMock, _dispatchGroupMock);
		queryInfoCallback.onFailure("");
		Mockito.verify(_dispatchGroupMock, Mockito.times(1)).leave();
		Mockito.verify(_queryInfoMetadataMock, Mockito.times(1)).setError(Mockito.anyString());

	}
}
