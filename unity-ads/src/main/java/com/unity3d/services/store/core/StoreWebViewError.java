package com.unity3d.services.store.core;

import com.unity3d.scar.adapter.common.WebViewAdsError;
import com.unity3d.services.core.webview.WebViewEventCategory;

public class StoreWebViewError extends WebViewAdsError {
	public StoreWebViewError(Enum<?> errorCategory, String description, Object... errorArguments) {
		super(errorCategory, description, errorArguments);
	}

	@Override
	public String getDomain() {
		return WebViewEventCategory.STORE.name();
	}

}
