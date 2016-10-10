package com.wds.ads.api;

import android.content.Intent;
import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.wds.ads.adunit.AdUnitActivity;
import com.wds.ads.adunit.AdUnitError;
import com.wds.ads.adunit.AdUnitSoftwareActivity;
import com.wds.ads.log.DeviceLog;
import com.wds.ads.misc.Utilities;
import com.wds.ads.properties.ClientProperties;
import com.wds.ads.webview.WebViewApp;
import com.wds.ads.webview.bridge.WebViewCallback;
import com.wds.ads.webview.bridge.WebViewExposed;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;

public class AdUnit {
	private static AdUnitActivity _adUnitActivity;
	private static int _currentActivityId = -1;

	private AdUnit () {
	}

	public static AdUnitActivity getAdUnitActivity () {
		return _adUnitActivity;
	}

	public static void setAdUnitActivity(AdUnitActivity activity) {
		_adUnitActivity = activity;
	}

	public static int getCurrentAdUnitActivityId () {
		return _currentActivityId;
	}

	public static void setCurrentAdUnitActivityId (int activityId) {
		_currentActivityId = activityId;
	}

	@WebViewExposed
	public static void open (Integer activityId, JSONArray views, Integer orientation, WebViewCallback callback) {
		open(activityId, views, orientation, null, callback);
	}

	@WebViewExposed
	public static void open (Integer activityId, JSONArray views, Integer orientation, JSONArray keyevents, WebViewCallback callback) {
		open(activityId, views, orientation, keyevents, 0, true, callback);
	}

	@WebViewExposed
	public static void open (Integer activityId, JSONArray views, Integer orientation, JSONArray keyevents, Integer systemUiVisibility, Boolean hardwareAcceleration, WebViewCallback callback) {
		final Intent intent;

		if(hardwareAcceleration) {
			DeviceLog.debug("Unity Ads opening new hardware accelerated ad unit activity");
			intent = new Intent(ClientProperties.getActivity(), AdUnitActivity.class);
		} else {
			DeviceLog.debug("Unity Ads opening new ad unit activity, hardware acceleration disabled");
			intent = new Intent(ClientProperties.getActivity(), AdUnitSoftwareActivity.class);
		}

		int flags = Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_NEW_TASK;
		intent.addFlags(flags);

		if (activityId != null) {
			try {
				intent.putExtra(AdUnitActivity.EXTRA_ACTIVITY_ID, activityId.intValue());
			}
			catch (Exception e) {
				DeviceLog.exception("Could not set activityId for intent", e);
				callback.error(AdUnitError.ACTIVITY_ID, activityId.intValue(), e.getMessage());
				return;
			}

			setCurrentAdUnitActivityId(activityId.intValue());
		}
		else {
			DeviceLog.error("Activity ID is NULL");
			callback.error(AdUnitError.ACTIVITY_ID, "Activity ID NULL");
			return;
		}

		try {
			intent.putExtra(AdUnitActivity.EXTRA_VIEWS, getViewList(views));
		}
		catch (Exception e) {
			DeviceLog.exception("Error parsing views from viewList", e);
			callback.error(AdUnitError.CORRUPTED_VIEWLIST, views, e.getMessage());
			return;
		}

		if (keyevents != null) {
			try {
				intent.putExtra(AdUnitActivity.EXTRA_KEY_EVENT_LIST, getKeyEventList(keyevents));
			}
			catch (Exception e) {
				DeviceLog.exception("Error parsing views from viewList", e);
				callback.error(AdUnitError.CORRUPTED_KEYEVENTLIST, keyevents, e.getMessage());
				return;
			}
		}

		intent.putExtra(AdUnitActivity.EXTRA_SYSTEM_UI_VISIBILITY, systemUiVisibility);
		intent.putExtra(AdUnitActivity.EXTRA_ORIENTATION, orientation);
		ClientProperties.getActivity().startActivity(intent);
		DeviceLog.debug("Opened AdUnitActivity with: " + views.toString());
		callback.invoke();
	}

	@WebViewExposed
	public static void close (WebViewCallback callback) {
		if (getAdUnitActivity() != null) {
			getAdUnitActivity().finish();
			callback.invoke();
		}
		else {
			callback.error(AdUnitError.ACTIVITY_NULL);
		}
	}

	@WebViewExposed
	public static void setViews (final JSONArray views, final WebViewCallback callback) {
		final String[] viewList;
		boolean corrupted = false;
		try {
			viewList = getViewList(views);
		}
		catch (JSONException e) {
			callback.error(AdUnitError.CORRUPTED_VIEWLIST, views);
			corrupted = true;
		}

		if (!corrupted) {
			Utilities.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (getAdUnitActivity() != null) {
						try {
							getAdUnitActivity().setViews(getViewList(views));
						} catch (Exception e) {
							DeviceLog.exception("Corrupted viewlist", e);
						}
					}
				}
			});
		}

		if (getAdUnitActivity() != null) {
				callback.invoke(views);
		}
		else {
			callback.error(AdUnitError.ACTIVITY_NULL);
		}

	}

	@WebViewExposed
	public static void getViews (final WebViewCallback callback) {
		if (getAdUnitActivity() != null) {
			String[] views = getAdUnitActivity().getViews();
			callback.invoke(new JSONArray(Arrays.asList(views)));
		}
		else {
			callback.error(AdUnitError.ACTIVITY_NULL);
		}
	}

	@WebViewExposed
	public static void setOrientation (final Integer orientation, final WebViewCallback callback) {
		Utilities.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (getAdUnitActivity() != null) {
					getAdUnitActivity().setOrientation(orientation);
				}
			}
		});

		if (getAdUnitActivity() != null) {
			callback.invoke(orientation);
		}
		else {
			callback.error(AdUnitError.ACTIVITY_NULL);
		}

	}

	@WebViewExposed
	public static void getOrientation (WebViewCallback callback) {
		if (getAdUnitActivity() != null) {
			callback.invoke(getAdUnitActivity().getRequestedOrientation());
		}
		else {
			callback.error(AdUnitError.ACTIVITY_NULL);
		}
	}

	@WebViewExposed
	public static void setKeepScreenOn(final Boolean screenOn, final WebViewCallback callback) {
		Utilities.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if(getAdUnitActivity() != null) {
					boolean result = getAdUnitActivity().setKeepScreenOn(screenOn);
				}
			}
		});

		if(getAdUnitActivity() != null) {
			callback.invoke();
		} else {
			callback.error(AdUnitError.ACTIVITY_NULL);
		}

	}

	@WebViewExposed
	public static void setSystemUiVisibility (final Integer systemUiVisibility, final WebViewCallback callback) {
		Utilities.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (getAdUnitActivity() != null) {
					getAdUnitActivity().setSystemUiVisibility(systemUiVisibility);
				}
			}
		});

		if (getAdUnitActivity() != null) {
			callback.invoke(systemUiVisibility);
		}
		else {
			callback.error(AdUnitError.ACTIVITY_NULL);
		}

	}

	@WebViewExposed
	public static void setKeyEventList (final JSONArray keyevents, final WebViewCallback callback) {
		if (getAdUnitActivity() != null) {
			try {
				getAdUnitActivity().setKeyEventList(getKeyEventList(keyevents));
				callback.invoke(keyevents);
			}
			catch (Exception e) {
				DeviceLog.exception("Error parsing views from viewList", e);
				callback.error(AdUnitError.CORRUPTED_KEYEVENTLIST, keyevents, e.getMessage());
			}
		}
		else {
			callback.error(AdUnitError.ACTIVITY_NULL);
		}
	}

	private static String[] getViewList (JSONArray views) throws JSONException {
		String[] viewList = new String[views.length()];
		for (int viewidx = 0; viewidx < views.length(); viewidx++) {
			viewList[viewidx] = views.getString(viewidx);
		}

		return viewList;
	}

	private static ArrayList<Integer> getKeyEventList (JSONArray keyevents) throws JSONException {
		ArrayList<Integer> keyEvents = new ArrayList<>();
		for (Integer idx = 0; idx < keyevents.length(); idx++) {
			keyEvents.add(keyevents.getInt(idx));
		}

		return keyEvents;
	}
}