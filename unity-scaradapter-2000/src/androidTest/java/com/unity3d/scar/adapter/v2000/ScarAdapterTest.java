package com.unity3d.scar.adapter.v2000;

import android.app.Activity;
import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;

import com.unity3d.scar.adapter.common.GMAAdsError;
import com.unity3d.scar.adapter.common.IAdsErrorHandler;
import com.unity3d.scar.adapter.common.IScarInterstitialAdListenerWrapper;
import com.unity3d.scar.adapter.common.scarads.ScarAdMetadata;
import com.unity3d.scar.adapter.common.signals.ISignalCollectionListener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static com.unity3d.scar.adapter.v2000.Constants.SCARExampleAdUnitId;
import static com.unity3d.scar.adapter.v2000.Constants.SCARExampleInterstitialAdString;

@RunWith(MockitoJUnitRunner.class)
public class ScarAdapterTest {
	private Context context = InstrumentationRegistry.getInstrumentation().getContext();

	@Mock
	IAdsErrorHandler _adsErrorHandlerMock;

	private ScarAdapter _scarAdapter = new ScarAdapter(_adsErrorHandlerMock);

	@Mock
	ISignalCollectionListener _signalCollectionListenerMock;

	@Mock
	IScarInterstitialAdListenerWrapper _scarInterstitialAdListenerWrapperMock;

	@Test
	public void testScarAdapterGetSignals() {
		_scarAdapter.getSCARSignals(context, new String[]{"video"}, new String[]{"rewarded"}, _signalCollectionListenerMock);
		Mockito.verify(_signalCollectionListenerMock, Mockito.timeout(1000).times(1)).onSignalsCollected(Mockito.anyString());
	}

	@Test
	public void testScarAdapterGetSignalsEmptyInterstitialPlacement() {
		_scarAdapter.getSCARSignals(context, new String[0], new String[]{"rewarded"}, _signalCollectionListenerMock);
		Mockito.verify(_signalCollectionListenerMock, Mockito.timeout(1000).times(1)).onSignalsCollected(Mockito.anyString());
	}

	@Test
	public void testScarAdapterGetSignalsEmptyRewardedPlacement() {
		_scarAdapter.getSCARSignals(context, new String[]{"video"}, new String[0], _signalCollectionListenerMock);
		Mockito.verify(_signalCollectionListenerMock, Mockito.timeout(1000).times(1)).onSignalsCollected(Mockito.anyString());
	}

	@Test
	public void testScarAdapterLoad() {
		_scarAdapter.getSCARSignals(context, new String[]{"video"}, new String[0], _signalCollectionListenerMock);
		Mockito.verify(_signalCollectionListenerMock, Mockito.timeout(1000).times(1)).onSignalsCollected(Mockito.anyString());
		_scarAdapter.loadInterstitialAd(context, getDefaultScarMeta(), _scarInterstitialAdListenerWrapperMock);
		Mockito.verify(_scarInterstitialAdListenerWrapperMock, Mockito.timeout(5000).times(1)).onAdLoaded();
	}

	@Test
	public void testScarAdapterLoadAndShow() {
		_scarAdapter.getSCARSignals(context, new String[]{"video"}, new String[0], _signalCollectionListenerMock);
		Mockito.verify(_signalCollectionListenerMock, Mockito.timeout(1000).times(1)).onSignalsCollected(Mockito.anyString());
		_scarAdapter.loadInterstitialAd(context, getDefaultScarMeta(), _scarInterstitialAdListenerWrapperMock);
		Mockito.verify(_scarInterstitialAdListenerWrapperMock, Mockito.timeout(5000).times(1)).onAdLoaded();
		try(ActivityScenario<Activity> scenario = ActivityScenario.launch(Activity.class)) {
			scenario.onActivity(new ActivityScenario.ActivityAction<Activity>() {
				@Override
				public void perform(Activity activity) {
					_scarAdapter.show(activity, "", "video");
				}
			});
		}
		Mockito.verify(_scarInterstitialAdListenerWrapperMock, Mockito.timeout(20000).times(1)).onAdOpened();
		Mockito.verify(_scarInterstitialAdListenerWrapperMock, Mockito.timeout(20000).times(1)).onAdImpression();
	}

	@Test
	public void testScarAdapterShowWithoutLoad() {
		_scarAdapter = new ScarAdapter(_adsErrorHandlerMock);
		try(ActivityScenario<Activity> scenario = ActivityScenario.launch(Activity.class)) {
			scenario.onActivity(new ActivityScenario.ActivityAction<Activity>() {
				@Override
				public void perform(Activity activity) {
					_scarAdapter.show(activity, "", "video");
				}
			});
		}
		Mockito.verify(_adsErrorHandlerMock, Mockito.timeout(20000).times(1)).handleError(Mockito.any(GMAAdsError.class));
	}

	private ScarAdMetadata getDefaultScarMeta() {
		return new ScarAdMetadata("video", "", SCARExampleAdUnitId, SCARExampleInterstitialAdString, 30);
	}
}