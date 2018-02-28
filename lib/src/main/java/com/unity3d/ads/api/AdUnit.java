package com.unity3d.ads.api;

import android.content.Intent;
import android.util.SparseArray;
import android.util.SparseIntArray;

import com.unity3d.ads.adunit.AdUnitActivity;
import com.unity3d.ads.adunit.AdUnitError;
import com.unity3d.ads.adunit.AdUnitMotionEvent;
import com.unity3d.ads.adunit.AdUnitSoftwareActivity;
import com.unity3d.ads.adunit.AdUnitTransparentActivity;
import com.unity3d.ads.adunit.AdUnitTransparentSoftwareActivity;
import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.misc.Utilities;
import com.unity3d.ads.properties.ClientProperties;
import com.unity3d.ads.webview.bridge.WebViewCallback;
import com.unity3d.ads.webview.bridge.WebViewExposed;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

public class AdUnit {
	private static AdUnitActivity _adUnitActivity;
	private static int _currentActivityId = -1;

	private AdUnit () {
	}

	public static void setAdUnitActivity (AdUnitActivity activity) {
		_adUnitActivity = activity;
	}

	public static AdUnitActivity getAdUnitActivity () {
		return _adUnitActivity;
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
		open(activityId, views, orientation, keyevents, systemUiVisibility, hardwareAcceleration, false, callback);
	}

	@WebViewExposed
	public static void open (Integer activityId, JSONArray views, Integer orientation, JSONArray keyevents, Integer systemUiVisibility, Boolean hardwareAcceleration, Boolean isTransparent, WebViewCallback callback) {
		final Intent intent;

		if(!hardwareAcceleration && isTransparent) {
			DeviceLog.debug("Unity Ads opening new transparent ad unit activity, hardware acceleration disabled");
			intent = new Intent(ClientProperties.getActivity(), AdUnitTransparentSoftwareActivity.class);
		} else if(hardwareAcceleration && !isTransparent) {
			DeviceLog.debug("Unity Ads opening new hardware accelerated ad unit activity");
			intent = new Intent(ClientProperties.getActivity(), AdUnitActivity.class);
		} else if(hardwareAcceleration && isTransparent) {
			DeviceLog.debug("Unity Ads opening new hardware accelerated transparent ad unit activity");
			intent = new Intent(ClientProperties.getActivity(), AdUnitTransparentActivity.class);
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
			callback.error(AdUnitError.ADUNIT_NULL);
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
			callback.error(AdUnitError.ADUNIT_NULL);
		}

	}

	@WebViewExposed
	public static void getViews (final WebViewCallback callback) {
		if (getAdUnitActivity() != null) {
			String[] views = getAdUnitActivity().getViews();
			callback.invoke(new JSONArray(Arrays.asList(views)));
		}
		else {
			callback.error(AdUnitError.ADUNIT_NULL);
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
			callback.error(AdUnitError.ADUNIT_NULL);
		}

	}

	@WebViewExposed
	public static void getOrientation (WebViewCallback callback) {
		if (getAdUnitActivity() != null) {
			callback.invoke(getAdUnitActivity().getRequestedOrientation());
		}
		else {
			callback.error(AdUnitError.ADUNIT_NULL);
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
			callback.error(AdUnitError.ADUNIT_NULL);
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
			callback.error(AdUnitError.ADUNIT_NULL);
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
			callback.error(AdUnitError.ADUNIT_NULL);
		}
	}

	@WebViewExposed
	public static void setViewFrame (final String view, final Integer x, final Integer y, final Integer width, final Integer height, final WebViewCallback callback) {
		Utilities.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (getAdUnitActivity() != null) {
					getAdUnitActivity().setViewFrame(view, x, y, width, height);
				}
			}
		});

		if (getAdUnitActivity() != null) {
			callback.invoke();
		}
		else {
			callback.error(AdUnitError.ADUNIT_NULL);
		}
	}

	@WebViewExposed
	public static void getViewFrame (final String view, final WebViewCallback callback) {
		if (getAdUnitActivity() != null) {
			if (getAdUnitActivity().getViewFrame(view) != null) {
				Map<String, Integer> map = getAdUnitActivity().getViewFrame(view);
				callback.invoke(map.get("x"), map.get("y"), map.get("width"), map.get("height"));
			}
			else {
				callback.error(AdUnitError.UNKNOWN_VIEW);
			}
		}
		else {
			callback.error(AdUnitError.ADUNIT_NULL);
		}
	}

	@WebViewExposed
	public static void startMotionEventCapture (final Integer maxEvents, final WebViewCallback callback) {
		if (getAdUnitActivity() != null) {
			if (getAdUnitActivity().getLayout() != null) {
				getAdUnitActivity().getLayout().startCapture(maxEvents);
				callback.invoke();
			}
			else {
				callback.error(AdUnitError.LAYOUT_NULL);
			}
		}
		else {
			callback.error(AdUnitError.ADUNIT_NULL);
		}
	}

	@WebViewExposed
	public static void endMotionEventCapture (final WebViewCallback callback) {
		if (getAdUnitActivity() != null) {
			if (getAdUnitActivity().getLayout() != null) {
				getAdUnitActivity().getLayout().endCapture();
				callback.invoke();
			}
			else {
				callback.error(AdUnitError.LAYOUT_NULL);
			}
		}
		else {
			callback.error(AdUnitError.ADUNIT_NULL);
		}
	}

	@WebViewExposed
	public static void clearMotionEventCapture (final WebViewCallback callback) {
		if (getAdUnitActivity() != null) {
			if (getAdUnitActivity().getLayout() != null) {
				getAdUnitActivity().getLayout().clearCapture();
				callback.invoke();
			}
			else {
				callback.error(AdUnitError.LAYOUT_NULL);
			}
		}
		else {
			callback.error(AdUnitError.ADUNIT_NULL);
		}
	}

	@WebViewExposed
	public static void getMotionEventCount (final JSONArray eventTypes, final WebViewCallback callback) {
		ArrayList<Integer> requestedEventTypes = new ArrayList<>();

		for (int i = 0; i < eventTypes.length(); i++) {
			try {
				requestedEventTypes.add(eventTypes.getInt(i));
			}
			catch (Exception e) {
				DeviceLog.exception("Error retrieving int from eventTypes", e);
			}
		}

		if (getAdUnitActivity() != null) {
			if (getAdUnitActivity().getLayout() != null) {
				if (getAdUnitActivity().getLayout().getCurrentEventCount() >= getAdUnitActivity().getLayout().getMaxEventCount()) {
					callback.error(AdUnitError.MAX_MOTION_EVENT_COUNT_REACHED);
					return;
				}

				SparseIntArray counts = getAdUnitActivity().getLayout().getEventCount(requestedEventTypes);
				JSONObject retObj = new JSONObject();

				for (int i = 0; i < counts.size(); i++) {
					int key = counts.keyAt(i);
					int value = counts.get(key);

					try {
						retObj.put(Integer.toString(key), value);
					}
					catch (Exception e) {
						DeviceLog.exception("Error building response JSON", e);
					}
				}

				callback.invoke(retObj);
			}
			else {
				callback.error(AdUnitError.LAYOUT_NULL);
			}
		}
		else {
			callback.error(AdUnitError.ADUNIT_NULL);
		}
	}

	@WebViewExposed
	public static void getMotionEventData (final JSONObject infoIndices, final WebViewCallback callback) {
		Iterator<String> infoIterator = infoIndices.keys();
		SparseArray<ArrayList<Integer>> requestedInfos = new SparseArray<>();

		while (infoIterator.hasNext()) {
			String key = infoIterator.next();
			int keyInt = Integer.parseInt(key);
			if (requestedInfos.get(keyInt) == null) {
				requestedInfos.put(keyInt, new ArrayList<Integer>());
			}

			JSONArray keyIndices = null;

			try {
				keyIndices = infoIndices.getJSONArray(key);
			}
			catch (Exception e) {
				DeviceLog.exception("Couldn't fetch keyIndices", e);
			}

			if (keyIndices != null) {
				for (int i = 0; i < keyIndices.length(); i++) {
					try {
						requestedInfos.get(keyInt).add(keyIndices.getInt(i));
					}
					catch (Exception e) {
						DeviceLog.exception("Couldn't add value to requested infos", e);
					}
				}
			}
		}

		if (getAdUnitActivity() != null) {
			if (getAdUnitActivity().getLayout() != null) {
				if (getAdUnitActivity().getLayout().getCurrentEventCount() >= getAdUnitActivity().getLayout().getMaxEventCount()) {
					callback.error(AdUnitError.MAX_MOTION_EVENT_COUNT_REACHED);
					return;
				}

				SparseArray<SparseArray<AdUnitMotionEvent>> eventInfos = getAdUnitActivity().getLayout().getEvents(requestedInfos);

				JSONObject infoObj = new JSONObject();

				for (int eventTypeIndex = 0; eventTypeIndex < eventInfos.size(); eventTypeIndex++) {
					int key = eventInfos.keyAt(eventTypeIndex);
					SparseArray<AdUnitMotionEvent> values = eventInfos.get(key);

					JSONObject typeObj = new JSONObject();

					for (int motionEventIndex = 0; motionEventIndex < values.size(); motionEventIndex++) {
						JSONObject eventObj = new JSONObject();
						int eventKey = values.keyAt(motionEventIndex);
						AdUnitMotionEvent motionEvent = values.get(eventKey);
						try {
							eventObj.put("action", motionEvent.getAction());
							eventObj.put("isObscured", motionEvent.isObscured());
							eventObj.put("toolType", motionEvent.getToolType());
							eventObj.put("source", motionEvent.getSource());
							eventObj.put("deviceId", motionEvent.getDeviceId());
							eventObj.put("x" , motionEvent.getX());
							eventObj.put("y", motionEvent.getY());
							eventObj.put("eventTime", motionEvent.getEventTime());
							eventObj.put("pressure", motionEvent.getPressure());
							eventObj.put("size", motionEvent.getSize());

							typeObj.put(Integer.toString(eventKey), eventObj);
						}
						catch (Exception e) {
							DeviceLog.debug("Couldn't construct event info", e);
						}
					}

					try {
						infoObj.put(Integer.toString(key), typeObj);
					}
					catch (Exception e) {
						DeviceLog.debug("Couldn't construct info object", e);
					}
				}

				callback.invoke(infoObj);
			}
			else {
				callback.error(AdUnitError.LAYOUT_NULL);
			}
		}
		else {
			callback.error(AdUnitError.ADUNIT_NULL);
		}
	}

	@WebViewExposed
	public static void getCurrentMotionEventCount (final WebViewCallback callback) {
		if (getAdUnitActivity() != null) {
			if (getAdUnitActivity().getLayout() != null) {
				callback.invoke(getAdUnitActivity().getLayout().getCurrentEventCount());
			}
			else {
				callback.error(AdUnitError.LAYOUT_NULL);
			}
		}
		else {
			callback.error(AdUnitError.ADUNIT_NULL);
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
