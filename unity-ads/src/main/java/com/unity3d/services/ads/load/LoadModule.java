package com.unity3d.services.ads.load;

import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.UnityAdsLoadOptions;
import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.configuration.IInitializationListener;
import com.unity3d.services.core.configuration.IInitializationNotificationCenter;
import com.unity3d.services.core.configuration.InitializationNotificationCenter;
import com.unity3d.services.core.device.Device;
import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.properties.SdkProperties;
import com.unity3d.services.core.request.SDKMetrics;
import com.unity3d.services.core.webview.WebViewApp;
import com.unity3d.services.core.webview.bridge.CallbackStatus;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoadModule implements IInitializationListener {

	private static LoadModule instance;
	private static Configuration _configuration;

	public static LoadModule getInstance() {
		if (instance == null) {
			IInitializationNotificationCenter initializationNotificationCenter = InitializationNotificationCenter.getInstance();
			instance = new LoadModule(initializationNotificationCenter);
		}
		return instance;
	}

	private class LoadEventState {
		public String placementId;
		public String listenerId;
		public long time;
		public IUnityAdsLoadListener listener;
		public Runnable timeoutRunnable;
		public UnityAdsLoadOptions loadOptions;

		LoadEventState(String placementId, String listenerId, IUnityAdsLoadListener listener, Runnable timeoutRunnable, long elapsedRealtime, UnityAdsLoadOptions loadOptions) {
			this.placementId = placementId;
			this.listenerId = listenerId;
			this.listener = listener;
			this.time = elapsedRealtime;
			this.timeoutRunnable = timeoutRunnable;
			this.loadOptions = loadOptions;
		}
	}

	private final LinkedList<LoadEventState> _loadEventBuffer;
	private final LinkedHashMap<String, LoadEventState> _loadListeners;
	private Method _loadCallback;
	private Handler _handler;
	private ExecutorService _executorService;

	public LoadModule(IInitializationNotificationCenter initializationNotificationCenter) {
		_loadEventBuffer = new LinkedList<>();
		_loadListeners = new LinkedHashMap<>();
		_executorService = Executors.newSingleThreadExecutor();
		try {
			_loadCallback = LoadModule.class.getMethod("loadCallback", CallbackStatus.class);
		} catch (NoSuchMethodException e) {
			_loadCallback = null;
		}
		_handler = new Handler(Looper.getMainLooper());
		if (_configuration == null) {
			_configuration = new Configuration();
		}

		initializationNotificationCenter.addListener(this);
	}

	public void load(final String placementId, final UnityAdsLoadOptions loadOptions, final IUnityAdsLoadListener listener) {
		if (TextUtils.isEmpty(placementId)) {
			Utilities.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					listener.onUnityAdsFailedToLoad(placementId);
				}
			});
			return;
		}

		final LoadEventState loadEventState = createLoadEvent(placementId, listener, loadOptions);

		if (SdkProperties.getCurrentInitializationState() == SdkProperties.InitializationState.INITIALIZED_SUCCESSFULLY) {
			_executorService.submit(new Runnable() {
				@Override
				public void run() {
					LoadModule.this.runLoadRequest(loadEventState);
				}
			});
		} else if (SdkProperties.getCurrentInitializationState() == SdkProperties.InitializationState.INITIALIZED_FAILED) {
			sendAdFailedToLoad(placementId, loadEventState.listenerId);
		} else {
			synchronized (_loadEventBuffer) {
				_loadEventBuffer.add(loadEventState);
			}
		}
	}

	private void runLoadRequest(LoadEventState loadEventState) {
		try {
			if (!load(loadEventState)) {
				throw new Exception("Failed to send load request to WebView");
			}
		} catch (Exception e) {
			DeviceLog.error(e.getMessage());
			sendAdFailedToLoad(loadEventState.placementId, loadEventState.listenerId);
		}
	}

	public void sendAdLoaded(final String placementId, String listenerId) {
		LoadEventState loadEventState;
		synchronized (_loadListeners) {
			loadEventState = _loadListeners.remove(listenerId);
		}

		if (loadEventState == null) {
			return;
		}

		_handler.removeCallbacks(loadEventState.timeoutRunnable);

		final IUnityAdsLoadListener listener = loadEventState.listener;
		if (listener == null) {
			return;
		}

		Utilities.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				listener.onUnityAdsAdLoaded(placementId);
			}
		});
	}

	public void sendAdFailedToLoad(final String placementId, String listenerId) {
		LoadEventState loadEventState;
		synchronized (_loadListeners) {
			loadEventState =_loadListeners.remove(listenerId);
		}

		if (loadEventState == null) {
			return;
		}

		_handler.removeCallbacks(loadEventState.timeoutRunnable);

		final IUnityAdsLoadListener listener = loadEventState.listener;
		if (listener == null) {
			return;
		}

		Utilities.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				listener.onUnityAdsFailedToLoad(placementId);
			}
		});
	}

	private LoadEventState createLoadEvent(final String placementId, IUnityAdsLoadListener listener, UnityAdsLoadOptions loadOptions) {
		final String listenerId = UUID.randomUUID().toString();

		Runnable timeoutRunnable = new Runnable() {
			@Override
			public void run() {
				LoadModule.this.sendAdFailedToLoad(placementId, listenerId);
			}
		};

		LoadEventState loadEventState = new LoadEventState(placementId, listenerId, listener, timeoutRunnable, Device.getElapsedRealtime(), loadOptions);

		synchronized (_loadListeners) {
			_loadListeners.put(listenerId, loadEventState);
		}

		_handler.postDelayed(timeoutRunnable, _configuration.getNoFillTimeout());

		return loadEventState;
	}

	@Override
	public void onSdkInitialized() {
		final LoadEventState[] loadEventStates;
		synchronized (_loadEventBuffer) {
			loadEventStates = new LoadEventState[_loadEventBuffer.size()];
			_loadEventBuffer.toArray(loadEventStates);
			_loadEventBuffer.clear();
		}

		_executorService.submit(new Runnable() {
			@Override
			public void run() {
				for (LoadEventState loadEventState : loadEventStates) {
					runLoadRequest(loadEventState);
				}
			}
		});
	}

	@Override
	public void onSdkInitializationFailed(String message, int code) {
		final LoadEventState[] loadEventStates;
		synchronized (_loadEventBuffer) {
			loadEventStates = new LoadEventState[_loadEventBuffer.size()];
			_loadEventBuffer.toArray(loadEventStates);
			_loadEventBuffer.clear();
		}

		for (LoadEventState loadEventState : loadEventStates) {
			sendAdFailedToLoad(loadEventState.placementId, loadEventState.listenerId);
		}
	}

	private static ConditionVariable _waitLoadStatus;
	private static volatile CallbackStatus _lastStatus = CallbackStatus.ERROR;

	private synchronized boolean load(LoadEventState loadEvent) throws Exception {
		if (_loadCallback == null) {
			throw new Exception("Callback for load request was not found");
		}

		JSONObject options = new JSONObject();
		options.put("listenerId", loadEvent.listenerId);
		options.put("placementId", loadEvent.placementId);
		options.put("time", loadEvent.time);
		options.put("options", loadEvent.loadOptions.getData());

		_lastStatus = CallbackStatus.ERROR;
		_waitLoadStatus = new ConditionVariable();
		WebViewApp.getCurrentApp().invokeMethod("webview", "load", this._loadCallback, options);
		boolean success = _waitLoadStatus.block(_configuration.getLoadTimeout());
		_waitLoadStatus = null;
		if (!success) {
			SDKMetrics.getInstance().sendEvent("native_load_callback_failed");
		}
		return success && _lastStatus == CallbackStatus.OK;
	}

	public static void loadCallback(CallbackStatus status) {
		if (_waitLoadStatus != null) {
			_lastStatus = status;
			_waitLoadStatus.open();
		}
	}

	public static void setConfiguration (Configuration configuration) {
		_configuration = configuration;
	}

}
