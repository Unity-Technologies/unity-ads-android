package com.unity3d.services.core.request.metrics;

import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.configuration.PrivacyConfig;
import com.unity3d.services.core.configuration.PrivacyConfigStorage;
import com.unity3d.services.core.device.Device;
import com.unity3d.services.core.misc.IObserver;
import com.unity3d.services.core.properties.SdkProperties;
import com.unity3d.services.core.device.Storage;
import com.unity3d.services.core.device.StorageManager;
import com.unity3d.services.core.log.DeviceLog;

import java.util.HashMap;
import java.util.Map;

public class MetricCommonTags {

	private static final String METRIC_COMMON_TAG_COUNTRY_ISO = "iso";
	private static final String METRIC_COMMON_TAG_PLATFORM = "plt";
	private static final String METRIC_COMMON_TAG_SDK_VERSION = "sdk";
	private static final String METRIC_COMMON_TAG_SYSTEM_VERSION = "system";
	private static final String METRIC_COMMON_TAG_PRIVACY_MODE = "prvc";
	private static final String METRIC_COMMON_TAG_TEST_MODE = "tm";
	private static final String METRIC_COMMON_TAG_CONFIG_SOURCE = "src";

	private static final String METRIC_COMMON_TAG_MEDIATION_NAME = "m_name";
	private static final String METRIC_COMMON_TAG_MEDIATION_VERSION = "m_ver";
	private static final String METRIC_COMMON_TAG_MEDIATION_ADAPTER = "m_ad_ver";

	public static final String METRIC_COMMON_TAG_PLATFORM_ANDROID = "android";

	private final String _countryISO;
	private final String _platform;
	private final String _sdkVersion;
	private final String _systemVersion;
	private final boolean _testMode;
	private String _mediationName;
	private String _mediationVersion;
	private String _mediationAdapter;
	private String _privacyMode;
	private String _configSrc;
	private Map<String, String> _experiments;

	private final IObserver<PrivacyConfig> _privacyObserver = new IObserver<PrivacyConfig>() {
		@Override
		public void updated(PrivacyConfig config) {
			_privacyMode = config.getPrivacyStatus().toString().toLowerCase();
		}
	};

	public MetricCommonTags() {
		this._countryISO = Device.getNetworkCountryISO();
		this._platform = METRIC_COMMON_TAG_PLATFORM_ANDROID;
		this._sdkVersion = SdkProperties.getVersionName();
		this._systemVersion = Device.getOsVersion();
		this._privacyMode = PrivacyConfigStorage.getInstance().getPrivacyConfig().getPrivacyStatus().toLowerCase();
    	this._testMode = SdkProperties.isTestMode();

		PrivacyConfigStorage.getInstance().registerObserver(_privacyObserver);
	}

	public void updateWithConfig(Configuration config) {
		this._configSrc = config.getSrc();
		if (config.getExperiments() != null) {
			this._experiments = config.getExperiments().getExperimentTags();
		}
	}

	public Map<String, String> asMap() {
		if (mediationIsEmpty()) {
			refreshMediationData();
		}

		Map<String, String> result = new HashMap<>();

		if (_countryISO != null) result.put(METRIC_COMMON_TAG_COUNTRY_ISO, _countryISO);
		if (_platform != null) result.put(METRIC_COMMON_TAG_PLATFORM, _platform);
		if (_countryISO != null) result.put(METRIC_COMMON_TAG_SDK_VERSION, _sdkVersion);
		if (_systemVersion != null) result.put(METRIC_COMMON_TAG_SYSTEM_VERSION, _systemVersion);
    	if (_privacyMode != null) result.put(METRIC_COMMON_TAG_PRIVACY_MODE, _privacyMode);
		if (_configSrc != null) result.put(METRIC_COMMON_TAG_CONFIG_SOURCE, _configSrc);
		if (_mediationName != null) result.put(METRIC_COMMON_TAG_MEDIATION_NAME, _mediationName);
		if (_mediationVersion != null) result.put(METRIC_COMMON_TAG_MEDIATION_VERSION, _mediationVersion);
		if (_mediationAdapter != null) result.put(METRIC_COMMON_TAG_MEDIATION_ADAPTER, _mediationAdapter);
		if (_experiments != null) result.putAll(_experiments);
		result.put(METRIC_COMMON_TAG_TEST_MODE, String.valueOf(_testMode));


		return result;
	}

	public void shutdown() {
		PrivacyConfigStorage.getInstance().unregisterObserver(_privacyObserver);
  }
  
	private void refreshMediationData() {
		try {
			Storage storage = StorageManager.getStorage(StorageManager.StorageType.PUBLIC);
			if (storage != null && storage.initStorage()) {
				_mediationName = (String) storage.get("mediation.name.value");
				_mediationVersion = (String) storage.get("mediation.version.value");
				_mediationAdapter = (String) storage.get("mediation.adapter_version.value");
			}
		} catch (Exception e) {
			DeviceLog.debug("Failed to refreshMediationData: %s", e.getLocalizedMessage());
		}
	}

	private boolean mediationIsEmpty() {
		return _mediationName == null || _mediationName.isEmpty();
	}
}
