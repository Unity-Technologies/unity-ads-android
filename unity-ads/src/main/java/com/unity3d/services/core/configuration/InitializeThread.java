package com.unity3d.services.core.configuration;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.ConditionVariable;

import com.unity3d.services.core.api.Lifecycle;
import com.unity3d.services.core.connectivity.ConnectivityMonitor;
import com.unity3d.services.core.connectivity.IConnectivityListener;
import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.services.core.properties.SdkProperties;
import com.unity3d.services.core.request.WebRequest;
import com.unity3d.services.core.webview.WebViewApp;

import java.io.File;
import java.net.MalformedURLException;

public class InitializeThread extends Thread  {
	private static InitializeThread _thread;
	private InitializeState _state;
	private boolean _stopThread = false;

	private InitializeThread(InitializeState state) {
		super();
		_state = state;
	}

	@Override
	public void run() {
		try {
			while ((_state != null && !(_state instanceof InitializeStateComplete)) && !_stopThread) {
				try {
					_state = _state.execute();
				} catch (Exception e) {
					DeviceLog.exception("Unity Ads SDK encountered an error during initialization, cancel initialization", e);
					_state = new InitializeThread.InitializeStateForceReset();
				} catch (OutOfMemoryError oom) {
					DeviceLog.exception("Application doesn't have enough memory to initialize Unity Ads SDK", new Exception(oom));
					_state = new InitializeThread.InitializeStateForceReset();
				}
			}
		} catch (OutOfMemoryError oom) {
			//Do Nothing
		}
		_thread = null;
	}

	public void quit() {
		_stopThread = true;
	}

	public static synchronized void initialize(Configuration configuration) {
		if (_thread == null) {
			_thread = new InitializeThread(new InitializeStateReset(configuration));
			_thread.setName("UnityAdsInitializeThread");
			_thread.start();
		}
	}

	public static synchronized void reset() {
		if (_thread == null) {
			_thread = new InitializeThread(new InitializeStateForceReset());
			_thread.setName("UnityAdsResetThread");
			_thread.start();
		}
	}

	/* STATE CLASSES */

	private abstract static class InitializeState {
		public abstract InitializeState execute();
	}

	public static class InitializeStateReset extends InitializeState {
		private Configuration _configuration;

		public InitializeStateReset(Configuration configuration) {
			_configuration = configuration;
		}

		@Override
		public InitializeState execute() {
			DeviceLog.debug("Unity Ads init: starting init");

			final ConditionVariable cv = new ConditionVariable();
			final WebViewApp currentApp = WebViewApp.getCurrentApp();
			boolean success = true;

			if (currentApp != null) {
				currentApp.setWebAppLoaded(false);
				currentApp.setWebAppInitialized(false);

				if (currentApp.getWebView() != null) {
					Utilities.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							currentApp.getWebView().destroy();
							currentApp.setWebView(null);
							cv.open();
						}
					});

					success = cv.block(10000);
				}

				if (!success) {
					return new InitializeThread.InitializeStateError("reset webapp", new Exception("Reset failed on opening ConditionVariable"), _configuration);
				}
			}

			if (Build.VERSION.SDK_INT > 13) {
				unregisterLifecycleCallbacks();
			}

			SdkProperties.setCacheDirectory(null);
			File cacheDir = SdkProperties.getCacheDirectory();
			if (cacheDir == null) {
				return new InitializeThread.InitializeStateError("reset webapp", new Exception("Cache directory is NULL"), _configuration);
			}

			SdkProperties.setInitialized(false);

			_configuration.setConfigUrl(SdkProperties.getConfigUrl());

			for (String moduleName : _configuration.getModuleConfigurationList()) {
				IModuleConfiguration moduleConfiguration = _configuration.getModuleConfiguration(moduleName);
				if (moduleConfiguration != null) {
					moduleConfiguration.resetState(_configuration);
				}
			}

			return new InitializeStateInitModules(_configuration);
		}

		@TargetApi(14)
		private void unregisterLifecycleCallbacks () {
			if (Lifecycle.getLifecycleListener() != null) {
				if (ClientProperties.getApplication() != null) {
					ClientProperties.getApplication().unregisterActivityLifecycleCallbacks(Lifecycle.getLifecycleListener());
				}

				Lifecycle.setLifecycleListener(null);
			}
		}
	}

	public static class InitializeStateForceReset extends InitializeStateReset {

		public InitializeStateForceReset() {
			super(new Configuration());
		}

		@Override
		public InitializeState execute() {
			super.execute();
			return null;
		}
	}

	public static class InitializeStateInitModules extends InitializeState {
		private Configuration _configuration;

		public InitializeStateInitModules(Configuration configuration) {
			_configuration = configuration;
		}

		public Configuration getConfiguration() {
			return _configuration;
		}

		@Override
		public InitializeState execute() {
			for (String moduleName : _configuration.getModuleConfigurationList()) {
				IModuleConfiguration moduleConfiguration = _configuration.getModuleConfiguration(moduleName);
				if (moduleConfiguration != null) {
					if(!moduleConfiguration.initModuleState(_configuration)) {
						return null;
					}
				}
			}

			return new InitializeStateConfig(_configuration);
		}
	}

	public static class InitializeStateConfig extends InitializeState {
		private int _retries = 0;
		private int _maxRetries = 6;
		private int _retryDelay = 5; // seconds
		private Configuration _configuration;

		public InitializeStateConfig(Configuration configuration) {
			_configuration = configuration;
		}

		@Override
		public InitializeState execute() {
			DeviceLog.info("Unity Ads init: load configuration from " + SdkProperties.getConfigUrl());

			try {
				_configuration.makeRequest();
			} catch (Exception e) {
				if (_retries < _maxRetries) {
					_retryDelay = _retryDelay * 2;
					_retries++;
					return new InitializeStateRetry(this, _retryDelay);
				}

				return new InitializeStateNetworkError(e, this, _configuration);
			}

			return new InitializeStateLoadCache(_configuration);
		}
	}

	public static class InitializeStateLoadCache extends InitializeState {
		private Configuration _configuration;

		public InitializeStateLoadCache(Configuration configuration) {
			_configuration = configuration;
		}

		public Configuration getConfiguration() {
			return _configuration;
		}

		@Override
		public InitializeState execute() {
			DeviceLog.debug("Unity Ads init: check if webapp can be loaded from local cache");

			byte[] localWebViewData;

			try {
				localWebViewData = Utilities.readFileBytes(new File(SdkProperties.getLocalWebViewFile()));
			} catch (Exception e) {
				DeviceLog.debug("Unity Ads init: webapp not found in local cache: " + e.getMessage());
				return new InitializeStateLoadWeb(_configuration);
			}

			String localWebViewHash = Utilities.Sha256(localWebViewData);

			if (localWebViewHash != null && localWebViewHash.equals(_configuration.getWebViewHash())) {
				String webViewDataString;

				try {
					webViewDataString = new String(localWebViewData, "UTF-8");
				} catch (Exception e) {
					return new InitializeStateError("load cache", e, _configuration);
				}

				DeviceLog.info("Unity Ads init: webapp loaded from local cache");
				return new InitializeStateCreate(_configuration, webViewDataString);
			}

			return new InitializeStateLoadWeb(_configuration);
		}
	}

	public static class InitializeStateLoadWeb extends InitializeState {
		private Configuration _configuration;
		private int _retries = 0;
		private int _maxRetries = 6;
		private int _retryDelay = 5; // seconds

		public InitializeStateLoadWeb(Configuration configuration) {
			_configuration = configuration;
		}

		public Configuration getConfiguration() {
			return _configuration;
		}

		@Override
		public InitializeState execute() {
			DeviceLog.info("Unity Ads init: loading webapp from " + _configuration.getWebViewUrl());

			WebRequest request;

			try {
				request = new WebRequest(_configuration.getWebViewUrl(), "GET", null);
			}
			catch (MalformedURLException e) {
				DeviceLog.exception("Malformed URL", e);
				return new InitializeStateError("make webrequest", e, _configuration);
			}

			String webViewData;

			try {
				webViewData = request.makeRequest();
			} catch (Exception e) {
				if (_retries < _maxRetries) {
					_retryDelay = _retryDelay * 2;
					_retries++;
					return new InitializeStateRetry(this, _retryDelay);
				}

				return new InitializeStateNetworkError(e, this, _configuration);
			}

			String webViewHash = _configuration.getWebViewHash();
			if (webViewHash != null && !Utilities.Sha256(webViewData).equals(webViewHash)) {
				return new InitializeStateError("load web", new Exception("Invalid webViewHash"), _configuration);
			}

			if(webViewHash != null) {
				Utilities.writeFile(new File(SdkProperties.getLocalWebViewFile()), webViewData);
			}

			return new InitializeStateCreate(_configuration, webViewData);
		}
	}

	public static class InitializeStateCreate extends InitializeState {
		private Configuration _configuration;
		private String _webViewData;

		public InitializeStateCreate(Configuration configuration, String webViewData) {
			_configuration = configuration;
			_webViewData = webViewData;
		}

		public Configuration getConfiguration() {
			return _configuration;
		}

		public String getWebData() {
			return _webViewData;
		}

		@Override
		public InitializeState execute() {
			DeviceLog.debug("Unity Ads init: creating webapp");

			final Configuration configuration = _configuration;
			configuration.setWebViewData(_webViewData);
			boolean createSuccessFull;

			try {
				createSuccessFull = WebViewApp.create(configuration);
			}
			catch (IllegalThreadStateException e) {
				DeviceLog.exception("Illegal Thread", e);
				return new InitializeStateError("create webapp", e, _configuration);
			}

			if (createSuccessFull) {
				return new InitializeStateComplete(_configuration);
			}
			else {
				DeviceLog.error("Unity Ads WebApp creation failed!");
				return new InitializeStateError("create webapp", new Exception("Creation of WebApp failed!"), _configuration);
			}
		}
	}

	public static class InitializeStateComplete extends InitializeState {
		private Configuration _configuration;

		public InitializeStateComplete(Configuration configuration) {
			_configuration = configuration;
		}

		@Override
		public InitializeState execute() {
			for (String moduleName : _configuration.getModuleConfigurationList()) {
				IModuleConfiguration moduleConfiguration = _configuration.getModuleConfiguration(moduleName);
				if (moduleConfiguration != null) {
					moduleConfiguration.initCompleteState(_configuration);
				}
			}

			return null;
		}
	}

	public static class InitializeStateError extends InitializeState {
		String _state;
		Exception _exception;
		protected Configuration _configuration;

		public InitializeStateError(String state, Exception exception, Configuration configuration) {
			_state = state;
			_exception = exception;
			_configuration = configuration;
		}

		@Override
		public InitializeState execute() {
			DeviceLog.error("Unity Ads init: halting init in " + _state + ": " + _exception.getMessage());

			for (String moduleName : _configuration.getModuleConfigurationList()) {
				IModuleConfiguration moduleConfiguration = _configuration.getModuleConfiguration(moduleName);
				if (moduleConfiguration != null) {
					moduleConfiguration.initErrorState(_configuration, _state, _exception.getMessage());
				}
			}

			return null;
		}
	}

	public static class InitializeStateNetworkError extends InitializeStateError implements IConnectivityListener {
		protected static final int CONNECTED_EVENT_THRESHOLD_MS = 10000;
		protected static final int MAX_CONNECTED_EVENTS = 500;
		private static int _receivedConnectedEvents = 0;
		private static long _lastConnectedEventTimeMs = 0;

		private InitializeState _erroredState;
		private ConditionVariable _conditionVariable;

		public InitializeStateNetworkError(Exception exception, InitializeState erroredState, Configuration configuration) {
			super("network error", exception, configuration);
			_erroredState = erroredState;
		}

		@Override
		public InitializeState execute() {
			DeviceLog.error("Unity Ads init: network error, waiting for connection events");

			_conditionVariable = new ConditionVariable();
			ConnectivityMonitor.addListener(this);

			if (_conditionVariable.block(10000L * 60L)) {
				ConnectivityMonitor.removeListener(this);
				return _erroredState;
			}
			else {
				ConnectivityMonitor.removeListener(this);
				return new InitializeStateError("network error", new Exception("No connected events within the timeout!"), _configuration);
			}
		}

		@Override
		public void onConnected() {
			_receivedConnectedEvents++;

			DeviceLog.debug("Unity Ads init got connected event");
			if (shouldHandleConnectedEvent()) {
				_conditionVariable.open();
			}

			if (_receivedConnectedEvents > MAX_CONNECTED_EVENTS) {
				ConnectivityMonitor.removeListener(this);
			}

			_lastConnectedEventTimeMs = System.currentTimeMillis();
		}

		@Override
		public void onDisconnected() {
			DeviceLog.debug("Unity Ads init got disconnected event");
		}

		private boolean shouldHandleConnectedEvent () {
			if (System.currentTimeMillis() - _lastConnectedEventTimeMs >= CONNECTED_EVENT_THRESHOLD_MS &&
					_receivedConnectedEvents <= MAX_CONNECTED_EVENTS) {
				return true;
			}

			return false;
		}
	}

	public static class InitializeStateRetry extends InitializeState {
		InitializeState _state;
		int _delay;

		public InitializeStateRetry(InitializeState state, int delay) {
			_state = state;
			_delay = delay;
		}

		@Override
		public InitializeState execute() {
			DeviceLog.debug("Unity Ads init: retrying in " + _delay + " seconds");
			try {
				Thread.sleep(_delay * 1000L);
			} catch(Exception e) {
				DeviceLog.exception("Init retry interrupted", e);
			}

			return _state;
		}
	}
}
