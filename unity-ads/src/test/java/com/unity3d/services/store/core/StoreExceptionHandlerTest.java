package com.unity3d.services.store.core;

import static org.mockito.Mockito.times;

import com.unity3d.services.ads.gmascar.handlers.WebViewErrorHandler;
import com.unity3d.services.store.StoreError;
import com.unity3d.services.store.StoreEvent;

import org.json.JSONException;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StoreExceptionHandlerTest {

	@Mock
	public WebViewErrorHandler _webviewErrorHandler;

	@Test
	public void testStoreExceptionHandlerNoSuchMethodException() {
		NoSuchMethodException exception = new NoSuchMethodException("No such method");
		callAndValidateStoreExceptionHandler(StoreEvent.PURCHASES_UPDATED_ERROR, StoreError.NO_SUCH_METHOD, exception);
	}

	@Test
	public void testStoreExceptionHandlerIllegalAccessException() {
		IllegalAccessException exception = new IllegalAccessException("Illegal access");
		callAndValidateStoreExceptionHandler(StoreEvent.PURCHASES_UPDATED_ERROR, StoreError.ILLEGAL_ACCESS, exception);
	}

	@Ignore("Somehow, the message passed in JSONException doesn't set the internal message.")
	@Test
	public void testStoreExceptionHandlerJsonException() {
		JSONException exception = new JSONException("Invalid JSON", new RuntimeException());
		callAndValidateStoreExceptionHandler(StoreEvent.PURCHASES_UPDATED_ERROR, StoreError.JSON_ERROR, exception);
	}

	@Test
	public void testStoreExceptionHandlerClassNotFoundException() {
		ClassNotFoundException exception = new ClassNotFoundException("Class not found");
		callAndValidateStoreExceptionHandler(StoreEvent.PURCHASES_UPDATED_ERROR, StoreError.CLASS_NOT_FOUND, exception);
	}

	private void callAndValidateStoreExceptionHandler(StoreEvent storeEvent, StoreError storeError, Exception exception) {
		StoreExceptionHandler storeExceptionHandler = new StoreExceptionHandler(_webviewErrorHandler);
		storeExceptionHandler.handleStoreException(storeEvent, 0, exception);
		Mockito.verify(_webviewErrorHandler, times(1)).handleError(Mockito.argThat(
			new StoreExceptionHandlerMatcher(storeEvent, exception.getMessage(), 0, storeError, exception.getMessage())));
	}
}
