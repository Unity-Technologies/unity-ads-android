package com.unity3d.ads.webplayer;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ClientCertRequest;
import android.webkit.ConsoleMessage;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.HttpAuthHandler;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.misc.Utilities;
import com.unity3d.ads.misc.ViewUtilities;
import com.unity3d.ads.webview.WebViewApp;
import com.unity3d.ads.webview.WebViewEventCategory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class WebPlayer extends WebView {
	private Map<String, String> _erroredSettings;
	private JSONObject _eventSettings;
	private Method _evaluateJavascript = null;

	public WebPlayer(Context context) {
		super(context);
	}

	public WebPlayer(Context context, JSONObject webSettings, JSONObject webPlayerSettings) {
		super(context);

		WebSettings settings = getSettings();

		if(Build.VERSION.SDK_INT >= 16) {
			settings.setAllowFileAccessFromFileURLs(false);
			settings.setAllowUniversalAccessFromFileURLs(false);
		}
		if (Build.VERSION.SDK_INT >= 19) {
			try {
				_evaluateJavascript = WebView.class.getMethod("evaluateJavascript", String.class, ValueCallback.class);
			} catch(NoSuchMethodException e) {
				DeviceLog.exception("Method evaluateJavascript not found", e);
				_evaluateJavascript = null;
			}
		}

		settings.setAppCacheEnabled(false);
		settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		settings.setDatabaseEnabled(false);

		settings.setDomStorageEnabled(false);
		settings.setGeolocationEnabled(false);
		settings.setJavaScriptEnabled(true);
		settings.setLoadsImagesAutomatically(true);

		settings.setPluginState(WebSettings.PluginState.OFF);
		settings.setRenderPriority(WebSettings.RenderPriority.NORMAL);
		settings.setSaveFormData(false);
		settings.setSavePassword(false);

		setHorizontalScrollBarEnabled(false);
		setVerticalScrollBarEnabled(false);
		setInitialScale(0);
		setBackgroundColor(Color.TRANSPARENT);
		ViewUtilities.setBackground(this, new ColorDrawable(Color.TRANSPARENT));
		setBackgroundResource(0);

		setSettings(webSettings, webPlayerSettings);

		setWebViewClient(new WebPlayerClient());
		setWebChromeClient(new WebPlayerChromeClient());
		setDownloadListener(new WebPlayerDownloadListener());

		addJavascriptInterface(new WebPlayerBridgeInterface(), "webplayerbridge");
	}

	public void setEventSettings (JSONObject eventSettings) {
		_eventSettings = eventSettings;
	}

	public void setSettings (JSONObject webSettings, JSONObject webPlayerSettings) {
		if (_erroredSettings != null) {
			_erroredSettings.clear();
		}

		WebSettings settings = getSettings();
		setTargetSettings(settings, webSettings);
		setTargetSettings(this, webPlayerSettings);
	}

	public Map<String, String> getErroredSettings () {
		return _erroredSettings;
	}

	private Object setTargetSettings (Object targetObj, JSONObject settings) {
		if (settings != null) {
			Iterator<String> keysIterator = settings.keys();
			while (keysIterator.hasNext()) {
				String key = keysIterator.next();
				try {
					JSONArray parameters = settings.getJSONArray(key);
					Class<?>[] types = getTypes(parameters);
					Method m = targetObj.getClass().getMethod(key, types);
					m.invoke(targetObj, getValues(parameters));
				} catch (Exception e) {
					addErroredSetting(key, e.getMessage());
					DeviceLog.exception("Setting errored", e);
				}
			}
		}

		return targetObj;
	}


	public void invokeJavascript(String data) {
		Utilities.runOnUiThread(new JavaScriptInvocation(data, this));
	}

	public void sendEvent(JSONArray params) {
		StringBuilder builder = new StringBuilder();
		builder.append("javascript:window.nativebridge.receiveEvent(");
		builder.append(params.toString());
		builder.append(")");
		invokeJavascript(builder.toString());
	}

	private class JavaScriptInvocation implements Runnable {
		private String _jsString = null;
		private android.webkit.WebView _webView = null;

		public JavaScriptInvocation(String jsString, android.webkit.WebView webView) {
			_jsString = jsString;
			_webView = webView;
		}

		@Override
		public void run() {
			if (_jsString != null) {
				try {
					if (Build.VERSION.SDK_INT >= 19) {
						_evaluateJavascript.invoke(_webView, _jsString, null);
					} else {
						loadUrl(_jsString);
					}
				} catch (Exception e) {
					DeviceLog.exception("Error while processing JavaScriptString", e);
				}
			} else {
				DeviceLog.error("Could not process JavaScript, the string is NULL");
			}
		}
	}

	private Class<?>[] getTypes(JSONArray parameters) throws JSONException, ClassNotFoundException {
		Class<?>[] types;
		if(parameters == null) {
			return null;
		} else {
			types = new Class[parameters.length()];
		}

		if(parameters != null) {
			for (int i = 0; i < parameters.length(); i++) {
				if (parameters.get(i) instanceof JSONObject) {
					JSONObject param = (JSONObject)parameters.get(i);
					String className = param.getString("className");
					Class<?> theClass = Class.forName(className);

					if (theClass != null) {
						types[i] = theClass;
					}
				}
				else {
					Class<?> currentClass = parameters.get(i).getClass();
					types[i] = getPrimitiveClass(currentClass);
				}
			}
		}

		return types;
	}


	public Class<?> getPrimitiveClass(Class<?> className) {
		String typeName = className.getName();
		if (typeName.equals("java.lang.Byte"))
			return byte.class;
		if (typeName.equals("java.lang.Short"))
			return short.class;
		if (typeName.equals("java.lang.Integer"))
			return int.class;
		if (typeName.equals("java.lang.Long"))
			return long.class;
		if (typeName.equals("java.lang.Character"))
			return char.class;
		if (typeName.equals("java.lang.Float"))
			return float.class;
		if (typeName.equals("java.lang.Double"))
			return double.class;
		if (typeName.equals("java.lang.Boolean"))
			return boolean.class;
		if (typeName.equals("java.lang.Void"))
			return void.class;

		return className;
	}


	private Object[] getValues(JSONArray parameters) throws JSONException, ClassNotFoundException, NoSuchMethodException {
		Object[] values;
		if(parameters == null) {
			return null;
		} else {
			values = new Object[parameters.length()];
		}

		Object[] params = new Object[parameters.length()];
		for (int i = 0; i < parameters.length(); i++) {
			if (parameters.get(i) instanceof JSONObject) {
				JSONObject param = (JSONObject)parameters.get(i);
				Object value = param.get("value");
				String type = param.getString("type");
				String className = null;

				if (param.has("className")) {
					className = param.getString("className");
				}

				if (className != null && type.equals("Enum")) {
					Class<?> enumClass = Class.forName(className);
					if (enumClass != null) {
						params[i] = Enum.valueOf((Class<Enum>)enumClass, (String)value);
					}
				}
			}
			else {
				params[i] = parameters.get(i);
			}
		}

		if(parameters != null) {
			System.arraycopy(params, 0, values, 0, parameters.length());
		}

		return values;
	}

	private void addErroredSetting (String key, String error) {
		if (_erroredSettings == null) {
			_erroredSettings = new HashMap<>();
		}

		_erroredSettings.put(key, error);
	}

	private boolean shouldCallSuper (String event) {
		try {
			if (_eventSettings != null && _eventSettings.has(event) && _eventSettings.getJSONObject(event).has("callSuper")) {
				return _eventSettings.getJSONObject(event).getBoolean("callSuper");
			}
		}
		catch (Exception e) {
			DeviceLog.exception("Error getting super call status", e);
		}

		return true;
	}

	private boolean shouldSendEvent (String event) {
		try {
			if (_eventSettings != null && _eventSettings.has(event) && _eventSettings.getJSONObject(event).has("sendEvent")) {
				return _eventSettings.getJSONObject(event).getBoolean("sendEvent");
			}
		}
		catch (Exception e) {
			DeviceLog.exception("Error getting send event status", e);
		}

		return false;
	}

	private <T> T getReturnValue(String event, Class<T> type, T defaultValue) {
		try {
			if (_eventSettings != null && _eventSettings.has(event) && _eventSettings.getJSONObject(event).has("returnValue")) {
				return type.cast(_eventSettings.getJSONObject(event).get("returnValue"));
			}
		}
		catch (Exception e) {
			DeviceLog.exception("Error getting default return value", e);
		}

		return defaultValue;
	}

	private boolean hasReturnValue(String event) {
		try {
			if (_eventSettings != null && _eventSettings.has(event) && _eventSettings.getJSONObject(event).has("returnValue")) {
				return true;
			}
		}
		catch (Exception e) {
			DeviceLog.exception("Error getting default return value", e);
		}

		return false;
	}

	private class WebPlayerClient extends WebViewClient {
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			if (shouldCallSuper("onPageStarted")) {
				super.onPageStarted(view, url, favicon);
			}
			if (shouldSendEvent("onPageStarted")) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.WEBPLAYER, WebPlayerEvent.PAGE_STARTED, url);
			}
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			if (shouldCallSuper("onPageFinished")) {
				super.onPageFinished(view, url);
			}
			if (shouldSendEvent("onPageFinished")) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.WEBPLAYER, WebPlayerEvent.PAGE_FINISHED, url);
			}
		}

		@Override
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			if (shouldCallSuper("onReceivedError")) {
				super.onReceivedError(view, errorCode, description, failingUrl);
			}
			if (shouldSendEvent("onReceivedError")) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.WEBPLAYER, WebPlayerEvent.ERROR, failingUrl, description);
			}
		}

		@TargetApi(25)
		@Override
		public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
			if (shouldCallSuper("onReceivedError")) {
				super.onReceivedError(view, request, error);
			}
			if (shouldSendEvent("onReceivedError")) {
				String description = "";
				if (error != null && error.getDescription() != null) {
					description = error.getDescription().toString();
				}
				String url = "";
				if (request != null && request.getUrl() != null) {
					url = request.getUrl().toString();
				}
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.WEBPLAYER, WebPlayerEvent.ERROR, url, description);
			}
		}

		@Override
		public void onLoadResource(WebView view, String url) {
			if (shouldCallSuper("onLoadResource")) {
				super.onLoadResource(view, url);
			}
			if (shouldSendEvent("onLoadResource")) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.WEBPLAYER, WebPlayerEvent.LOAD_RESOUCE, url);
			}
		}

		@TargetApi(14)
		@Override
		public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
			if (shouldCallSuper("onReceivedSslError")) {
				super.onReceivedSslError(view, handler, error);
			}
			if (shouldSendEvent("onReceivedSslError")) {
				String url = "";
				if (error != null) {
					url = error.getUrl();
				}
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.WEBPLAYER, WebPlayerEvent.SSL_ERROR, url);
			}
		}

		@TargetApi(21)
		@Override
		public void onReceivedClientCertRequest(WebView view, ClientCertRequest request) {
			if (shouldCallSuper("onReceivedClientCertRequest")) {
				super.onReceivedClientCertRequest(view, request);
			}
			if (shouldSendEvent("onReceivedClientCertRequest")) {
				String host = "";
				int port = -1;

				if (request != null) {
					host = request.getHost();
					port = request.getPort();
				}
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.WEBPLAYER, WebPlayerEvent.CLIENT_CERT_REQUEST, host, port);
			}
		}

		@Override
		public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
			if (shouldCallSuper("onReceivedHttpAuthRequest")) {
				super.onReceivedHttpAuthRequest(view, handler, host, realm);
			}
			if (shouldSendEvent("onReceivedHttpAuthRequest")) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.WEBPLAYER, WebPlayerEvent.HTTP_AUTH_REQUEST, host, realm);
			}
		}

		@Override
		public void onScaleChanged(WebView view, float oldScale, float newScale) {
			if (shouldCallSuper("onScaleChanged")) {
				super.onScaleChanged(view, oldScale, newScale);
			}
			if (shouldSendEvent("onScaleChanged")) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.WEBPLAYER, WebPlayerEvent.SCALE_CHANGED, oldScale, newScale);
			}
		}

		@Override
		public void onReceivedLoginRequest(WebView view, String realm, String account, String args) {
			if (shouldCallSuper("onReceivedLoginRequest")) {
				super.onReceivedLoginRequest(view, realm, account, args);
			}
			if (shouldSendEvent("onReceivedLoginRequest")) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.WEBPLAYER, WebPlayerEvent.LOGIN_REQUEST, realm, account, args);
			}
		}

		@TargetApi(21)
		@Override
		public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
			if (shouldCallSuper("onReceivedHttpError")) {
				super.onReceivedHttpError(view, request, errorResponse);
			}
			if (shouldSendEvent("onReceivedHttpError")) {
				String url = "";
				if (request != null && request.getUrl() != null) {
					url = request.getUrl().toString();
				}

				int statusCode = -1;
				String reasonPhrase = "";
				if (errorResponse != null) {
					statusCode = errorResponse.getStatusCode();
					reasonPhrase = errorResponse.getReasonPhrase();
				}

				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.WEBPLAYER, WebPlayerEvent.HTTP_ERROR, url, reasonPhrase, statusCode);
			}
		}

		@TargetApi(21)
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
			Boolean returnValue = false;

			if (shouldCallSuper("shouldOverrideUrlLoading")) {
				returnValue = super.shouldOverrideUrlLoading(view, request);
			}
			if (shouldSendEvent("shouldOverrideUrlLoading")) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.WEBPLAYER, WebPlayerEvent.SHOULD_OVERRIDE_URL_LOADING, request.getUrl().toString(), request.getMethod());
			}
			if (hasReturnValue("shouldOverrideUrlLoading")) {
				returnValue = getReturnValue("shouldOverrideUrlLoading", java.lang.Boolean.class, true);
			}

			return returnValue;
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Boolean returnValue = false;

			if (shouldCallSuper("shouldOverrideUrlLoading")) {
				returnValue = super.shouldOverrideUrlLoading(view, url);
			}
			if (shouldSendEvent("shouldOverrideUrlLoading")) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.WEBPLAYER, WebPlayerEvent.SHOULD_OVERRIDE_URL_LOADING, url);
			}
			if (hasReturnValue("shouldOverrideUrlLoading")) {
				returnValue = getReturnValue("shouldOverrideUrlLoading", java.lang.Boolean.class, true);
			}

			return returnValue;
		}

		@Override
		public void onPageCommitVisible(WebView view, String url) {
			if (shouldCallSuper("onPageCommitVisible")) {
				super.onPageCommitVisible(view, url);
			}
			if (shouldSendEvent("onPageCommitVisible")) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.WEBPLAYER, WebPlayerEvent.PAGE_COMMIT_VISIBLE, url);
			}
		}

		@TargetApi(21)
		@Override
		public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
			WebResourceResponse returnValue = null;

			if (shouldCallSuper("shouldInterceptRequest")) {
				returnValue = super.shouldInterceptRequest(view, request);
			}
			if (shouldSendEvent("shouldInterceptRequest")) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.WEBPLAYER, WebPlayerEvent.SHOULD_INTERCEPT_REQUEST, request.getUrl().toString());
			}

			return returnValue;
		}

		@Override
		public void onFormResubmission(WebView view, Message dontResend, Message resend) {
			if (shouldCallSuper("onFormResubmission")) {
				super.onFormResubmission(view, dontResend, resend);
			}
			if (shouldSendEvent("onFormResubmission")) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.WEBPLAYER, WebPlayerEvent.FORM_RESUBMISSION);
			}
		}

		@Override
		public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
			Boolean returnValue = false;

			if (shouldCallSuper("shouldOverrideKeyEvent")) {
				returnValue = super.shouldOverrideKeyEvent(view, event);
			}
			if (shouldSendEvent("shouldOverrideKeyEvent")) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.WEBPLAYER, WebPlayerEvent.SHOULD_OVERRIDE_KEY_EVENT, event.getKeyCode(), event.getAction());
			}
			if (hasReturnValue("shouldOverrideKeyEvent")) {
				returnValue = getReturnValue("shouldOverrideKeyEvent", java.lang.Boolean.class, true);
			}

			return returnValue;
		}

		@Override
		public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
			if (shouldCallSuper("onUnhandledKeyEvent")) {
				super.onUnhandledKeyEvent(view, event);
			}
			if (shouldSendEvent("onUnhandledKeyEvent")) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.WEBPLAYER, WebPlayerEvent.UNHANDLED_KEY_EVENT, event.getKeyCode(), event.getAction());
			}
		}
	}

	@TargetApi(21)
	private class WebPlayerChromeClient extends WebChromeClient {

		@Override
		public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
			if (shouldCallSuper("onGeolocationPermissionsShowPrompt")) {
				super.onGeolocationPermissionsShowPrompt(origin, callback);
			}
			if (shouldSendEvent("onGeolocationPermissionsShowPrompt")) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.WEBPLAYER, WebPlayerEvent.GEOLOCATION_PERMISSIONS_SHOW, origin);
			}
		}

		@Override
		public void onPermissionRequest(PermissionRequest request) {
			if (shouldCallSuper("onPermissionRequest")) {
				super.onPermissionRequest(request);
			}
			if (shouldSendEvent("onPermissionRequest")) {
				String url = "";
				if (request != null && request.getOrigin() != null) {
					url = request.getOrigin().toString();
				}
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.WEBPLAYER, WebPlayerEvent.PERMISSION_REQUEST, url);
			}
		}

		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			if (shouldCallSuper("onProgressChanged")) {
				super.onProgressChanged(view, newProgress);
			}
			if (shouldSendEvent("onProgressChanged")) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.WEBPLAYER, WebPlayerEvent.PROGRESS_CHANGED, newProgress);
			}
		}

		@Override
		public void onReceivedTitle(WebView view, String title) {
			if (shouldCallSuper("onReceivedTitle")) {
				super.onReceivedTitle(view, title);
			}
			if (shouldSendEvent("onReceivedTitle")) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.WEBPLAYER, WebPlayerEvent.RECEIVED_TITLE, title);
			}
		}

		@Override
		public void onReceivedIcon(WebView view, Bitmap icon) {
			if (shouldCallSuper("onReceivedIcon")) {
				super.onReceivedIcon(view, icon);
			}
			if (shouldSendEvent("onReceivedIcon")) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.WEBPLAYER, WebPlayerEvent.RECEIVED_ICON);
			}
		}

		@Override
		public void onReceivedTouchIconUrl(WebView view, String url, boolean precomposed) {
			if (shouldCallSuper("onReceivedTouchIconUrl")) {
				super.onReceivedTouchIconUrl(view, url, precomposed);
			}
			if (shouldSendEvent("onReceivedTouchIconUrl")) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.WEBPLAYER, WebPlayerEvent.RECEIVED_TOUCH_ICON_URL, url, precomposed);
			}
		}

		@Override
		public void onShowCustomView(View view, CustomViewCallback callback) {
			if (shouldCallSuper("onShowCustomView")) {
				super.onShowCustomView(view, callback);
			}
			if (shouldSendEvent("onShowCustomView")) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.WEBPLAYER, WebPlayerEvent.SHOW_CUSTOM_VIEW);
			}
		}

		@Override
		public void onHideCustomView() {
			if (shouldCallSuper("onHideCustomView")) {
				super.onHideCustomView();
			}
			if (shouldSendEvent("onHideCustomView")) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.WEBPLAYER, WebPlayerEvent.HIDE_CUSTOM_VIEW);
			}
		}

		@Override
		public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
			Boolean returnValue = false;

			if (shouldCallSuper("onCreateWindow")) {
				returnValue = super.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
			}
			if (shouldSendEvent("onCreateWindow")) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.WEBPLAYER, WebPlayerEvent.CREATE_WINDOW, isDialog, isUserGesture, resultMsg);
			}
			if (hasReturnValue("onCreateWindow")) {
				returnValue = getReturnValue("onCreateWindow", java.lang.Boolean.class, false);
			}

			return returnValue;
		}

		@Override
		public void onRequestFocus(WebView view) {
			if (shouldCallSuper("onRequestFocus")) {
				super.onRequestFocus(view);
			}
			if (shouldSendEvent("onRequestFocus")) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.WEBPLAYER, WebPlayerEvent.REQUEST_FOCUS);
			}
		}

		@Override
		public void onCloseWindow(WebView window) {
			if (shouldCallSuper("onCloseWindow")) {
				super.onCloseWindow(window);
			}
			if (shouldSendEvent("onCloseWindow")) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.WEBPLAYER, WebPlayerEvent.CLOSE_WINDOW);
			}
		}

		@Override
		public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
			Boolean returnValue = false;

			if (shouldCallSuper("onJsAlert")) {
				returnValue = super.onJsAlert(view, url, message, result);
			}
			if (shouldSendEvent("onJsAlert")) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.WEBPLAYER, WebPlayerEvent.JS_ALERT, url, message, result);
			}
			if (hasReturnValue("onJsAlert")) {
				returnValue = getReturnValue("onJsAlert", java.lang.Boolean.class, true);
			}

			return returnValue;
		}

		@Override
		public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
			Boolean returnValue = false;

			if (shouldCallSuper("onJsConfirm")) {
				returnValue = super.onJsConfirm(view, url, message, result);
			}
			if (shouldSendEvent("onJsConfirm")) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.WEBPLAYER, WebPlayerEvent.JS_CONFIRM, url, message);
			}
			if (hasReturnValue("onJsConfirm")) {
				returnValue = getReturnValue("onJsConfirm", java.lang.Boolean.class, true);
			}

			return returnValue;
		}

		@Override
		public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
			Boolean returnValue = false;

			if (shouldCallSuper("onJsPrompt")) {
				returnValue = super.onJsPrompt(view, url, message, defaultValue, result);
			}
			if (shouldSendEvent("onJsPrompt")) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.WEBPLAYER, WebPlayerEvent.JS_PROMPT, url, message, defaultValue);
			}
			if (hasReturnValue("onJsPrompt")) {
				returnValue = getReturnValue("onJsPrompt", java.lang.Boolean.class, true);
			}

			return returnValue;
		}

		@Override
		public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
			Boolean returnValue = false;

			if (shouldCallSuper("onConsoleMessage")) {
				returnValue = super.onConsoleMessage(consoleMessage);
			}
			if (shouldSendEvent("onConsoleMessage")) {
				String message = "";
				if (consoleMessage != null) {
					message = consoleMessage.message();
				}
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.WEBPLAYER, WebPlayerEvent.CONSOLE_MESSAGE, message);
			}
			if (hasReturnValue("onConsoleMessage")) {
				returnValue = getReturnValue("onConsoleMessage", java.lang.Boolean.class, true);
			}

			return returnValue;
		}

		@Override
		public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
			Boolean returnValue = false;

			if (shouldCallSuper("onShowFileChooser")) {
				returnValue = super.onShowFileChooser(webView, filePathCallback, fileChooserParams);
			}
			if (shouldSendEvent("onShowFileChooser")) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.WEBPLAYER, WebPlayerEvent.SHOW_FILE_CHOOSER);
			}
			if (hasReturnValue("onShowFileChooser")) {
				returnValue = getReturnValue("onShowFileChooser", java.lang.Boolean.class, true);
				if (returnValue) {
					filePathCallback.onReceiveValue(null);
				}
			}

			return returnValue;
		}
	}

	private class WebPlayerDownloadListener implements DownloadListener {
		@Override
		public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
			if (shouldSendEvent("onDownloadStart")) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.WEBPLAYER, WebPlayerEvent.DOWNLOAD_START, url, userAgent, contentDisposition, mimetype, contentLength);
			}
		}
	}
}
