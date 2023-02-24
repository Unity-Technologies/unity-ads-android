package com.unity3d.services.ads.gmascar;

import com.unity3d.scar.adapter.common.GMAAdsError;
import com.unity3d.scar.adapter.common.GMAEvent;
import com.unity3d.scar.adapter.common.IScarAdapter;
import com.unity3d.scar.adapter.common.scarads.ScarAdMetadata;
import com.unity3d.services.ads.gmascar.adapters.ScarAdapterFactory;
import com.unity3d.services.ads.gmascar.bridges.AdapterStatusBridge;
import com.unity3d.services.ads.gmascar.bridges.InitializationStatusBridge;
import com.unity3d.services.ads.gmascar.bridges.InitializeListenerBridge;
import com.unity3d.services.ads.gmascar.bridges.mobileads.MobileAdsBridgeBase;
import com.unity3d.services.ads.gmascar.finder.GMAInitializer;
import com.unity3d.services.ads.gmascar.finder.PresenceDetector;
import com.unity3d.services.ads.gmascar.finder.ScarAdapterVersion;
import com.unity3d.services.ads.gmascar.finder.ScarVersionFinder;
import com.unity3d.services.ads.gmascar.handlers.BiddingSignalsHandler;
import com.unity3d.services.ads.gmascar.handlers.ScarInterstitialAdHandler;
import com.unity3d.services.ads.gmascar.handlers.ScarRewardedAdHandler;
import com.unity3d.services.ads.gmascar.handlers.SignalsHandler;
import com.unity3d.services.ads.gmascar.handlers.WebViewErrorHandler;
import com.unity3d.services.ads.gmascar.utils.GMAEventSender;
import com.unity3d.services.core.misc.EventSubject;
import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.services.core.timer.DefaultIntervalTimerFactory;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Arrays;

/**
 * Adapter bridge that uses the Scar Adapter module based on the version of the GMA SDK in the developer's game.
 */
public class GMAScarAdapterBridge {

	private final ScarVersionFinder _scarVersionFinder;
	private final MobileAdsBridgeBase _mobileAdsBridge;
	private final InitializeListenerBridge _initializationListenerBridge;
	private final InitializationStatusBridge _initializationStatusBridge;
	private final AdapterStatusBridge _adapterStatusBridge;
	private final PresenceDetector _presenceDetector;
	private final GMAInitializer _gmaInitializer;
	private final WebViewErrorHandler _webViewErrorHandler;
	private final ScarAdapterFactory _scarAdapterFactory;
	private final GMAEventSender _gmaEventSender;
	private IScarAdapter _scarAdapter;

	public GMAScarAdapterBridge(@Nullable MobileAdsBridgeBase mobileAdsBridge,
								InitializeListenerBridge initializeListenerBridge,
								InitializationStatusBridge initializationStatusBridge,
								AdapterStatusBridge adapterStatusBridge,
								WebViewErrorHandler webViewErrorHandler,
								ScarAdapterFactory adapterFactory,
								GMAEventSender eventSender) {
		_initializationStatusBridge = initializationStatusBridge;
		_initializationListenerBridge = initializeListenerBridge;
		_adapterStatusBridge = adapterStatusBridge;
		_webViewErrorHandler = webViewErrorHandler;
		_scarAdapterFactory = adapterFactory;
		_mobileAdsBridge = mobileAdsBridge;
		_gmaEventSender = eventSender;
		_presenceDetector = new PresenceDetector(_mobileAdsBridge, _initializationListenerBridge, _initializationStatusBridge, _adapterStatusBridge);
		_gmaInitializer = new GMAInitializer(_mobileAdsBridge, _initializationListenerBridge, _initializationStatusBridge, _adapterStatusBridge, _gmaEventSender);
		_scarVersionFinder = new ScarVersionFinder(_mobileAdsBridge, _presenceDetector, _gmaInitializer, _gmaEventSender);
	}

	public void initializeScar() {
		if (_presenceDetector.areGMAClassesPresent()) {
			_gmaEventSender.send(GMAEvent.SCAR_PRESENT);
			_gmaInitializer.initializeGMA();
		} else {
			_webViewErrorHandler.handleError(new GMAAdsError(GMAEvent.SCAR_NOT_PRESENT));
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
		SignalsHandler signalListener = new SignalsHandler(_gmaEventSender);

		if (_scarAdapter != null) {
			_scarAdapter.getSCARSignals(ClientProperties.getApplicationContext(), interstitialList, rewardedList, signalListener);
		} else {
			_webViewErrorHandler.handleError(GMAAdsError.InternalSignalsError("Could not create SCAR adapter object"));
		}
	}

	/**
	 * Helper function to check if GMA SCAR bidding signals collection is supported.
	 *
	 * @return true if supported, false otherwise
	 */
	public boolean hasSCARBiddingSupport() {
		if (_mobileAdsBridge != null && _mobileAdsBridge.hasSCARBiddingSupport()) {
			_scarAdapter = getScarAdapterObject();
			return _scarAdapter != null;
		}

		return false;
	}

	/**
	 * Helper function to fetch GMA SCAR bidding signals.
	 *
	 * @param handler {@link BiddingSignalsHandler} to be notified when signals are ready.
	 */
	public void getSCARBiddingSignals(BiddingSignalsHandler handler) {
		if (_mobileAdsBridge != null && _mobileAdsBridge.hasSCARBiddingSupport()) {
			_scarAdapter = getScarAdapterObject();
			if (_scarAdapter != null) {
				_scarAdapter.getSCARBiddingSignals(ClientProperties.getApplicationContext(), handler);
			} else {
				handler.onSignalsCollectionFailed("Could not create SCAR adapter object.");
			}
		} else {
			handler.onSignalsCollectionFailed("SCAR bidding unsupported.");
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
		ScarInterstitialAdHandler adListener = new ScarInterstitialAdHandler(scarAdMetadata, getScarEventSubject(scarAdMetadata.getVideoLengthMs()), _gmaEventSender);
		_scarAdapter.loadInterstitialAd(ClientProperties.getApplicationContext(), scarAdMetadata, adListener);
	}

	private void loadRewardedAd(final ScarAdMetadata scarAdMetadata) {
		ScarRewardedAdHandler adListener = new ScarRewardedAdHandler(scarAdMetadata, getScarEventSubject(scarAdMetadata.getVideoLengthMs()), _gmaEventSender);
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

	private EventSubject getScarEventSubject(Integer videoLengthMs) {
		return new EventSubject<>(new ArrayDeque<>(Arrays.asList(GMAEvent.FIRST_QUARTILE, GMAEvent.MIDPOINT, GMAEvent.THIRD_QUARTILE, GMAEvent.LAST_QUARTILE)), videoLengthMs, new DefaultIntervalTimerFactory());
	}

	private IScarAdapter getScarAdapterObject() {
		if (_scarAdapter == null && _mobileAdsBridge != null) {
			ScarAdapterVersion adapterVersion = _mobileAdsBridge.getAdapterVersion(_scarVersionFinder.getVersionCode());
			_scarAdapter = _scarAdapterFactory.createScarAdapter(adapterVersion, _webViewErrorHandler);
		}
		return _scarAdapter;
	}

}

