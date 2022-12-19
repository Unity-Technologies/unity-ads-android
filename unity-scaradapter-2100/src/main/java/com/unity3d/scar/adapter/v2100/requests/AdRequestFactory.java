package com.unity3d.scar.adapter.v2100.requests;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.unity3d.scar.adapter.common.requests.RequestExtras;

public class AdRequestFactory {

	private RequestExtras _requestExtras;

	public AdRequestFactory(RequestExtras requestExtras) {
		_requestExtras = requestExtras;
	}

	public AdRequest.Builder getAdRequest() {
		return new AdRequest.Builder()
			.setRequestAgent(_requestExtras.getVersionName())
			.addNetworkExtrasBundle(AdMobAdapter.class, _requestExtras.getExtras());
	}

	public AdRequest buildAdRequest() {
		return getAdRequest().build();
	}

	public AdRequest buildAdRequestWithAdString(String adString) {
		return getAdRequest().setAdString(adString).build();
	}
}
