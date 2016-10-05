package com.wds.ads.api;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.wds.ads.log.DeviceLog;
import com.wds.ads.properties.ClientProperties;
import com.wds.ads.webview.bridge.WebViewCallback;
import com.wds.ads.webview.bridge.WebViewExposed;

import org.json.JSONArray;
import org.json.JSONObject;

public class Intent {
	@WebViewExposed
	public static void launch (JSONObject intentData, WebViewCallback callback) {
		android.content.Intent intent;

		String className = (String)intentData.opt("className");
		String packageName = (String)intentData.opt("packageName");
		String action = (String)intentData.opt("action");
		String uri = (String)intentData.opt("uri");
		String mimeType = (String)intentData.opt("mimeType");
		JSONArray categories = (JSONArray)intentData.opt("categories");
		Integer flags = (Integer)intentData.opt("flags");
		JSONArray extras = (JSONArray)intentData.opt("extras");

		if (packageName != null && className == null && action == null && mimeType == null) {
			PackageManager pm = ClientProperties.getApplicationContext().getPackageManager();
			intent = pm.getLaunchIntentForPackage(packageName);

			if (intent != null && flags > -1) {
				intent.addFlags(flags);
			}
		}
		else {
			intent = new android.content.Intent();

			if (className != null && packageName != null)
				intent.setClassName(packageName, className);

			if (action != null)
				intent.setAction(action);

			if (uri != null)
				intent.setData(Uri.parse(uri));

			if (mimeType != null)
				intent.setType(mimeType);

			if (flags != null && flags > -1)
				intent.setFlags(flags);

			if (!setCategories(intent, categories))
				callback.error(IntentError.COULDNT_PARSE_CATEGORIES, categories);

			if (!setExtras(intent, extras))
				callback.error(IntentError.COULDNT_PARSE_EXTRAS, extras);
		}

		if (intent != null) {
			if (getStartingActivity() != null) {
				getStartingActivity().startActivity(intent);
				callback.invoke();
			}
			else {
				callback.error(IntentError.ACTIVITY_WAS_NULL);
			}
		}
		else {
			callback.error(IntentError.INTENT_WAS_NULL);
		}
	}

	private static boolean setCategories (android.content.Intent intent, JSONArray categories) {
		if (categories != null && categories.length() > 0) {
			for (int i = 0; i < categories.length(); i++) {
				try {
					intent.addCategory(categories.getString(i));
				} catch (Exception e) {
					DeviceLog.exception("Couldn't parse categories for intent", e);
					return false;
				}
			}
		}

		return true;
	}

	private static boolean setExtras (android.content.Intent intent, JSONArray extras) {
		if (extras != null) {
			JSONObject item;
			String key;
			Object value;

			for (int i = 0; i < extras.length(); i++) {
				try {
					item = extras.getJSONObject(i);
					key = item.getString("key");
					value = item.get("value");
				}
				catch (Exception e) {
					DeviceLog.exception("Couldn't parse extras", e);
					return false;
				}

				 if (!setExtra(intent, key, value))
					 return false;
			}
		}

		return true;
	}

	private static boolean setExtra (android.content.Intent intent, String key, Object value) {
		if (value instanceof String) {
			intent.putExtra(key, (String)value);
		}
		else if (value instanceof Integer) {
			intent.putExtra(key, ((Integer)value).intValue());
		}
		else if (value instanceof Double) {
			intent.putExtra(key, ((Double)value).doubleValue());
		}
		else if (value instanceof Boolean) {
			intent.putExtra(key, ((Boolean)value).booleanValue());
		}
		else {
			DeviceLog.error("Unable to parse launch intent extra " + key);
			return false;
		}

		return true;
	}

	private static Activity getStartingActivity () {
		Activity act = null;

		if (AdUnit.getAdUnitActivity() != null) {
			act = AdUnit.getAdUnitActivity();
		}
		else if (ClientProperties.getActivity() != null) {
			act = ClientProperties.getActivity();
		}

		return act;
	}

	public enum IntentError {
		COULDNT_PARSE_EXTRAS,
		COULDNT_PARSE_CATEGORIES,
		INTENT_WAS_NULL,
		ACTIVITY_WAS_NULL
	}
}
