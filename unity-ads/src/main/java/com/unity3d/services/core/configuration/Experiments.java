package com.unity3d.services.core.configuration;

import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Experiments extends ExperimentsBase {

	private static final Set<String> NEXT_SESSION_FLAGS = new HashSet<>(Collections.singletonList("tsi_prw"));

	private final JSONObject _experimentData;

	public Experiments() {
		this(null);
	}

	public Experiments(JSONObject experimentData) {
		if (experimentData == null) {
			_experimentData = new JSONObject();
		} else {
			_experimentData = experimentData;
		}
	}

	@Override
	public boolean shouldNativeTokenAwaitPrivacy() {
		return _experimentData.optBoolean(TSI_TAG_NATIVE_TOKEN_AWAIT_PRIVACY, false);
	}

	@Override
	public boolean isNativeWebViewCacheEnabled() {
		return _experimentData.optBoolean(EXP_TAG_NATIVE_WEBVIEW_CACHE, false);
	}

	@Override
	public boolean isWebAssetAdCaching() {
		return _experimentData.optBoolean(EXP_TAG_WEB_AD_ASSET_CACHING, false);
	}

	@Override
	public boolean isWebGestureNotRequired() {
		return _experimentData.optBoolean(EXP_TAG_WEB_GESTURE_NOT_REQUIRED, false);
	}

	@Override
	public boolean isScarInitEnabled() {
		return _experimentData.optBoolean(EXP_TAG_SCAR_INIT, false);
	}

	@Override
	public boolean isScarBannerHbEnabled() {
		return _experimentData.optBoolean(EXP_TAG_SCAR_HB_BN, false);
	}

	@Override
	public boolean isJetpackLifecycle() {
		return _experimentData.optBoolean(EXP_TAG_JETPACK_LIFECYCLE, false);
	}

	@Override
	public boolean isOkHttpEnabled() {
		return _experimentData.optBoolean(EXP_TAG_OK_HTTP, false);
  }
  
    @Override
	public boolean isWebMessageEnabled() {
		return _experimentData.optBoolean(EXP_TAG_WEB_MESSAGE, false);
	}

	@Override
	public boolean isWebViewAsyncDownloadEnabled() {
		return _experimentData.optBoolean(EXP_TAG_WEBVIEW_ASYNC_DOWNLOAD, false);
	}

	@Override
	public boolean isCronetCheckEnabled() {
		return _experimentData.optBoolean(EXP_TAG_CRONET_CHECK, false);
	}

	@Override
	public boolean isNativeShowTimeoutDisabled() {
		return _experimentData.optBoolean(EXP_TAG_SHOW_TIMEOUT_DISABLED, false);
	}

	@Override
	public boolean isNativeLoadTimeoutDisabled() {
		return _experimentData.optBoolean(EXP_TAG_LOAD_TIMEOUT_DISABLED, false);
	}

	@Override
	public boolean isCaptureHDRCapabilitiesEnabled() {
		return _experimentData.optBoolean(EXP_TAG_HDR_CAPABILITIES, false);
	}

	@Override
	public boolean isPCCheckEnabled() {
		return _experimentData.optBoolean(EXP_TAG_IS_PC_CHECK_ENABLED, false);
	}

	public JSONObject getExperimentsAsJson() {
		return _experimentData;
	}

	public Map<String, String> getExperimentTags() {
		Map<String, String> map = new HashMap<>();
		for (Iterator<String> keyItor = _experimentData.keys(); keyItor.hasNext(); ) {
			String key = keyItor.next();
			map.put(key, String.valueOf(_experimentData.opt(key)));
		}
		return map;
	}

	@Override
	public JSONObject getNextSessionExperiments() {
		if (_experimentData == null) return null;
		Map<String, String> nextSessionFlags = new HashMap<>();
		for (Iterator<String> it = _experimentData.keys(); it.hasNext();) {
			String currentKey = it.next();
			if (NEXT_SESSION_FLAGS.contains(currentKey)) {
				nextSessionFlags.put(currentKey, String.valueOf(_experimentData.optBoolean(currentKey)));
			}
		}
		return new JSONObject(nextSessionFlags);
	}

	@Override
	public JSONObject getCurrentSessionExperiments() {
		if (_experimentData == null) return null;
		Map<String, String> currentSessionFlags = new HashMap<>();
		for (Iterator<String> it = _experimentData.keys(); it.hasNext();) {
			String currentKey = it.next();
			if (!NEXT_SESSION_FLAGS.contains(currentKey)) {
				currentSessionFlags.put(currentKey, String.valueOf(_experimentData.optBoolean(currentKey)));
			}
		}
		return new JSONObject(currentSessionFlags);
	}

}
