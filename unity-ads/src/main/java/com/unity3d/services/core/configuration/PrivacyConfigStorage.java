package com.unity3d.services.core.configuration;

import com.unity3d.services.core.misc.IObserver;
import com.unity3d.services.core.misc.Observable;

public class PrivacyConfigStorage extends Observable<PrivacyConfig> {
	private static PrivacyConfigStorage _instance;
	private PrivacyConfig _privacyConfig;

	private PrivacyConfigStorage() {
		_privacyConfig = new PrivacyConfig();
	}

	public static PrivacyConfigStorage getInstance() {
		if (_instance == null) {
			_instance = new PrivacyConfigStorage();
		}
		return _instance;
	}

	public synchronized PrivacyConfig getPrivacyConfig() {
		return _privacyConfig;
	}

	@Override
	public synchronized void registerObserver(IObserver<PrivacyConfig> observer) {
		super.registerObserver(observer);
		// Trigger this observer if privacy config was previously set
		if (_privacyConfig.getPrivacyStatus() != PrivacyConfigStatus.UNKNOWN) {
			observer.updated(_privacyConfig);
		}
	}

	public synchronized void setPrivacyConfig(PrivacyConfig privacyConfig) {
		_privacyConfig = privacyConfig;
		notifyObservers(privacyConfig);
	}
}
