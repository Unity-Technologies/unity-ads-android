package com.unity3d.services.core.configuration;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.ConditionVariable;
import android.text.TextUtils;

import com.unity3d.ads.UnityAds;
import com.unity3d.services.ads.token.TokenStorage;
import com.unity3d.services.core.api.DownloadLatestWebViewStatus;
import com.unity3d.services.core.api.Lifecycle;
import com.unity3d.services.core.connectivity.ConnectivityMonitor;
import com.unity3d.services.core.connectivity.IConnectivityListener;
import com.unity3d.services.core.lifecycle.CachedLifecycle;
import com.unity3d.services.core.lifecycle.LifecycleCache;
import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.services.core.properties.SdkProperties;
import com.unity3d.services.core.request.metrics.SDKMetrics;
import com.unity3d.services.core.request.WebRequest;
import com.unity3d.services.core.request.metrics.TSIMetric;
import com.unity3d.services.core.webview.WebViewApp;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;

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
			while (_state != null && !_stopThread) {
				try {
					_state = _state.execute();
				} catch (Exception e) {
					final String message = "Unity Ads SDK encountered an error during initialization, cancel initialization";
					DeviceLog.exception(message, e);
					Utilities.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							SdkProperties.notifyInitializationFailed(UnityAds.UnityAdsInitializationError.INTERNAL_ERROR, message);
						}
					});
					_state = new InitializeThread.InitializeStateForceReset();
				} catch (OutOfMemoryError oom) {
					final String message = "Unity Ads SDK failed to initialize due to application doesn't have enough memory to initialize Unity Ads SDK";
					DeviceLog.exception(message, new Exception(oom));
					Utilities.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							SdkProperties.notifyInitializationFailed(UnityAds.UnityAdsInitializationError.INTERNAL_ERROR, message);
						}
					});
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
			InitializeEventsMetricSender.getInstance().didInitStart();
			CachedLifecycle.register();
			_thread = new InitializeThread(new InitializeStateLoadConfigFile(configuration));
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


	public static synchronized DownloadLatestWebViewStatus downloadLatestWebView() {
		if (_thread != null) {
			return DownloadLatestWebViewStatus.INIT_QUEUE_NOT_EMPTY;
		}

		if (SdkProperties.getLatestConfiguration() == null) {
			return DownloadLatestWebViewStatus.MISSING_LATEST_CONFIG;
		}

		_thread = new InitializeThread(new InitializeStateCheckForCachedWebViewUpdate(SdkProperties.getLatestConfiguration()));
		_thread.setName("UnityAdsDownloadThread");
		_thread.start();
		return DownloadLatestWebViewStatus.BACKGROUND_DOWNLOAD_STARTED;
	}
	/* STATE CLASSES */

	private abstract static class InitializeState {
		public abstract InitializeState execute();
	}

	public static class InitializeStateLoadConfigFile extends InitializeState {
		private Configuration _configuration;

		public InitializeStateLoadConfigFile(Configuration configuration) {
			_configuration = configuration;
		}

		@Override
		public InitializeState execute() {
			DeviceLog.debug("Unity Ads init: Loading Config File Parameters");
			Configuration localConfig = null;

			File configFile = new File(SdkProperties.getLocalConfigurationFilepath());
			if (!configFile.exists()) {
				return new InitializeStateReset(_configuration);
			}

			// Attempt to overwrite default configuration with local configuration
			try {
				String fileContent = new String(Utilities.readFileBytes(configFile));
				JSONObject loadedJson = new JSONObject(fileContent);
				localConfig = new Configuration(loadedJson);
				_configuration = localConfig;
			} catch (Exception e) {
				DeviceLog.debug("Unity Ads init: Using default configuration parameters");
			} finally {
				return new InitializeStateReset(_configuration);
			}
		}
	}

	public static class InitializeStateReset extends InitializeState {
		private Configuration _configuration;
		private int _resetWebAppTimeout;

		public InitializeStateReset(Configuration configuration) {
			_configuration = configuration;
			_resetWebAppTimeout = configuration.getResetWebappTimeout();
		}

		public Configuration getConfiguration() {
			return _configuration;
		}

		@Override
		public InitializeState execute() {
			DeviceLog.debug("Unity Ads init: starting init");

			final ConditionVariable cv = new ConditionVariable();
			final WebViewApp currentApp = WebViewApp.getCurrentApp();
			boolean success = true;

			if (currentApp != null) {
				currentApp.resetWebViewAppInitialization();

				if (currentApp.getWebView() != null) {
					Utilities.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							currentApp.getWebView().destroy();
							currentApp.setWebView(null);
							cv.open();
						}
					});

					success = cv.block(this._resetWebAppTimeout);
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
			SdkProperties.setInitializeState(SdkProperties.InitializationState.NOT_INITIALIZED);
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

		public static final String InitializeStateInitModuleStateName = "init modules";

		@Override
		public InitializeState execute() {
			for (String moduleName : _configuration.getModuleConfigurationList()) {
				IModuleConfiguration moduleConfiguration = _configuration.getModuleConfiguration(moduleName);
				if (moduleConfiguration != null) {
					if(!moduleConfiguration.initModuleState(_configuration)) {
						return new InitializeStateError(InitializeStateInitModuleStateName, new Exception("Unity Ads config server resolves to loopback address (due to ad blocker?)"), _configuration);
					}
				}
			}

			return new InitializeStateConfig(_configuration);
		}
	}

	public static class InitializeStateConfig extends InitializeState {
		private Configuration _configuration;
		private Configuration _localConfig;
		private int _retries;
		private long _retryDelay;
		private int _maxRetries;
		private double _scalingFactor;
		private InitializeState _nextState;

		public InitializeStateConfig(Configuration localConfiguration) {
			_configuration = new Configuration(SdkProperties.getConfigUrl(), localConfiguration.getExperiments());
			_retries = 0;
			_retryDelay = localConfiguration.getRetryDelay();
			_maxRetries = localConfiguration.getMaxRetries();
			_scalingFactor = localConfiguration.getRetryScalingFactor();
			_localConfig = localConfiguration;
			_nextState = null;
		}

		public Configuration getConfiguration() {
			return _configuration;
		}

		@Override
		public InitializeState execute() {
			DeviceLog.info("Unity Ads init: load configuration from " + SdkProperties.getConfigUrl());
			InitializeState nextState;
			if (_configuration.getExperiments() != null && _configuration.getExperiments().isTwoStageInitializationEnabled()) {
				nextState = executeWithLoader();
			} else {
				nextState = executeLegacy(_configuration);
			}
			return nextState;
		}

		public InitializeState executeLegacy(Configuration configuration) {
			try {
				configuration.makeRequest();
			} catch (Exception e) {
				if (_retries < _maxRetries) {
					_retryDelay *= _scalingFactor;
					_retries++;
					return new InitializeStateRetry(this, _retryDelay);
				}

				return new InitializeStateNetworkError("network config request", e, this, _localConfig);
			}

			if (configuration.getDelayWebViewUpdate()) {
				return new InitializeStateLoadCacheConfigAndWebView(configuration, _localConfig);
			}

			return new InitializeStateLoadCache(configuration);
		}

		public InitializeState executeWithLoader() {
			ConfigurationLoader configurationLoader = new ConfigurationLoader(_configuration);
			final Configuration legacyConfiguration = new Configuration(SdkProperties.getConfigUrl(), new Experiments());
			try {
				configurationLoader.loadConfiguration(new IConfigurationLoaderListener() {
					@Override
					public void onSuccess(Configuration configuration) {
						_configuration = configuration;
						if (_configuration.getDelayWebViewUpdate()) {
							_nextState = new InitializeStateLoadCacheConfigAndWebView(_configuration, _localConfig);
						}
						TokenStorage.setInitToken(_configuration.getUnifiedAuctionToken());
						_configuration.saveToDisk();
						_nextState = new InitializeStateLoadCache(_configuration);
					}

					@Override
					public void onError(String errorMsg) {
						SDKMetrics.getInstance().sendMetric(TSIMetric.newEmergencySwitchOff(_configuration.getMetricTags()));
						_nextState = executeLegacy(legacyConfiguration);
					}
				});
				return _nextState;
			} catch (Exception e) {
				if (_retries < _maxRetries) {
					_retryDelay *= _scalingFactor;
					_retries++;
					return new InitializeStateRetry(this, _retryDelay);
				}

				return new InitializeStateNetworkError("network config request", e, this, _configuration);
			}
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
		private int _retries;
		private long _retryDelay;
		private int _maxRetries;
		private double _scalingFactor;

		public InitializeStateLoadWeb(Configuration configuration) {
			_configuration = configuration;
			_retries = 0;
			_retryDelay = configuration.getRetryDelay();
			_maxRetries = configuration.getMaxRetries();
			_scalingFactor = configuration.getRetryScalingFactor();
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
				return new InitializeStateError("malformed webview request", e, _configuration);
			}

			String webViewData;

			try {
				webViewData = request.makeRequest();
			} catch (Exception e) {
				if (_retries < _maxRetries) {
					_retryDelay *= _scalingFactor;
					_retries++;
					return new InitializeStateRetry(this, _retryDelay);
				}

				return new InitializeStateNetworkError("network webview request", e, this, _configuration);
			}

			String webViewHash = _configuration.getWebViewHash();
			if (webViewHash != null && !Utilities.Sha256(webViewData).equals(webViewHash)) {
				return new InitializeStateError("invalid hash", new Exception("Invalid webViewHash"), _configuration);
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
		public static final String InitializeStateCreateStateName = "create webapp";

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
				return new InitializeStateError(InitializeStateCreateStateName, e, _configuration);
			}

			if (createSuccessFull) {
				return new InitializeStateComplete(_configuration);
			}
			else {
				String errorMessage = "Unity Ads WebApp creation failed";
				if (WebViewApp.getCurrentApp().getWebAppFailureMessage() != null) {
					errorMessage = WebViewApp.getCurrentApp().getWebAppFailureMessage();
				}
				DeviceLog.error(errorMessage);
				return new InitializeStateError(InitializeStateCreateStateName, new Exception(errorMessage), _configuration);
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

			// TODO: Fix _state.replaceAll... with Enum values to ensure future compatibility with tag values - This works for now
			SDKMetrics.getInstance().sendEvent("native_initialization_failed", new HashMap<String, String> (){{
				put("stt", _state.replaceAll(" ", "_"));
			}});
			return null;
		}
	}

	public static class InitializeStateNetworkError extends InitializeStateError implements IConnectivityListener {
		private static int _receivedConnectedEvents;
		private static long _lastConnectedEventTimeMs;

		private String _state;
		private InitializeState _erroredState;
		private ConditionVariable _conditionVariable;
		private long _networkErrorTimeout;
		private int _maximumConnectedEvents;
		private int _connectedEventThreshold;

		public InitializeStateNetworkError(String state, Exception exception, InitializeState errorState, Configuration configuration) {
			super(state, exception, configuration);
			_state = state;
			_receivedConnectedEvents = 0;
			_lastConnectedEventTimeMs = 0;
			_erroredState = errorState;
			_networkErrorTimeout = configuration.getNetworkErrorTimeout();
			_maximumConnectedEvents = configuration.getMaximumConnectedEvents();
			_connectedEventThreshold = configuration.getConnectedEventThreshold();
		}

		@Override
		public InitializeState execute() {
			DeviceLog.error("Unity Ads init: network error, waiting for connection events");

			_conditionVariable = new ConditionVariable();
			ConnectivityMonitor.addListener(this);

			if (_conditionVariable.block(_networkErrorTimeout)) {
				ConnectivityMonitor.removeListener(this);
				return _erroredState;
			}
			else {
				ConnectivityMonitor.removeListener(this);
				return new InitializeStateError(_state, new Exception("No connected events within the timeout!"), _configuration);
			}
		}

		@Override
		public void onConnected() {
			_receivedConnectedEvents++;

			DeviceLog.debug("Unity Ads init got connected event");
			if (shouldHandleConnectedEvent()) {
				_conditionVariable.open();
			}

			if (_receivedConnectedEvents > _maximumConnectedEvents) {
				ConnectivityMonitor.removeListener(this);
			}

			_lastConnectedEventTimeMs = System.currentTimeMillis();
		}

		@Override
		public void onDisconnected() {
			DeviceLog.debug("Unity Ads init got disconnected event");
		}

		private boolean shouldHandleConnectedEvent () {
			if (System.currentTimeMillis() - _lastConnectedEventTimeMs >= _connectedEventThreshold &&
					_receivedConnectedEvents <= _maximumConnectedEvents) {
				return true;
			}

			return false;
		}
	}

	public static class InitializeStateRetry extends InitializeState {
		InitializeState _state;
		long _delay;

		public InitializeStateRetry(InitializeState state, long delay) {
			_state = state;
			_delay = delay;
		}

		@Override
		public InitializeState execute() {
			DeviceLog.debug("Unity Ads init: retrying in " + _delay + " milliseconds");
			try {
				Thread.sleep(_delay);
			} catch(Exception e) {
				DeviceLog.exception("Init retry interrupted", e);
			}

			return _state;
		}
	}

	public static class InitializeStateLoadCacheConfigAndWebView extends InitializeState {
		private Configuration _configuration;
		private Configuration _localConfig;

		public InitializeStateLoadCacheConfigAndWebView(Configuration configuration, Configuration localConfig) {
			_configuration = configuration;
			_localConfig = localConfig;
		}

		public Configuration getConfiguration() {
			return _configuration;
		}

		@Override
		public InitializeState execute() {

			try {
				byte[] localWebViewData = loadCachedFileToByteArray(new File(SdkProperties.getLocalWebViewFile()));
				return new InitializeStateCheckForUpdatedWebView(_configuration, localWebViewData, _localConfig);
			} catch (Exception exception) {
				//If we are unable to load cached webview data, then bail out, clean up whatever is in the cache, and load from the web
			}

			InitializeStateLoadWeb initializeStateLoadWeb = new InitializeStateLoadWeb(_configuration);
			return new InitializeStateCleanCache(_configuration, initializeStateLoadWeb);
		}
	}

	private static byte[] loadCachedFileToByteArray(File fileToLoad) throws IOException {
		if (fileToLoad != null && fileToLoad.exists()) {
			try {
				return Utilities.readFileBytes(fileToLoad);
			} catch (IOException e) {
				throw new IOException("could not read from file");
			}
		}
		throw new IOException("file not found");
	}

	public static class InitializeStateCleanCache extends InitializeState {
		private Configuration _configuration;
		private InitializeState _nextState;

		public InitializeStateCleanCache(Configuration configuration, InitializeState nextState) {
			_configuration = configuration;
			_nextState = nextState;
		}

		public Configuration getConfiguration() {
			return _configuration;
		}

		@Override
		public InitializeState execute() {
			try {
				File localConfig = new File(SdkProperties.getLocalConfigurationFilepath());
				File localWebView = new File(SdkProperties.getLocalWebViewFile());

				localConfig.delete();
				localWebView.delete();
			} catch (Exception exception) {
				DeviceLog.error("Failure trying to clean cache: " + exception.getMessage());
			}

			return _nextState;
		}
	}

	public static class InitializeStateCleanCacheIgnoreError extends InitializeStateCleanCache {
		public InitializeStateCleanCacheIgnoreError(Configuration configuration, InitializeState nextState) {
			super(configuration, nextState);
		}

		@Override
		public InitializeState execute() {
			try {
				InitializeState state = super.execute();
				if (!(state instanceof InitializeStateError)) {
					return state;
				}
			} catch (Exception e) { }
			return null;
		}
	}

	public static class InitializeStateCheckForUpdatedWebView extends InitializeState {
		private Configuration _configuration;
		private byte[] _localWebViewData;
		private Configuration _localWebViewConfiguration;

		public InitializeStateCheckForUpdatedWebView(Configuration configuration, byte[] localWebViewData, Configuration localWebViewConfiguration) {
			_configuration = configuration;
			_localWebViewData = localWebViewData;
			_localWebViewConfiguration = localWebViewConfiguration;
		}

		@Override
		public InitializeState execute() {
			try {
				String localWebViewHash = Utilities.Sha256(_localWebViewData);
				if (!localWebViewHash.equals(_configuration.getWebViewHash())) {
					SdkProperties.setLatestConfiguration(_configuration);
				}

				//Prepare to load the WebView from cache.  We will first see if there is cached config to use to load with our cached webViewData
				//If there is no cached config, or its invalid, we will next attempt to use the downloaded config to load with the cached webViewData
				//If both of those options fail, we will attempt to clean whatever garbage is in the cache and load from web.

				//We also compare the SdkProperties version and the configuration version to ensure that the cached configuration and data are for the expected native sdk version we are running.
				if (!TextUtils.isEmpty(localWebViewHash)) {
					if (_localWebViewConfiguration != null && _localWebViewConfiguration.getWebViewHash() != null && _localWebViewConfiguration.getWebViewHash().equals(localWebViewHash) && SdkProperties.getVersionName().equals(_localWebViewConfiguration.getSdkVersion())) {
						String localWebViewData = new String(_localWebViewData, "UTF-8");
						return new InitializeStateCreate(_localWebViewConfiguration, localWebViewData);
					} else if (_configuration != null && _configuration.getWebViewHash().equals(localWebViewHash)) {
						String localWebViewData = new String(_localWebViewData, "UTF-8");
						return new InitializeStateCreate(_configuration, localWebViewData);
					}
				}
			} catch (Exception exception) {
			}

			InitializeStateLoadWeb initializeStateLoadWeb = new InitializeStateLoadWeb(_configuration);
			return new InitializeStateCleanCache(_configuration, initializeStateLoadWeb);
		}
	}

	public static class InitializeStateDownloadWebView extends InitializeState {
		private Configuration _configuration;
		private int _retries;
		private long _retryDelay;

		public InitializeStateDownloadWebView(Configuration configuration) {
			_configuration = configuration;
			_retries = 0;
			_retryDelay = configuration.getRetryDelay();
		}

		@Override
		public InitializeState execute() {
			DeviceLog.info("Unity Ads init: downloading webapp from " + _configuration.getWebViewUrl());

			WebRequest request;

			try {
				request = new WebRequest(_configuration.getWebViewUrl(), "GET", null);
			}
			catch (Exception e) {
				DeviceLog.exception("Malformed URL", e);
				return null;
			}

			String webViewData;

			try {
				webViewData = request.makeRequest();
			} catch (Exception e) {
				if (_retries < _configuration.getMaxRetries()) {
					_retryDelay *= _configuration.getRetryScalingFactor();
					_retries++;
					return new InitializeStateRetry(this, _retryDelay);
				}

				return null;
			}

			String webViewHash = _configuration.getWebViewHash();
			if (webViewData != null && webViewHash != null && Utilities.Sha256(webViewData).equals(webViewHash)) {
				return new InitializeStateUpdateCache(_configuration, webViewData);
			}

			return null;
		}
	}

	public static class InitializeStateUpdateCache extends InitializeState {
		private Configuration _configuration;
		private String _webViewData;

		public Configuration getConfiguration() {
			return _configuration;
		}

		public InitializeStateUpdateCache(Configuration configuration, String webViewData) {
			_configuration = configuration;
			_webViewData = webViewData;
		}

		@Override
		public InitializeState execute() {
			if(_configuration != null && _webViewData != null) {
				try {
					Utilities.writeFile(new File(SdkProperties.getLocalWebViewFile()), _webViewData);
					Utilities.writeFile(new File(SdkProperties.getLocalConfigurationFilepath()), _configuration.getJSONString());
				} catch (Exception exception) {
					return new InitializeStateCleanCacheIgnoreError(_configuration, null);
				}
			}

			return null;
		}
	}

	public static class InitializeStateCheckForCachedWebViewUpdate extends InitializeState {
		private Configuration _configuration;

		public Configuration getConfiguration() {
			return _configuration;
		}

		public InitializeStateCheckForCachedWebViewUpdate(Configuration configuration) {
			_configuration = configuration;
		}

		@Override
		public InitializeState execute() {
			try {
				byte[] localWebViewData = loadCachedFileToByteArray(new File(SdkProperties.getLocalWebViewFile()));
				String localWebViewHash = Utilities.Sha256(localWebViewData);

				if (localWebViewHash.equals(_configuration.getWebViewHash())) {
					String localWebViewDataString = new String(localWebViewData, "UTF-8");
					return new InitializeStateUpdateCache(_configuration, localWebViewDataString);
				}
			} catch (Exception exception) { }

			return new InitializeStateDownloadWebView(_configuration);
		}
	}
}
