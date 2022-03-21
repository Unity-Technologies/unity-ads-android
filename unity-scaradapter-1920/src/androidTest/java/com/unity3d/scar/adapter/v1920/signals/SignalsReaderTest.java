package com.unity3d.scar.adapter.v1920.signals;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.unity3d.scar.adapter.common.signals.ISignalCollectionListener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Matchers.any;

@RunWith(MockitoJUnitRunner.class)
public class SignalsReaderTest {
	private Context context = InstrumentationRegistry.getInstrumentation().getContext();
	private ISignalCollectionListener _signalCollectionListener;

	@Before
	public void before() {
		_signalCollectionListener = Mockito.mock(ISignalCollectionListener.class);
	}

	@Test
	public void testGetScarSignals() {
		SignalsReader signalsReader = new SignalsReader(new SignalsStorage());
		signalsReader.getSCARSignals(context, new String[]{"video"}, new String[]{"rewarded"}, _signalCollectionListener);
		Mockito.verify(_signalCollectionListener, Mockito.timeout(10000).times(1)).onSignalsCollected(any(String.class));
	}

	@Test
	public void testGetScarSignalsNoRewarded() {
		SignalsReader signalsReader = new SignalsReader(new SignalsStorage());
		signalsReader.getSCARSignals(context, new String[]{"video"}, new String[]{}, _signalCollectionListener);
		Mockito.verify(_signalCollectionListener, Mockito.timeout(10000).times(1)).onSignalsCollected(any(String.class));
	}

	@Test
	public void testGetScarSignalsNoInterstitial() {
		SignalsReader signalsReader = new SignalsReader(new SignalsStorage());
		signalsReader.getSCARSignals(context, new String[]{}, new String[]{"rewarded"}, _signalCollectionListener);
		Mockito.verify(_signalCollectionListener, Mockito.timeout(10000).times(1)).onSignalsCollected(any(String.class));
	}

}
