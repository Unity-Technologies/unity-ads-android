package com.unity3d.scar.adapter.common;

import com.unity3d.scar.adapter.common.signals.SignalCallbackListener;
import com.unity3d.scar.adapter.common.signals.SignalsResult;
import com.unity3d.scar.adapter.common.signals.SignalsStorage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SignalCallbackListenerTest {

	@Mock
	Object _queryInfoMock;

	@Mock
	DispatchGroup _dispatchGroupMock;

	@Mock
	SignalsStorage _signalsStorageMock;

	@Mock
	SignalsResult _signalsResultMock;

	SignalCallbackListener<Object> signalCallbackListener;
	SignalCallbackListener<Object> signalCallbackListenerNoStorage;


	private String TEST_PLACEMENT_ID = "video";
	private String TEST_SIGNAL = "ABC123";
	private String TEST_ERROR_MESSAGE = "ERROR: Signal collection failed";

	@Before
	public void setup() {
		signalCallbackListener = new SignalCallbackListener(_dispatchGroupMock, _signalsStorageMock, _signalsResultMock);
		signalCallbackListenerNoStorage = new SignalCallbackListener(_dispatchGroupMock, _signalsResultMock);
	}

	@Test
	public void testOnSuccess() {
		signalCallbackListener.onSuccess(TEST_PLACEMENT_ID, TEST_SIGNAL, _queryInfoMock);
		signalCallbackListenerNoStorage.onSuccess(TEST_PLACEMENT_ID, TEST_SIGNAL, _queryInfoMock);

		Mockito.verify(_signalsStorageMock, Mockito.times(1)).put(TEST_PLACEMENT_ID, _queryInfoMock);
		Mockito.verify(_signalsResultMock, Mockito.times(2)).addToSignalsMap(TEST_PLACEMENT_ID, TEST_SIGNAL);
		Mockito.verify(_dispatchGroupMock, Mockito.times(2)).leave();
	}


	@Test
	public void testOnFailure() {
		signalCallbackListener.onFailure(TEST_ERROR_MESSAGE);
		signalCallbackListenerNoStorage.onFailure(TEST_ERROR_MESSAGE);

		Mockito.verify(_signalsStorageMock, Mockito.times(0)).put(TEST_PLACEMENT_ID, _queryInfoMock);
		Mockito.verify(_signalsResultMock, Mockito.times(0)).addToSignalsMap(TEST_PLACEMENT_ID, TEST_SIGNAL);
		Mockito.verify(_signalsResultMock, Mockito.times(2)).setErrorMessage(TEST_ERROR_MESSAGE);
		Mockito.verify(_dispatchGroupMock, Mockito.times(2)).leave();	}
}
