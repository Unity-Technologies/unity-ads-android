package com.unity3d.services.ads.gmascar.handlers;

import com.unity3d.scar.adapter.common.GMAEvent;
import com.unity3d.scar.adapter.common.IScarAdListenerWrapper;
import com.unity3d.scar.adapter.common.scarads.ScarAdMetadata;
import com.unity3d.services.ads.gmascar.utils.GMAEventSender;
import com.unity3d.services.core.misc.EventSubject;
import com.unity3d.services.core.misc.IEventListener;
import com.unity3d.services.core.webview.WebViewApp;
import com.unity3d.services.core.webview.WebViewEventCategory;

public abstract class ScarAdHandlerBase implements IScarAdListenerWrapper {

	final protected ScarAdMetadata _scarAdMetadata;
	final protected EventSubject<GMAEvent> _eventSubject;
	final protected GMAEventSender _gmaEventSender;

	public ScarAdHandlerBase(ScarAdMetadata scarAdMetadata, EventSubject<GMAEvent> eventSubject) {
		_scarAdMetadata = scarAdMetadata;
		_eventSubject = eventSubject;
		_gmaEventSender = new GMAEventSender();
	}

	@Override
	public void onAdLoaded() {
		_gmaEventSender.send(GMAEvent.AD_LOADED, _scarAdMetadata.getPlacementId(), _scarAdMetadata.getQueryId());
	}

	@Override
	public void onAdFailedToLoad(int errorCode, String errorString) {
		_gmaEventSender.send(GMAEvent.LOAD_ERROR, _scarAdMetadata.getPlacementId(), _scarAdMetadata.getQueryId(), errorString, errorCode);
	}

	@Override
	public void onAdOpened() {
		WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.GMA, GMAEvent.AD_STARTED);

		_eventSubject.subscribe(new IEventListener<GMAEvent>() {
			@Override
			public void onNextEvent(GMAEvent nextEvent) {
				_gmaEventSender.send(nextEvent);
			}
		});
	}

	@Override
	public void onAdSkipped() {
		_gmaEventSender.send(GMAEvent.AD_SKIPPED);
	}

	@Override
	public void onAdClosed() {
		_gmaEventSender.send(GMAEvent.AD_CLOSED);
		_eventSubject.unsubscribe();
	}
}
