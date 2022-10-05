package com.unity3d.services.core.device.reader;

public class JsonStorageKeyNames {
	public static final String UNIFIED_CONFIG_KEY = "unifiedconfig";
	public static final String UNIFIED_CONFIG_PII_KEY = "unifiedconfig.pii";
	public static final String ADVERTISING_TRACKING_ID_KEY = "advertisingTrackingId";
	public static final String ADVERTISING_TRACKING_ID_NORMALIZED_KEY = UNIFIED_CONFIG_PII_KEY + "." + ADVERTISING_TRACKING_ID_KEY;
	public static final String DATA_KEY = "data";
	public static final String GAME_SESSION_ID_KEY = "gameSessionId";
	public static final String GAME_SESSION_ID_NORMALIZED_KEY = UNIFIED_CONFIG_KEY + "." + DATA_KEY + "." + GAME_SESSION_ID_KEY;
	public static final String PRIVACY_SPM_KEY = "privacy.spm.value";
	public static final String PRIVACY_MODE_KEY = "privacy.mode.value";
	public static final String USER_NON_BEHAVIORAL_KEY = "user.nonBehavioral";
	public static final String USER_NON_BEHAVIORAL_VALUE_KEY = "user.nonbehavioral.value";
	public static final String USER_NON_BEHAVIORAL_VALUE_ALT_KEY = "user.nonBehavioral.value";
}
