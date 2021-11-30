package com.unity3d.services.store.core;

import com.unity3d.scar.adapter.common.WebViewAdsError;
import com.unity3d.services.store.StoreEvent;

import org.mockito.ArgumentMatcher;

public class StoreExceptionHandlerMatcher implements ArgumentMatcher<WebViewAdsError> {
	private final WebViewAdsError _webViewAdsError;

	public StoreExceptionHandlerMatcher(StoreEvent storeEvent, String description, Object... errorArguments) {
		_webViewAdsError = new StoreWebViewError(storeEvent, description, errorArguments);
	}

	@Override
	public boolean matches(WebViewAdsError argument) {
		int argIndex = 0;
		boolean argsEquals = true;
		for (Object errorArg : _webViewAdsError.getErrorArguments()) {
			if(!errorArg.equals(argument.getErrorArguments()[argIndex])){
				argsEquals = false;
			}
			argIndex++;
		}
		return _webViewAdsError.getDomain().equals(argument.getDomain()) &&
			   _webViewAdsError.getDescription().equals(argument.getDescription()) &&
				argsEquals;
	}
}
