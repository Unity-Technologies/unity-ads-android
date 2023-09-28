package com.unity3d.scar.adapter.v2000.signals;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.gms.ads.query.QueryInfo;
import com.unity3d.scar.adapter.common.scarads.UnityAdFormat;
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
	public void testGetScarSignalInterstitial() {
		SignalsCollector signalsCollector = new SignalsCollector(new SignalsStorage<QueryInfo>());
		signalsCollector.getSCARSignal(context, "video", UnityAdFormat.INTERSTITIAL, _signalCollectionListener);
		Mockito.verify(_signalCollectionListener, Mockito.timeout(10000).times(1)).onSignalsCollected(Mockito.contains("{\"video\":"));
	}

	@Test
	public void testGetScarSignalRewarded() {
		SignalsCollector signalsCollector = new SignalsCollector(new SignalsStorage<QueryInfo>());
		signalsCollector.getSCARSignal(context, "rewarded", UnityAdFormat.REWARDED, _signalCollectionListener);
		Mockito.verify(_signalCollectionListener, Mockito.timeout(10000).times(1)).onSignalsCollected(Mockito.contains("{\"rewarded\":"));
	}

	@Test
	public void testGetScarSignalBanner() {
		SignalsCollector signalsCollector = new SignalsCollector(new SignalsStorage<QueryInfo>());
		signalsCollector.getSCARSignal(context, "banner", UnityAdFormat.BANNER, _signalCollectionListener);
		Mockito.verify(_signalCollectionListener, Mockito.timeout(10000).times(1)).onSignalsCollected(Mockito.contains("{\"banner\":"));
	}
}
