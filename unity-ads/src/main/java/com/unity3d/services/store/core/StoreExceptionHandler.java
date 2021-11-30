package com.unity3d.services.store.core;

import com.unity3d.services.ads.gmascar.handlers.WebViewErrorHandler;
import com.unity3d.services.store.StoreError;
import com.unity3d.services.store.StoreEvent;

import org.json.JSONException;

import java.lang.reflect.InvocationTargetException;

public class StoreExceptionHandler {
	private final WebViewErrorHandler _webViewErrorHandler;
	public StoreExceptionHandler(WebViewErrorHandler webViewErrorHandler) {
		_webViewErrorHandler = webViewErrorHandler;
	}

	public void handleStoreException(StoreEvent storeEvent, int operationId, Exception exception) {
		sendErrorToWebView(storeEvent, getStoreError(exception), operationId, exception);
	}

	private void sendErrorToWebView(StoreEvent storeEvent, StoreError storeError, int operationId, Exception exception) {
		_webViewErrorHandler.handleError(new StoreWebViewError(storeEvent, exception.getMessage(), operationId, storeError, exception.getMessage()));
	}

	private StoreError getStoreError(Exception exception) {
		StoreError result = StoreError.UNKNOWN_ERROR;
		if (exception instanceof NoSuchMethodException) {
			result = StoreError.NO_SUCH_METHOD;
		} else if (exception instanceof IllegalAccessException) {
			result = StoreError.ILLEGAL_ACCESS;
		} else if (exception instanceof JSONException) {
			result = StoreError.JSON_ERROR;
		} else if (exception instanceof InvocationTargetException) {
			result = StoreError.INVOCATION_TARGET;
		} else if (exception instanceof ClassNotFoundException) {
			result = StoreError.CLASS_NOT_FOUND;
		}
		return result;
	}
}
