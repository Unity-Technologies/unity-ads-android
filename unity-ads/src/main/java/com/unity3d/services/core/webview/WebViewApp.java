package com.unity3d.services.core.webview;

import android.os.Build;
import android.os.ConditionVariable;
import android.os.Looper;
import android.view.ViewGroup;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebViewClient;

import com.unity3d.services.ads.api.AdUnit;
import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.configuration.InitializeThread;
import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.misc.ViewUtilities;
import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.services.core.properties.SdkProperties;
import com.unity3d.services.core.request.SDKMetrics;
import com.unity3d.services.core.webview.bridge.CallbackStatus;
import com.unity3d.services.core.webview.bridge.Invocation;
import com.unity3d.services.core.webview.bridge.NativeCallback;
import com.unity3d.services.core.webview.bridge.WebViewBridge;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class WebViewApp {

	private static WebViewApp _currentApp;
	private static ConditionVariable _conditionVariable;
	private static final int INVOKE_JS_CHARS_LENGTH = 22;

	private boolean _webAppLoaded = false;
	private WebView _webView;
	private Configuration _configuration;
	private HashMap<String, NativeCallback> _nativeCallbacks;
	private static AtomicReference<Boolean> _initialized = new AtomicReference<Boolean>(false);
	private static AtomicReference<String> _webAppFailureMessage = new AtomicReference<>();
	private static AtomicReference<Integer> _webAppFailureCode = new AtomicReference<>();

	private WebViewApp (Configuration configuration) {
		setConfiguration(configuration);
		WebViewBridge.setClassTable(getConfiguration().getWebAppApiClassList());
		_webView = new WebView(ClientProperties.getApplicationContext());
		_webView.setWebViewClient(new WebAppClient());
		_webView.setWebChromeClient(new WebAppChromeClient());
	}

	public WebViewApp() { }

	public void setWebAppLoaded(boolean loaded) {
		_webAppLoaded = loaded;
	}

	public boolean isWebAppLoaded() {
		return _webAppLoaded;
	}

	public void setWebAppFailureMessage(String message) {
		_webAppFailureMessage.set(message);
	}

	public void setWebAppFailureCode(int code) {
		_webAppFailureCode.set(code);
	}

	public String getWebAppFailureMessage() {
		return _webAppFailureMessage.get();
	}

	public int getWebAppFailureCode() {
		return _webAppFailureCode.get();
	}

	public void setWebAppInitialized (boolean initialized) {
		_initialized.set(initialized);
		_conditionVariable.open();
	}

	public void resetWebViewAppInitialization() {
		_webAppLoaded = false;
		_webAppFailureCode.set(-1);
		_webAppFailureMessage.set("");
		_initialized.set(false);
	}

	public boolean isWebAppInitialized () {
		return _initialized.get();
	}

	public WebView getWebView () {
		return _webView;
	}

	public void setWebView (WebView webView) {
		_webView = webView;
	}

	public Configuration getConfiguration () {
		return _configuration;
	}

	public void setConfiguration (Configuration configuration) {
		_configuration = configuration;
	}

	private void invokeJavascriptMethod(String className, String methodName, JSONArray params) throws JSONException {
		String paramsString = params.toString();
		int stringLength = INVOKE_JS_CHARS_LENGTH + className.length() + methodName.length() + paramsString.length();
		StringBuilder sb = new StringBuilder(stringLength);
		sb.append("javascript:window.");
		sb.append(className);
		sb.append(".");
		sb.append(methodName);
		sb.append("(");
		sb.append(paramsString);
		sb.append(");");
		String javaScriptString = sb.toString();
		DeviceLog.debug("Invoking javascript: " + javaScriptString);
		getWebView().invokeJavascript(javaScriptString);
	}

	public boolean sendEvent (Enum eventCategory, Enum eventId, Object... params) {
		if(!isWebAppLoaded()) {
			DeviceLog.debug("sendEvent ignored because web app is not loaded");
			return false;
		}

		JSONArray paramList = new JSONArray();
		paramList.put(eventCategory.name());
		paramList.put(eventId.name());

		for (Object o : params) {
			paramList.put(o);
		}

		try {
			invokeJavascriptMethod("nativebridge", "handleEvent", paramList);
		} catch(Exception e) {
			DeviceLog.exception("Error while sending event to WebView", e);
			return false;
		}

		return true;
	}

	public boolean invokeMethod(String className, String methodName, Method callback, Object... params) {
		if(!isWebAppLoaded()) {
			DeviceLog.debug("invokeMethod ignored because web app is not loaded");
			return false;
		}

		JSONArray paramList = new JSONArray();
		paramList.put(className);
		paramList.put(methodName);

		if (callback != null) {
			NativeCallback nativeCallback = new NativeCallback(callback);

			addCallback(nativeCallback);
			paramList.put(nativeCallback.getId());
		}
		else {
			paramList.put(null);
		}

		if (params != null) {
			for (Object o : params) {
				paramList.put(o);
			}
		}

		try {
			invokeJavascriptMethod("nativebridge", "handleInvocation", paramList);
		}
		catch (Exception e) {
			DeviceLog.exception("Error invoking javascript method", e);
			return false;
		}

		return true;
	}

	public boolean invokeCallback(Invocation invocation) {
		if(!isWebAppLoaded()) {
			DeviceLog.debug("invokeBatchCallback ignored because web app is not loaded");
			return false;
		}

		JSONArray responseList = new JSONArray();

		ArrayList<ArrayList<Object>> responses = invocation.getResponses();
		if(responses != null && !responses.isEmpty()) {
			for (ArrayList<Object> response : responses) {
				CallbackStatus status = (CallbackStatus)response.get(0);
				Enum error = (Enum)response.get(1);
				Object[] params = (Object[])response.get(2);
				String callbackId = (String)params[0];
				params = Arrays.copyOfRange(params, 1, params.length);

				ArrayList<Object> tmp = new ArrayList<>();
				tmp.add(callbackId);
				tmp.add(status.toString());

				JSONArray paramArray = new JSONArray();
				if (error != null) {
					paramArray.put(error.name());
				}
				for(Object o : params) {
					paramArray.put(o);
				}
				tmp.add(paramArray);

				JSONArray paramList = new JSONArray();
				for (Object o : tmp) {
					paramList.put(o);
				}

				responseList.put(paramList);
			}
		}

		try {
			invokeJavascriptMethod("nativebridge", "handleCallback", responseList);
		} catch (Exception e) {
			DeviceLog.exception("Error while invoking batch response for WebView", e);
		}

		return true;
	}

	public void addCallback (NativeCallback callback) {
		if (_nativeCallbacks == null) {
			_nativeCallbacks = new HashMap<>();
		}

		synchronized (_nativeCallbacks) {
			_nativeCallbacks.put(callback.getId(), callback);
		}
	}

	public void removeCallback (NativeCallback callback) {
		if (_nativeCallbacks == null) {
			return;
		}

		synchronized (_nativeCallbacks) {
			_nativeCallbacks.remove(callback.getId());
		}
	}

	public NativeCallback getCallback (String callbackId) {
		synchronized (_nativeCallbacks) {
			return _nativeCallbacks.get(callbackId);
		}
	}

	/* STATIC METHODS */

	public static WebViewApp getCurrentApp () {
		return _currentApp;
	}

	public static void setCurrentApp (WebViewApp app) {
		_currentApp = app;
	}

	public static boolean create (final Configuration configuration) throws IllegalThreadStateException {
		DeviceLog.entered();

		if (Thread.currentThread().equals(Looper.getMainLooper().getThread())) {
			throw new IllegalThreadStateException("Cannot call create() from main thread!");
		}

		Utilities.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				WebViewApp webViewApp;

				try {
					webViewApp = new WebViewApp(configuration);
				}
				catch (Exception e) {
					DeviceLog.error("Couldn't construct WebViewApp");
					_conditionVariable.open();
					return;
				}

				String queryString = "?platform=android";

				try {
					if(configuration.getWebViewUrl() != null) {
						queryString = queryString + "&origin=" + URLEncoder.encode(configuration.getWebViewUrl(), "UTF-8");
					}
				} catch(UnsupportedEncodingException e) {
					DeviceLog.exception("Unsupported charset when encoding origin url", e);
				}

				try {
					if(configuration.getWebViewVersion() != null) {
						queryString = queryString + "&version=" + URLEncoder.encode(configuration.getWebViewVersion(), "UTF-8");
					}
				} catch(UnsupportedEncodingException e) {
					DeviceLog.exception("Unsupported charset when encoding webview version", e);
				}

				webViewApp.getWebView().loadDataWithBaseURL("file://" + SdkProperties.getLocalWebViewFile() + queryString, configuration.getWebViewData(), "text/html", "UTF-8", null);

				setCurrentApp(webViewApp);
			}
		});

		_conditionVariable = new ConditionVariable();
		final boolean webViewCreateDidNotTimeout = _conditionVariable.block(configuration.getWebViewAppCreateTimeout());
		final boolean webAppDefined = WebViewApp.getCurrentApp() != null;
		final boolean webAppInitialized = webAppDefined && WebViewApp.getCurrentApp().isWebAppInitialized();

		boolean createdSuccessfully = webViewCreateDidNotTimeout && webAppDefined && webAppInitialized;

		if (!createdSuccessfully) {
			SDKMetrics.getInstance().sendEventWithTags("native_webview_creation_failed", new HashMap<String, String>() {{
				put("wto", "" + !webViewCreateDidNotTimeout);
				put("wad", "" + webAppDefined);
				put("wai", "" + webAppInitialized);
			}});
		}
		return createdSuccessfully;
	}

	/* PRIVATE CLASSES */

	private class WebAppClient extends WebViewClient {

		@Override
		public boolean onRenderProcessGone(android.webkit.WebView view, final RenderProcessGoneDetail detail) {
			Utilities.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// We need to shut down current Ad Unit Activity in case we are showing an ad
					if (AdUnit.getAdUnitActivity() != null) {
						AdUnit.getAdUnitActivity().finish();
					}

					// Since WebViewHandler won't able to delete WebView due to race condition we do it here.
					// Still it is questionable if we need to do it. Since Ad Unit activity will be destroyed anyway.
					if (WebViewApp.getCurrentApp() != null && WebViewApp.getCurrentApp().getWebView() != null) {
						ViewUtilities.removeViewFromParent(WebViewApp.getCurrentApp().getWebView());
					}

					// Launch reset process
					InitializeThread.reset();
				}
			});

			DeviceLog.error("UnityAds Sdk WebView onRenderProcessGone : " + detail.toString());
			SDKMetrics.getInstance().sendEventWithTags("native_webview_render_process_gone", new HashMap<String, String>() {{
				// Only apply tags if minimum API Level applies
				if (Build.VERSION.SDK_INT >= 26) {
					put("dc", "" + detail.didCrash());
					put("pae", "" + detail.rendererPriorityAtExit());
				}
			}});

			// the ads sdk cannot be recovered but return true to prevent the crash
			return true;
		}

		@Override
		public void onPageFinished(android.webkit.WebView webview, String url) {
			super.onPageFinished(webview, url);
			DeviceLog.debug("onPageFinished url: " + url);
		}

		@Override
		public boolean shouldOverrideUrlLoading(android.webkit.WebView view, String url) {
			DeviceLog.debug("Trying to load url: " + url);
			return false;
		}

		@Override
		public void onReceivedError(android.webkit.WebView view, WebResourceRequest request, WebResourceError error) {
			super.onReceivedError(view, request, error);
			if (view != null) {
				DeviceLog.error("WEBVIEW_ERROR: " + view.toString());
			}
			if (request != null) {
				DeviceLog.error("WEBVIEW_ERROR: " + request.toString());
			}
			if (error != null) {
				DeviceLog.error("WEBVIEW_ERROR: " + error.toString());
			}
		}
	}

	private class WebAppChromeClient extends WebChromeClient {
		@SuppressWarnings("deprecation")
		@Override
		public void onConsoleMessage(String message, int lineNumber, String sourceID) {
			String sourceFile = sourceID;
			File tmp = null;

			try {
				tmp = new File(sourceID);
			}
			catch (Exception e) {
				DeviceLog.exception("Could not handle sourceId", e);
			}

			if (tmp != null)
				sourceFile = tmp.getName();

			// Only log JavaScript console if Android version < 4.4
			// 4.4 introduced Chromium that logs JS console messages
			// itself.
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
				DeviceLog.debug("JavaScript (sourceId=" + sourceFile + ", line=" + lineNumber + "): " + message);
			}
		}
	}
}
