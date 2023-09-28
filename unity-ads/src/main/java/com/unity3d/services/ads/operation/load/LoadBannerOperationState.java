package com.unity3d.services.ads.operation.load;

import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.UnityAdsLoadOptions;
import com.unity3d.scar.adapter.common.scarads.ScarAdMetadata;
import com.unity3d.services.banners.UnityBannerSize;
import com.unity3d.services.core.configuration.Configuration;

public class LoadBannerOperationState extends LoadOperationState {
	private UnityBannerSize _size;
	private ScarAdMetadata _scarAdMetadata;

	public LoadBannerOperationState(String placementId, String bannerAdId, UnityBannerSize size, IUnityAdsLoadListener listener, UnityAdsLoadOptions loadOptions, Configuration configuration) {
		super(placementId, listener, loadOptions, configuration);
		id = bannerAdId;
		this._size = size;
	}

	public UnityBannerSize getSize() {
		return _size;
	}

	public void setSize(UnityBannerSize size) {
		_size = size;
	}

	public void setScarAdMetadata(ScarAdMetadata scarAdMetadata) {
		_scarAdMetadata = scarAdMetadata;
	}

	public ScarAdMetadata getScarAdMetadata() {
		return _scarAdMetadata;
	}

	public boolean isScarAd() {
		return _scarAdMetadata != null;
	}
}
