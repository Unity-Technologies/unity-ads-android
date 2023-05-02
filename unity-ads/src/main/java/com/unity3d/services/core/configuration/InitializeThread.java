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
import com.unity3d.services.core.device.reader.DeviceInfoDataFactory;
import com.unity3d.services.core.device.reader.IDeviceInfoDataContainer;
import com.unity3d.services.core.lifecycle.CachedLifecycle;
import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.network.core.HttpClient;
import com.unity3d.services.core.network.model.HttpRequest;
import com.unity3d.services.core.network.model.HttpResponse;
import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.services.core.properties.SdkProperties;
import com.unity3d.services.core.request.metrics.Metric;
import com.unity3d.services.core.request.metrics.SDKMetrics;
import com.unity3d.services.core.request.metrics.SDKMetricsSender;
import com.unity3d.services.core.request.metrics.TSIMetric;
import com.unity3d.services.core.webview.WebView;
import com.unity3d.services.core.webview.WebViewApp;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class InitializeThread extends Thread  {
	private static InitializeThread _thread;
	private InitializeState _state;
	private String _stateName;
	private boolean _stopThread = false;
	private boolean _didRetry = false;
	private long _stateStartTimestamp;
	private final SDKMetricsSender _sdkMetricsSender = Utilities.getService(SDKMetricsSender.class);

	private InitializeThread(InitializeState state) {
		super();
		_state = state;
	}

	@Override
	public void run() {
		try {
			while (_state != null && !_stopThread) {
				try {
					handleStateStartMetrics(_state);
					_state = _state.execute();
					handleStateEndMetrics(_state);
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

	private void handleStateStartMetrics(InitializeState state) {
		if (isRetryState(state)) {
			_didRetry = true;
		} else {
			if (!_didRetry) {
				_stateStartTimestamp = System.nanoTime();
			}
			_didRetry = false;
		}
		_stateName = getMetricNameForState(state);
	}

	private void handleStateEndMetrics(InitializeState nextState) {
		if (_stateName == null || isRetryState(nextState) || _stateName.equals("native_retry_state")) return;
		long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - _stateStartTimestamp);
		_sdkMetricsSender.sendMetric(new Metric(_stateName, duration, getMetricTagsForState()));
	}

	private Map<String, String> getMetricTagsForState() {
		return InitializeEventsMetricSender.getInstance().getRetryTags();
	}

	private String getMetricNameForState(InitializeState state) {
		if (state == null) return null;
		final String nativePrefix = "native_";
		final String statePostfix = "_state";
		String className = state.getClass().getSimpleName();
		if (className.length() == 0) return null;
		className = className.substring(getStatePrefixLength()).toLowerCase(); // remove InitializeState prefix
		return new StringBuilder(nativePrefix.length() + className.length() + statePostfix.length()).append(nativePrefix).append(className).append(statePostfix).toString();
	}

	private int getStatePrefixLength() {
		final String initStatePrefix = "InitializeState";
		return initStatePrefix.length();
	}

	private boolean isRetryState(InitializeState state) {
		return state instanceof InitializeStateRetry;
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
			}
			return new InitializeStateReset(_configuration);
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
							WebView currentWebView = currentApp.getWebView();
							if (currentWebView != null) {
								currentWebView.destroy();
								currentApp.setWebView(null);
							}
							cv.open();
						}
					});

					success = cv.block(this._resetWebAppTimeout);
				}

				if (!success) {
					return new InitializeThread.InitializeStateError(ErrorState.ResetWebApp, new Exception("Reset failed on opening ConditionVariable"), _configuration);
				}
			}

			if (Build.VERSION.SDK_INT > 13) {
				unregisterLifecycleCallbacks();
			}

			SdkProperties.setCacheDirectory(null);
			File cacheDir = SdkProperties.getCacheDirectory();
			if (cacheDir == null) {
				return new InitializeThread.InitializeStateError(ErrorState.ResetWebApp, new Exception("Cache directory is NULL"), _configuration);
			}

			SdkProperties.setInitialized(false);

			for (Class moduleClass : _configuration.getModuleConfigurationList()) {
				IModuleConfiguration moduleConfiguration = _configuration.getModuleConfiguration(moduleClass);
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

		@Override
		public InitializeState execute() {
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
			_configuration = new Configuration(SdkProperties.getConfigUrl(), localConfiguration.getExperimentsReader());
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
			return executeWithLoader();
		}

		public InitializeState executeLegacy(Configuration configuration) {
			try {
				configuration.makeRequest();
			} catch (Exception e) {
				if (_retries < _maxRetries) {
					_retryDelay *= _scalingFactor;
					_retries++;
					InitializeEventsMetricSender.getInstance().onRetryConfig();
					return new InitializeStateRetry(this, _retryDelay);
				}

				return new InitializeStateNetworkError(ErrorState.NetworkConfigRequest, e, this, _localConfig);
			}

			if (configuration.getDelayWebViewUpdate()) {
				return new InitializeStateLoadCacheConfigAndWebView(configuration, _localConfig);
			}
			boolean isNativeWebViewCache = configuration.getExperiments().isNativeWebViewCacheEnabled();
			_nextState = isNativeWebViewCache ? new InitializeStateCreateWithRemote(configuration) : new InitializeStateLoadCache(configuration);
			return _nextState;
		}

		public InitializeState executeWithLoader() {
			PrivacyConfigStorage privacyConfigStorage = PrivacyConfigStorage.getInstance();
			DeviceInfoDataFactory deviceInfoDataFactory = new DeviceInfoDataFactory();
			IDeviceInfoDataContainer infoReaderCompressor = deviceInfoDataFactory.getDeviceInfoData(InitRequestType.TOKEN);
			SDKMetricsSender sdkMetricsSender = Utilities.getService(SDKMetricsSender.class);
			IConfigurationLoader configurationLoader = new ConfigurationLoader(new ConfigurationRequestFactory(_configuration, infoReaderCompressor), sdkMetricsSender);
			IDeviceInfoDataContainer privacyInfoReaderCompressor = deviceInfoDataFactory.getDeviceInfoData(InitRequestType.PRIVACY);
			configurationLoader = new PrivacyConfigurationLoader(configurationLoader, new ConfigurationRequestFactory(_configuration, privacyInfoReaderCompressor), privacyConfigStorage);
			final Configuration legacyConfiguration = new Configuration(SdkProperties.getConfigUrl());
			try {
				configurationLoader.loadConfiguration(new IConfigurationLoaderListener() {
					@Override
					public void onSuccess(Configuration configuration) {
						_configuration = configuration;
						_configuration.saveToDisk();
						if (_configuration.getDelayWebViewUpdate()) {
							_nextState = new InitializeStateLoadCacheConfigAndWebView(_configuration, _localConfig);
						}
						((TokenStorage)Utilities.getService(TokenStorage.class)).setInitToken(_configuration.getUnifiedAuctionToken());
						boolean isNativeWebViewCache = _configuration.getExperiments().isNativeWebViewCacheEnabled();
						_nextState = isNativeWebViewCache ? new InitializeStateCreateWithRemote(_configuration) : new InitializeStateLoadCache(_configuration);
					}

					@Override
					public void onError(String errorMsg) {
						sdkMetricsSender.sendMetric(TSIMetric.newEmergencySwitchOff());
						_nextState = executeLegacy(legacyConfiguration);
					}
				});
				return _nextState;
			} catch (Exception e) {
				if (_retries < _maxRetries) {
					_retryDelay *= _scalingFactor;
					_retries++;
					InitializeEventsMetricSender.getInstance().onRetryConfig();
					return new InitializeStateRetry(this, _retryDelay);
				}

				return new InitializeStateNetworkError(ErrorState.NetworkConfigRequest, e, this, _configuration);
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
					return new InitializeStateError(ErrorState.LoadCache, e, _configuration);
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
		private HttpClient _httpClient = Utilities.getService(HttpClient.class);

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

			HttpRequest request;

			try {
				request = new HttpRequest(_configuration.getWebViewUrl());
			}
			catch (Exception e) {
				DeviceLog.exception("Malformed URL", e);
				return new InitializeStateError(ErrorState.MalformedWebviewRequest, e, _configuration);
			}

			String webViewData;

			try {
				HttpResponse response = _httpClient.executeBlocking(request);
				webViewData = response.getBody().toString();
			} catch (Exception e) {
				if (_retries < _maxRetries) {
					_retryDelay *= _scalingFactor;
					_retries++;
					InitializeEventsMetricSender.getInstance().onRetryWebview();
					return new InitializeStateRetry(this, _retryDelay);
				}

				return new InitializeStateNetworkError(ErrorState.NetworkWebviewRequest, e, this, _configuration);
			}

			String webViewHash = _configuration.getWebViewHash();
			if (webViewHash != null && !Utilities.Sha256(webViewData).equals(webViewHash)) {
				return new InitializeStateError(ErrorState.InvalidHash, new Exception("Invalid webViewHash"), _configuration);
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
			ErrorState createErrorState;

			try {
				createErrorState = WebViewApp.create(configuration, false);
			}
			catch (IllegalThreadStateException e) {
				DeviceLog.exception("Illegal Thread", e);
				return new InitializeStateError(ErrorState.CreateWebApp, e, _configuration);
			}

			if (createErrorState == null) {
				return new InitializeStateComplete(_configuration);
			}
			else {
				String errorMessage = "Unity Ads WebApp creation failed";
				if (WebViewApp.getCurrentApp().getWebAppFailureMessage() != null) {
					errorMessage = WebViewApp.getCurrentApp().getWebAppFailureMessage();
				}
				DeviceLog.error(errorMessage);
				return new InitializeStateError(createErrorState, new Exception(errorMessage), _configuration);
			}
		}
	}
	public static class InitializeStateCreateWithRemote extends InitializeState {
		private Configuration _configuration;

		public InitializeStateCreateWithRemote(Configuration configuration) {
			_configuration = configuration;
		}

		public Configuration getConfiguration() {
			return _configuration;
		}

		@Override
		public InitializeState execute() {
			DeviceLog.debug("Unity Ads init: creating webapp");

			final Configuration configuration = _configuration;
			ErrorState createErrorState;

			try {
				createErrorState = WebViewApp.create(configuration, true);
			}
			catch (IllegalThreadStateException e) {
				DeviceLog.exception("Illegal Thread", e);
				return new InitializeStateError(ErrorState.CreateWebApp, e, _configuration);
			}

			if (createErrorState == null) {
				return new InitializeStateComplete(_configuration);
			}
			else {
				String errorMessage = "Unity Ads WebApp creation failed";
				if (WebViewApp.getCurrentApp().getWebAppFailureMessage() != null) {
					errorMessage = WebViewApp.getCurrentApp().getWebAppFailureMessage();
				}
				DeviceLog.error(errorMessage);
				return new InitializeStateError(createErrorState, new Exception(errorMessage), _configuration);
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
			for (Class moduleClass : _configuration.getModuleConfigurationList()) {
				IModuleConfiguration moduleConfiguration = _configuration.getModuleConfiguration(moduleClass);
				if (moduleConfiguration != null) {
					moduleConfiguration.initCompleteState(_configuration);
				}
			}

			return null;
		}
	}

	public static class InitializeStateError extends InitializeState {
		ErrorState _errorState;
		Exception _exception;
		protected Configuration _configuration;

		public InitializeStateError(ErrorState errorState, Exception exception, Configuration configuration) {
			_errorState = errorState;
			_exception = exception;
			_configuration = configuration;
		}

		@Override
		public InitializeState execute() {
			DeviceLog.error("Unity Ads init: halting init in " + _errorState.getMetricName() + ": " + _exception.getMessage());

			for (Class moduleClass : _configuration.getModuleConfigurationList()) {
				IModuleConfiguration moduleConfiguration = _configuration.getModuleConfiguration(moduleClass);
				if (moduleConfiguration != null) {
					moduleConfiguration.initErrorState(_configuration, _errorState, _exception.getMessage());
				}
			}

			return null;
		}
	}

	public static class InitializeStateNetworkError extends InitializeStateError implements IConnectivityListener {
		private static int _receivedConnectedEvents;
		private static long _lastConnectedEventTimeMs;

		private ErrorState _state;
		private InitializeState _erroredState;
		private ConditionVariable _conditionVariable;
		private long _networkErrorTimeout;
		private int _maximumConnectedEvents;
		private int _connectedEventThreshold;

		public InitializeStateNetworkError(ErrorState state, Exception exception, InitializeState errorState, Configuration configuration) {
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
				Thread.currentThread().interrupt();
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
		private HttpClient _httpClient = Utilities.getService(HttpClient.class);

		public InitializeStateDownloadWebView(Configuration configuration) {
			_configuration = configuration;
			_retries = 0;
			_retryDelay = configuration.getRetryDelay();
		}

		@Override
		public InitializeState execute() {
			DeviceLog.info("Unity Ads init: downloading webapp from " + _configuration.getWebViewUrl());

			HttpRequest request;

			try {
				request = new HttpRequest(_configuration.getWebViewUrl());
			}
			catch (Exception e) {
				DeviceLog.exception("Malformed URL", e);
				return null;
			}

			String webViewData;

			try {
				HttpResponse response = _httpClient.executeBlocking(request);
				webViewData = response.getBody().toString();
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
					Utilities.writeFile(new File(SdkProperties.getLocalConfigurationFilepath()), _configuration.getFilteredJsonString());
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
