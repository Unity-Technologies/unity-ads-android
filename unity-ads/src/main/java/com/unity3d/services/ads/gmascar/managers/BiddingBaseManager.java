package com.unity3d.services.ads.gmascar.managers;

import static com.unity3d.services.ads.gmascar.utils.ScarConstants.TOKEN_WITH_SCAR_FORMAT;
import static com.unity3d.services.core.misc.Utilities.wrapCustomerListener;

import com.unity3d.ads.IUnityAdsTokenListener;
import com.unity3d.services.ads.gmascar.GMA;
import com.unity3d.services.ads.gmascar.listeners.IBiddingSignalsListener;
import com.unity3d.services.ads.gmascar.models.BiddingSignals;
import com.unity3d.services.ads.gmascar.utils.ScarRequestHandler;
import com.unity3d.services.core.configuration.ConfigurationReader;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.request.metrics.SDKMetricsSender;
import com.unity3d.services.core.request.metrics.ScarMetric;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public abstract class BiddingBaseManager implements IBiddingManager {

	protected final AtomicBoolean isUploadPermitted = new AtomicBoolean(false);
	private final String tokenIdentifier;
	private final IUnityAdsTokenListener unityAdsTokenListener;
	private final ScarRequestHandler _scarRequestHandler;
	private final boolean _isAsyncTokenCall;
	private final boolean _isBannerEnabled;

	private final AtomicReference<BiddingSignals> signals = new AtomicReference<>();

	public BiddingBaseManager(boolean isBannerEnabled, IUnityAdsTokenListener unityAdsTokenListener) {
		this(isBannerEnabled, unityAdsTokenListener, new ScarRequestHandler());
	}

	public BiddingBaseManager(boolean isBannerEnabled, IUnityAdsTokenListener unityAdsTokenListener, ScarRequestHandler requestSender) {
		this.tokenIdentifier = UUID.randomUUID().toString();
		this._isBannerEnabled = isBannerEnabled;
		this.unityAdsTokenListener = unityAdsTokenListener;
		this._isAsyncTokenCall = unityAdsTokenListener != null;
		this._scarRequestHandler = requestSender;
	}

	public abstract void start();

	@Override
	public String getTokenIdentifier() {
		return tokenIdentifier;
	}

	@Override
	public String getFormattedToken(String unityToken) {
		if (unityToken == null || unityToken.isEmpty()) return null;
		String tokenIdentifier = getTokenIdentifier();
		if (tokenIdentifier == null || tokenIdentifier.isEmpty()) return unityToken;
		else return String.format(TOKEN_WITH_SCAR_FORMAT, tokenIdentifier, unityToken);
	}

	@Override
	public final void onUnityAdsTokenReady(String token) {
		if (unityAdsTokenListener != null) {
			wrapCustomerListener(() -> unityAdsTokenListener.onUnityAdsTokenReady(token));
		}
	}

	public void permitUpload() {
		isUploadPermitted.set(true);
	}

	public void permitSignalsUpload() {
		isUploadPermitted.set(true);
		attemptUpload();
	}

	public void fetchSignals() {
		getMetricSender().sendMetric(ScarMetric.hbSignalsFetchStart(_isAsyncTokenCall));

		new Thread(() -> GMA.getInstance().getSCARBiddingSignals(this._isBannerEnabled, new IBiddingSignalsListener() {
			@Override
			public void onSignalsReady(BiddingSignals signals) {
				BiddingBaseManager.this.onSignalsReady(signals);
				sendFetchResult("");
			}

			@Override
			public void onSignalsFailure(String msg) {
				sendFetchResult(msg);
			}
		})).start();
	}

	public void sendFetchResult(String errorMsg) {
		if (errorMsg != "") {
			getMetricSender().sendMetric(ScarMetric.hbSignalsFetchFailure(_isAsyncTokenCall, errorMsg));
		} else {
			getMetricSender().sendMetric(ScarMetric.hbSignalsFetchSuccess(_isAsyncTokenCall));
		}
	}

	// Could be private, but visible for testing
	public void onSignalsReady(BiddingSignals signals) {
		BiddingBaseManager.this.signals.set(signals);
		attemptUpload();
	}

	private synchronized void attemptUpload() {
		// ensure gate turned off once upload started to prevent threading issues and multiple uploads
		if (signals.get() != null && isUploadPermitted.compareAndSet(true, false)) {
			uploadSignals();
		}
	}

	public void uploadSignals() {
		getMetricSender().sendMetric(ScarMetric.hbSignalsUploadStart(_isAsyncTokenCall));

		final BiddingSignals signals = this.signals.get();
		if (signals == null || signals.isEmpty()) {
			getMetricSender().sendMetric(ScarMetric.hbSignalsUploadFailure(_isAsyncTokenCall, "null or empty signals"));
			return;
		}

		new Thread(() -> {
			try {
				// Since there are potential side effects of file read in the
				// getCurrentConfiguration call, we don't want this to be called in the constructor.
				_scarRequestHandler.makeUploadRequest(tokenIdentifier, signals, new ConfigurationReader().getCurrentConfiguration().getScarBiddingUrl());
				getMetricSender().sendMetric(ScarMetric.hbSignalsUploadSuccess(_isAsyncTokenCall));
			} catch (Exception e) {
				getMetricSender().sendMetric(ScarMetric.hbSignalsUploadFailure(_isAsyncTokenCall, e.getLocalizedMessage()));
			}
		}).start();
	}

	public SDKMetricsSender getMetricSender() {
		return Utilities.getService(SDKMetricsSender.class);
	}
}
