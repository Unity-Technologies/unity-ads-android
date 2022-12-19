package com.unity3d.scar.adapter.v2000.signals;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.gms.ads.query.QueryInfo;
import com.unity3d.scar.adapter.common.signals.ISignalCollectionListener;
import com.unity3d.scar.adapter.common.signals.SignalsStorage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Matchers.any;

@RunWith(MockitoJUnitRunner.class)
public class SignalsCollectorTest {

	private Context context = InstrumentationRegistry.getInstrumentation().getContext();
	private ISignalCollectionListener _signalCollectionListener;

	@Before
	public void before() {
		_signalCollectionListener = Mockito.mock(ISignalCollectionListener.class);
	}

	@Test
	public void testGetScarSignals() {
		SignalsCollector signalsCollector = new SignalsCollector(new SignalsStorage<QueryInfo>());
		signalsCollector.getSCARSignals(context, new String[]{"video"}, new String[]{"rewarded"}, _signalCollectionListener);
		Mockito.verify(_signalCollectionListener, Mockito.timeout(10000).times(1)).onSignalsCollected(any(String.class));
	}

	@Test
	public void testGetScarSignalsNoRewarded() {
		SignalsCollector signalsCollector = new SignalsCollector(new SignalsStorage<QueryInfo>());
		signalsCollector.getSCARSignals(context, new String[]{"video"}, new String[]{}, _signalCollectionListener);
		Mockito.verify(_signalCollectionListener, Mockito.timeout(10000).times(1)).onSignalsCollected(any(String.class));
	}

	@Test
	public void testGetScarSignalsNoInterstitial() {
		SignalsCollector signalsCollector = new SignalsCollector(new SignalsStorage<QueryInfo>());
		signalsCollector.getSCARSignals(context, new String[]{}, new String[]{"rewarded"}, _signalCollectionListener);
		Mockito.verify(_signalCollectionListener, Mockito.timeout(10000).times(1)).onSignalsCollected(any(String.class));
	}
}
