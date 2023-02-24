package com.unity3d.services.core.webview;

import android.os.Build;
import android.os.ConditionVariable;
import android.os.Looper;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebViewClient;

import com.unity3d.services.ads.api.AdUnit;
import com.unity3d.services.core.configuration.ErrorState;
import com.unity3d.services.core.webview.bridge.IWebViewBridgeInvoker;
import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.configuration.InitializeThread;
import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.misc.ViewUtilities;
import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.services.core.properties.SdkProperties;
import com.unity3d.services.core.request.metrics.SDKMetrics;
import com.unity3d.services.core.webview.bridge.CallbackStatus;
import com.unity3d.services.core.webview.bridge.Invocation;
import com.unity3d.services.core.webview.bridge.NativeCallback;
import com.unity3d.services.core.webview.bridge.WebViewBridge;

import org.json.JSONArray;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class WebViewApp implements IWebViewBridgeInvoker {

	private static WebViewApp _currentApp;
	private static ConditionVariable _conditionVariable;
	private static final int INVOKE_JS_CHARS_LENGTH = 22;

	private boolean _webAppLoaded = false;
	private WebView _webView;
	private Configuration _configuration;
	private final HashMap<String, NativeCallback> _nativeCallbacks = new HashMap<>();
	private static final AtomicReference<Boolean> _initialized = new AtomicReference<>(false);
	private static final AtomicReference<String> _webAppFailureMessage = new AtomicReference<>();
	private static final AtomicReference<Integer> _webAppFailureCode = new AtomicReference<>();

	public WebViewApp (Configuration configuration, boolean useWebViewWithCache, boolean shouldNotRequireGesturePlayback) {
		setConfiguration(configuration);
		WebViewBridge.setClassTable(getConfiguration().getWebAppApiClassList());
		_webView = useWebViewWithCache ? new WebViewWithCache(ClientProperties.getApplicationContext(), shouldNotRequireGesturePlayback) : new WebView(ClientProperties.getApplicationContext(), shouldNotRequireGesturePlayback);
		_webView.setWebViewClient(new WebAppClient());
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

	@SuppressWarnings("SameParameterValue")
	private void invokeJavascriptMethod(String className, String methodName, JSONArray params) {
		String javaScript = buildInvokeJavascript(className, methodName, params);
		DeviceLog.debug("Invoking javascript: %s", javaScript);
		getWebView().invokeJavascript(javaScript);
	}

	@SuppressWarnings("StringBufferReplaceableByString")
	private String buildInvokeJavascript(String className, String methodName, JSONArray params) {
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
		return sb.toString();
	}

	@SuppressWarnings("rawtypes")
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

	@Override
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

	@SuppressWarnings("rawtypes")
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
		synchronized (_nativeCallbacks) {
			_nativeCallbacks.put(callback.getId(), callback);
		}
	}

	public void removeCallback (NativeCallback callback) {
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

	public static ErrorState create (final Configuration configuration) throws IllegalThreadStateException {
		return create(configuration, false);
	}

	public static ErrorState create (final Configuration configuration, final boolean useRemoteUrl) throws IllegalThreadStateException {
		DeviceLog.entered();

		if (useRemoteUrl) return createWithRemoteUrl(configuration);

		if (Thread.currentThread().equals(Looper.getMainLooper().getThread())) {
			throw new IllegalThreadStateException("Cannot call create() from main thread!");
		}

		Utilities.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				WebViewApp webViewApp;

				try {
					webViewApp = new WebViewApp(configuration, configuration.getExperiments().isWebAssetAdCaching(), configuration.getExperiments().isWebGestureNotRequired());
				}
				catch (Exception e) {
					DeviceLog.error("Unity Ads SDK unable to create WebViewApp");
					_conditionVariable.open();
					return;
				}

				WebViewUrlBuilder webViewUrlBuilder =  new WebViewUrlBuilder("file://" + SdkProperties.getLocalWebViewFile(), configuration);
				String baseUrl = webViewUrlBuilder.getUrlWithQueryString();

				webViewApp.getWebView().loadDataWithBaseURL(baseUrl, configuration.getWebViewData(), "text/html", "UTF-8", null);

				setCurrentApp(webViewApp);
			}
		});

		_conditionVariable = new ConditionVariable();
		final boolean webViewCreateDidNotTimeout = _conditionVariable.block(configuration.getWebViewAppCreateTimeout());
		final boolean webAppDefined = WebViewApp.getCurrentApp() != null;
		final boolean webAppInitialized = webAppDefined && WebViewApp.getCurrentApp().isWebAppInitialized();

		boolean createdSuccessfully = webViewCreateDidNotTimeout && webAppDefined && webAppInitialized;

		if (!createdSuccessfully) {
			if (!webViewCreateDidNotTimeout) {
				return ErrorState.CreateWebviewTimeout;
			}

			if (WebViewApp.getCurrentApp() == null) {
				return ErrorState.CreateWebview;
			}

			return WebViewApp.getCurrentApp().getErrorStateFromWebAppCode();
		}
		return null;
	}

	private static ErrorState createWithRemoteUrl(final Configuration configuration) {
		if (Thread.currentThread().equals(Looper.getMainLooper().getThread())) {
			throw new IllegalThreadStateException("Cannot call create() from main thread!");
		}

		Utilities.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				WebViewApp webViewApp;

				try {
					webViewApp = new WebViewApp(configuration, true, configuration.getExperiments().isWebGestureNotRequired());
				}
				catch (Exception e) {
					DeviceLog.error("Unity Ads SDK unable to create WebViewApp");
					_conditionVariable.open();
					return;
				}

				WebViewUrlBuilder webViewUrlBuilder =  new WebViewUrlBuilder(configuration.getWebViewUrl(),  configuration);
				String baseUrl = webViewUrlBuilder.getUrlWithQueryString();

				webViewApp.getWebView().loadUrl(baseUrl);

				setCurrentApp(webViewApp);
			}
		});

		_conditionVariable = new ConditionVariable();
		final boolean webViewCreateDidNotTimeout = _conditionVariable.block(configuration.getWebViewAppCreateTimeout());
		final boolean webAppDefined = WebViewApp.getCurrentApp() != null;
		final boolean webAppInitialized = webAppDefined && WebViewApp.getCurrentApp().isWebAppInitialized();

		boolean createdSuccessfully = webViewCreateDidNotTimeout && webAppDefined && webAppInitialized;

		if (!createdSuccessfully) {
			if (!webViewCreateDidNotTimeout) {
				return ErrorState.CreateWebviewTimeout;
			}

			if (WebViewApp.getCurrentApp() == null) {
				return ErrorState.CreateWebview;
			}

			return WebViewApp.getCurrentApp().getErrorStateFromWebAppCode();
		}
		return null;
	}


	public ErrorState getErrorStateFromWebAppCode() {
		int failureCode = getWebAppFailureCode();
		if (failureCode == 1) {
			return ErrorState.CreateWebviewGameIdDisabled;
		}
		if (failureCode == 2) {
			return ErrorState.CreateWebviewConfigError;
		}
		if (failureCode == 3) {
			return ErrorState.CreateWebviewInvalidArgument;
		}
		return ErrorState.CreateWebview; //unknown
	}

	/* PRIVATE CLASSES */

	private static class WebAppClient extends WebViewClient {

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

			DeviceLog.error("UnityAds SDK WebView render process gone with following reason : " + detail.toString());
			SDKMetrics.getInstance().sendEvent("native_webview_render_process_gone", new HashMap<String, String>() {{
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
			DeviceLog.debug("Unity Ads SDK finished loading URL inside WebView: " + url);
		}

		@Override
		public boolean shouldOverrideUrlLoading(android.webkit.WebView view, String url) {
			DeviceLog.debug("Unity Ads SDK attempts to load URL inside WebView: " + url);
			return false;
		}

		@Override
		public void onReceivedError(android.webkit.WebView view, WebResourceRequest request, WebResourceError error) {
			super.onReceivedError(view, request, error);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && request != null && error != null) {
				DeviceLog.error("Unity Ads SDK encountered an error (code: " +  error.getErrorCode() + ")  in WebView while loading a resource " + request.getUrl());
			} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && request != null) {
				DeviceLog.error("Unity Ads SDK encountered an error in WebView while loading a resource " + request.getUrl());
			} else {
				DeviceLog.error("Unity Ads SDK encountered an error in WebView while loading a resource");
			}
		}
	}
}
