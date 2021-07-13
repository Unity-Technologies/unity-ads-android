package com.unity3d.services.ads.gmascar;

import com.unity3d.scar.adapter.common.GMAAdsError;
import com.unity3d.scar.adapter.common.GMAEvent;
import com.unity3d.scar.adapter.common.IScarAdapter;
import com.unity3d.scar.adapter.common.scarads.ScarAdMetadata;
import com.unity3d.services.ads.gmascar.adapters.ScarAdapterFactory;
import com.unity3d.services.ads.gmascar.bridges.AdapterStatusBridge;
import com.unity3d.services.ads.gmascar.bridges.InitializationStatusBridge;
import com.unity3d.services.ads.gmascar.bridges.InitializeListenerBridge;
import com.unity3d.services.ads.gmascar.bridges.MobileAdsBridge;
import com.unity3d.services.ads.gmascar.finder.GMAInitializer;
import com.unity3d.services.ads.gmascar.finder.PresenceDetector;
import com.unity3d.services.ads.gmascar.finder.ScarVersionFinder;
import com.unity3d.services.ads.gmascar.handlers.ScarInterstitialAdHandler;
import com.unity3d.services.ads.gmascar.handlers.ScarRewardedAdHandler;
import com.unity3d.services.ads.gmascar.handlers.SignalsHandler;
import com.unity3d.services.ads.gmascar.handlers.WebViewErrorHandler;
import com.unity3d.services.core.properties.ClientProperties;

/**
 * Adapter bridge that uses the Scar Adapter module based on the version of the GMA SDK in the developer's game.
 */
public class GMAScarAdapterBridge {

	private IScarAdapter _scarAdapter;
	private MobileAdsBridge _mobileAdsBridge;
	private ScarVersionFinder _scarVersionFinder;

	private InitializeListenerBridge _initializationListenerBridge;
	private InitializationStatusBridge _initializationStatusBridge;
	private AdapterStatusBridge _adapterStatusBridge;
	private PresenceDetector _presenceDetector;
	private GMAInitializer _gmaInitializer;
	private WebViewErrorHandler _webViewErrorHandler;
	private ScarAdapterFactory _scarAdapterFactory;

	public GMAScarAdapterBridge() {
		_mobileAdsBridge = new MobileAdsBridge();
		_initializationStatusBridge = new InitializationStatusBridge();
		_initializationListenerBridge = new InitializeListenerBridge();
		_adapterStatusBridge = new AdapterStatusBridge();
		_webViewErrorHandler = new WebViewErrorHandler();
		_scarAdapterFactory = new ScarAdapterFactory();
		_presenceDetector = new PresenceDetector(_mobileAdsBridge, _initializationListenerBridge, _initializationStatusBridge, _adapterStatusBridge);
		_gmaInitializer = new GMAInitializer(_mobileAdsBridge, _initializationListenerBridge, _initializationStatusBridge, _adapterStatusBridge);
		_scarVersionFinder = new ScarVersionFinder(_mobileAdsBridge, _presenceDetector, _gmaInitializer);
	}

	public void initializeScar() {
		if (_presenceDetector.areGMAClassesPresent()) {
			_gmaInitializer.initializeGMA();
		} else {
			_webViewErrorHandler.handleError(new GMAAdsError(GMAEvent.INIT_ERROR));
		}
	}

	public boolean isInitialized() {
		return _gmaInitializer.isInitialized();
	}

	public void getVersion() {
		_scarVersionFinder.getVersion();
	}

	public void getSCARSignals(String[] interstitialList, String[] rewardedList) {
		_scarAdapter = getScarAdapterObject();
		SignalsHandler signalListener = new SignalsHandler();

		if (_scarAdapter != null) {
			_scarAdapter.getSCARSignals(ClientProperties.getApplicationContext(), interstitialList, rewardedList, signalListener);
		} else {
			_webViewErrorHandler.handleError(GMAAdsError.InternalSignalsError("Could not create SCAR adapter object"));
		}
	}

	public void load(final boolean canSkip, final String placementId, final String queryId, final String adString, final String adUnitId, final int videoLengthMs) {
		ScarAdMetadata scarAdMetadata = new ScarAdMetadata(placementId, queryId, adUnitId, adString, videoLengthMs);
		_scarAdapter = getScarAdapterObject();
		if (_scarAdapter != null) {
			if (canSkip) {
				loadInterstitialAd(scarAdMetadata);
			} else {
				loadRewardedAd(scarAdMetadata);
			}
		} else {
			_webViewErrorHandler.handleError(GMAAdsError.InternalLoadError(scarAdMetadata, "Scar Adapter object is null"));
		}
	}

	private void loadInterstitialAd(final ScarAdMetadata scarAdMetadata) {
		ScarInterstitialAdHandler adListener = new ScarInterstitialAdHandler(scarAdMetadata);
		_scarAdapter.loadInterstitialAd(ClientProperties.getApplicationContext(), scarAdMetadata, adListener);
	}

	private void loadRewardedAd(final ScarAdMetadata scarAdMetadata) {
		ScarRewardedAdHandler adListener = new ScarRewardedAdHandler(scarAdMetadata);
		_scarAdapter.loadRewardedAd(ClientProperties.getApplicationContext(), scarAdMetadata, adListener);
	}

	public void show(final String placementId, final String queryId, final boolean canSkip) {
		ScarAdMetadata scarAdMetadata = new ScarAdMetadata(placementId, queryId);
		_scarAdapter = getScarAdapterObject();
		if (_scarAdapter != null) {
			_scarAdapter.show(ClientProperties.getActivity(), queryId, placementId);
		} else {
			_webViewErrorHandler.handleError(GMAAdsError.InternalShowError(scarAdMetadata, "Scar Adapter object is null"));
		}
	}

	private IScarAdapter getScarAdapterObject() {
		if (_scarAdapter == null) {
			long minorVersion = _scarVersionFinder.getGoogleSdkVersionCode();
			_scarAdapter = _scarAdapterFactory.createScarAdapter(minorVersion, _webViewErrorHandler);
		}
		return _scarAdapter;
	}

}

