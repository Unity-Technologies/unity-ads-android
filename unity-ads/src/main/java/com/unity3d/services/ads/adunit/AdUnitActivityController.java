package com.unity3d.services.ads.adunit;

import static com.unity3d.services.ads.adunit.AdUnitActivity.*;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.unity3d.services.ads.api.AdUnit;
import com.unity3d.services.core.api.Intent;
import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.misc.ViewUtilities;
import com.unity3d.services.core.webview.WebViewApp;
import com.unity3d.services.core.webview.WebViewEventCategory;
import com.unity3d.services.core.webview.bridge.IEventSender;

import org.json.JSONArray;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AdUnitActivityController {

	protected AdUnitRelativeLayout _layout;
	private String[] _views;
	private int _orientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
	private int _systemUiVisibility;
	private int _activityId;
	private ArrayList<Integer> _keyEventList;
	boolean _keepScreenOn;
	private Map<String, IAdUnitViewHandler> _viewHandlers;
	private int _displayCutoutMode;
	private final IAdUnitActivity _adUnitActivity;

	private final IEventSender _eventSender;
	private final IAdUnitViewHandlerFactory _adUnitViewHandlerFactory;

	public AdUnitActivityController(IAdUnitActivity activity, IEventSender eventSender, IAdUnitViewHandlerFactory adUnitViewHandlerFactory) {
		_adUnitActivity = activity;
		_eventSender = eventSender;
		_adUnitViewHandlerFactory = adUnitViewHandlerFactory;
	}

	public void onCreate(Bundle savedInstanceState) {
		// This error condition will trigger if activity is backgrounded while activity is in foreground,
		// app process is killed while app is in background and then app is yet again launched to foreground
		if(!_eventSender.canSend()) {
			DeviceLog.error("Unity Ads web app is null, closing Unity Ads activity from onCreate");
			_adUnitActivity.finish();
			return;
		}

		AdUnit.setAdUnitActivity(_adUnitActivity);
		Intent.setActiveActivity(_adUnitActivity.getActivity());

		createLayout();

		ViewUtilities.removeViewFromParent(_layout);
		_adUnitActivity.addContentView(_layout, _layout.getLayoutParams());

		AdUnitEvent event;

		if (savedInstanceState == null) {
			_views = _adUnitActivity.getIntent().getStringArrayExtra(EXTRA_VIEWS);
			_keyEventList = _adUnitActivity.getIntent().getIntegerArrayListExtra(EXTRA_KEY_EVENT_LIST);

			if (_adUnitActivity.getIntent().hasExtra(EXTRA_ORIENTATION)) {
				_orientation = _adUnitActivity.getIntent().getIntExtra(EXTRA_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
			}
			if (_adUnitActivity.getIntent().hasExtra(EXTRA_SYSTEM_UI_VISIBILITY)) {
				_systemUiVisibility = _adUnitActivity.getIntent().getIntExtra(EXTRA_SYSTEM_UI_VISIBILITY, 0);
			}
			if (_adUnitActivity.getIntent().hasExtra(EXTRA_ACTIVITY_ID)) {
				_activityId = _adUnitActivity.getIntent().getIntExtra(EXTRA_ACTIVITY_ID, -1);
			}
			if (_adUnitActivity.getIntent().hasExtra(EXTRA_DISPLAY_CUTOUT_MODE)) {
				_displayCutoutMode = _adUnitActivity.getIntent().getIntExtra(EXTRA_DISPLAY_CUTOUT_MODE, 0);
			}

			event = AdUnitEvent.ON_CREATE;
		} else {
			_views = savedInstanceState.getStringArray(EXTRA_VIEWS);
			_orientation = savedInstanceState.getInt(EXTRA_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
			_systemUiVisibility = savedInstanceState.getInt(EXTRA_SYSTEM_UI_VISIBILITY, 0);
			_keyEventList = savedInstanceState.getIntegerArrayList(EXTRA_KEY_EVENT_LIST);
			_keepScreenOn = savedInstanceState.getBoolean(EXTRA_KEEP_SCREEN_ON);
			_activityId = savedInstanceState.getInt(EXTRA_ACTIVITY_ID, -1);
			_displayCutoutMode = savedInstanceState.getInt(EXTRA_DISPLAY_CUTOUT_MODE, 0);
			setKeepScreenOn(_keepScreenOn);
			event = AdUnitEvent.ON_RESTORE;
		}

		setOrientation(_orientation);
		setSystemUiVisibility(_systemUiVisibility);
		setLayoutInDisplayCutoutMode(_displayCutoutMode);

		if(_views != null) {
			for (String viewName : _views) {
				IAdUnitViewHandler handler = getViewHandler(viewName);

				if (handler != null) {
					handler.onCreate(_adUnitActivity, savedInstanceState);
				}
			}
		}

		_eventSender.sendEvent(WebViewEventCategory.ADUNIT, event, _activityId);
	}

	public AdUnitRelativeLayout getLayout() {
		return _layout;
	}

	public void onStart() {
		if(!_eventSender.canSend()) {
			if(!_adUnitActivity.isFinishing()) {
				DeviceLog.error("Unity Ads web app is null, closing Unity Ads activity from onStart");
				_adUnitActivity.finish();
			}
			return;
		}

		if (_viewHandlers != null) {
			for (Map.Entry<String, IAdUnitViewHandler> entry : _viewHandlers.entrySet()) {
				if (entry.getValue() != null) {
					entry.getValue().onStart(_adUnitActivity);
				}
			}
		}

		_eventSender.sendEvent(WebViewEventCategory.ADUNIT, AdUnitEvent.ON_START, _activityId);
	}

	public void onStop() {
		if(!_eventSender.canSend()) {
			if(!_adUnitActivity.isFinishing()) {
				DeviceLog.error("Unity Ads web app is null, closing Unity Ads activity from onStop");
				_adUnitActivity.finish();
			}
			return;
		}

		if (_viewHandlers != null) {
			for (Map.Entry<String, IAdUnitViewHandler> entry : _viewHandlers.entrySet()) {
				if (entry.getValue() != null) {
					entry.getValue().onStop(_adUnitActivity);
				}
			}
		}

		_eventSender.sendEvent(WebViewEventCategory.ADUNIT, AdUnitEvent.ON_STOP, _activityId);
	}

	public void onResume() {
		if(!_eventSender.canSend()) {
			if(!_adUnitActivity.isFinishing()) {
				DeviceLog.error("Unity Ads web app is null, closing Unity Ads activity from onResume");
				_adUnitActivity.finish();
			}
			return;
		}

		setViews(_views);

		if (_viewHandlers != null) {
			for (Map.Entry<String, IAdUnitViewHandler> entry : _viewHandlers.entrySet()) {
				if (entry.getValue() != null) {
					entry.getValue().onResume(_adUnitActivity);
				}
			}
		}

		_eventSender.sendEvent(WebViewEventCategory.ADUNIT, AdUnitEvent.ON_RESUME, _activityId);
	}

	public void onPause() {
		if(!_eventSender.canSend()) {
			if(!_adUnitActivity.isFinishing()) {
				DeviceLog.error("Unity Ads web app is null, closing Unity Ads activity from onPause");
				_adUnitActivity.finish();
			}
			return;
		}

		if (WebViewApp.getCurrentApp().getWebView() == null) {
			DeviceLog.warning("Unity Ads web view is null, from onPause");
		} else if (_adUnitActivity.isFinishing()) {
			ViewUtilities.removeViewFromParent(WebViewApp.getCurrentApp().getWebView());
		}

		if (_viewHandlers != null) {
			for (Map.Entry<String, IAdUnitViewHandler> entry : _viewHandlers.entrySet()) {
				if (entry.getValue() != null) {
					entry.getValue().onPause(_adUnitActivity);
				}
			}
		}

		_eventSender.sendEvent(WebViewEventCategory.ADUNIT, AdUnitEvent.ON_PAUSE, _adUnitActivity.isFinishing(), _activityId);
	}

	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(EXTRA_ORIENTATION, _orientation);
		outState.putInt(EXTRA_SYSTEM_UI_VISIBILITY, _systemUiVisibility);
		outState.putIntegerArrayList(EXTRA_KEY_EVENT_LIST, _keyEventList);
		outState.putBoolean(EXTRA_KEEP_SCREEN_ON, _keepScreenOn);
		outState.putStringArray(EXTRA_VIEWS, _views);
		outState.putInt(EXTRA_ACTIVITY_ID, _activityId);
	}

	public void onDestroy() {
		if(!_eventSender.canSend()) {
			if(!_adUnitActivity.isFinishing()) {
				DeviceLog.error("Unity Ads web app is null, closing Unity Ads activity from onDestroy");
				_adUnitActivity.finish();
			}
			return;
		}

		_eventSender.sendEvent(WebViewEventCategory.ADUNIT, AdUnitEvent.ON_DESTROY, _adUnitActivity.isFinishing(), _activityId);

		if (_viewHandlers != null) {
			for (Map.Entry<String, IAdUnitViewHandler> entry : _viewHandlers.entrySet()) {
				if (entry.getValue() != null) {
					entry.getValue().onDestroy(_adUnitActivity);
				}
			}
		}

		if (AdUnit.getCurrentAdUnitActivityId() == _activityId) {
			AdUnit.setAdUnitActivity(null);
		}

		Intent.removeActiveActivity(_adUnitActivity.getActivity());
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (_keyEventList != null) {
			if (_keyEventList.contains(keyCode)) {
				_eventSender.sendEvent(WebViewEventCategory.ADUNIT, AdUnitEvent.KEY_DOWN, keyCode, event.getEventTime(), event.getDownTime(), event.getRepeatCount(), _activityId);
				return true;
			}
		}

		return false;
	}

	public void onWindowFocusChanged(boolean hasFocus) {
		if (hasFocus) {
			_eventSender.sendEvent(WebViewEventCategory.ADUNIT, AdUnitEvent.ON_FOCUS_GAINED, _activityId);
		} else {
			_eventSender.sendEvent(WebViewEventCategory.ADUNIT, AdUnitEvent.ON_FOCUS_LOST, _activityId);
		}
	}

	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		try {
			JSONArray permissionsArray = new JSONArray();
			JSONArray grantResultsArray = new JSONArray();

			for (String permission : permissions) {
				permissionsArray.put(permission);
			}

			for (int grantResult : grantResults) {
				grantResultsArray.put(grantResult);
			}

			_eventSender.sendEvent(WebViewEventCategory.PERMISSIONS, PermissionsEvent.PERMISSIONS_RESULT, requestCode, permissionsArray, grantResultsArray);
		}
		catch (Exception e) {
			_eventSender.sendEvent(WebViewEventCategory.PERMISSIONS, PermissionsEvent.PERMISSIONS_ERROR, e.getMessage());
		}
	}

	/* API */

	public void setViewFrame (String view, int x, int y, int width, int height) {
		IAdUnitViewHandler handler = getViewHandler(view);
		View targetView = null;

		if (view.equals("adunit")) {
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
			params.setMargins(x, y, 0, 0);
			_layout.setLayoutParams(params);
		}
		else if (handler != null) {
			targetView = handler.getView();
		}

		if (targetView != null) {
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
			params.setMargins(x, y, 0, 0);
			targetView.setLayoutParams(params);
		}
	}

	public Map<String, Integer> getViewFrame (String view) {
		IAdUnitViewHandler handler = getViewHandler(view);
		View targetView = null;

		if (view.equals("adunit")) {
			FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)_layout.getLayoutParams();
			HashMap<String, Integer> map = new HashMap<>();
			map.put("x", params.leftMargin);
			map.put("y", params.topMargin);
			map.put("width", _layout.getWidth());
			map.put("height", _layout.getHeight());
			return map;
		}
		else if (handler != null) {
			targetView = handler.getView();
		}

		if (targetView != null) {
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)targetView.getLayoutParams();
			HashMap<String, Integer> map = new HashMap<>();
			map.put("x", params.leftMargin);
			map.put("y", params.topMargin);
			map.put("width", targetView.getWidth());
			map.put("height", targetView.getHeight());
			return map;
		}

		return null;
	}

	public void setViews (String[] views) {
		String[] actualViews;

		if (views == null)
			actualViews = new String[0];
		else
			actualViews = views;

		ArrayList<String> newViews = new ArrayList<>(Arrays.asList(actualViews));

		if (_views == null) {
			_views = new String[0];
		}

		ArrayList<String> removedViews = new ArrayList<>(Arrays.asList(_views));
		removedViews.removeAll(newViews);

		for (String view : removedViews) {
			IAdUnitViewHandler handler = getViewHandler(view);
			handler.destroy();
		}

		_views = actualViews;

		for (String view : actualViews) {
			if (view == null) {
				continue;
			}

			IAdUnitViewHandler handler = getViewHandler(view);
			handler.create(_adUnitActivity);
			if (!handleViewPlacement(handler.getView())) {
				return;
			}
		}
	}

	private boolean handleViewPlacement (View view) {
		if (view == null) {
			_adUnitActivity.finish();
			DeviceLog.error("Could not place view because it is null, finishing activity");
			return false;
		}

		if (view.getParent() != null && view.getParent().equals(_layout)) {
			_layout.bringChildToFront(view);
		}
		else {
			ViewUtilities.removeViewFromParent(view);
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
			params.addRule(RelativeLayout.CENTER_IN_PARENT);
			params.setMargins(0, 0, 0, 0);
			view.setPadding(0, 0, 0, 0);
			_layout.addView(view, params);
		}

		return true;
	}

	public String[] getViews () {
		return _views;
	}

	public void setOrientation (int orientation) {
		_orientation = orientation;
		_adUnitActivity.setRequestedOrientation(orientation);
	}

	// Returns true if successfully set, false if error
	public boolean setKeepScreenOn(boolean keepScreenOn) {
		_keepScreenOn = keepScreenOn;

		// If activity is non-visual there is no window
		if(_adUnitActivity.getWindow() == null)
			return false;

		if(keepScreenOn) {
			_adUnitActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		} else {
			_adUnitActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

		return true;
	}

	public boolean setSystemUiVisibility (int flags) {
		_systemUiVisibility = flags;

		if (Build.VERSION.SDK_INT >= 11) {
			try {
				_adUnitActivity.getWindow().getDecorView().setSystemUiVisibility(flags);
				return true;
			}
			catch (Exception e) {
				DeviceLog.exception("Error while setting SystemUIVisibility", e);
				return false;
			}
		}

		return false;
	}

	public void setKeyEventList (ArrayList<Integer> keyevents) {
		_keyEventList = keyevents;
	}

	public IAdUnitViewHandler getViewHandler(String name) {
		IAdUnitViewHandler viewHandler;

		if (_viewHandlers != null && _viewHandlers.containsKey(name)) {
			viewHandler = _viewHandlers.get(name);
		}
		else {
			viewHandler = createViewHandler(name);

			if (viewHandler != null) {
				if (_viewHandlers == null) {
					_viewHandlers = new HashMap<>();
				}

				_viewHandlers.put(name, viewHandler);
			}
		}

		return viewHandler;
	}

	public void setLayoutInDisplayCutoutMode(int flags) {
		_displayCutoutMode = flags;

		// LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES also needs system ui flags View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
		if (Build.VERSION.SDK_INT >= 28 && _adUnitActivity.getWindow() != null) {
			WindowManager.LayoutParams lp = _adUnitActivity.getWindow().getAttributes();
			try {
				Field layoutInDisplayCutoutMode = lp.getClass().getField("layoutInDisplayCutoutMode");
				layoutInDisplayCutoutMode.setInt(lp, flags);
			} catch (IllegalAccessException e) {
				DeviceLog.debug("Error setting layoutInDisplayCutoutMode", e);
			} catch (NoSuchFieldException e) {
				DeviceLog.debug("Error getting layoutInDisplayCutoutMode", e);
			}
		}
	}

	/* LAYOUT */

	protected void createLayout () {
		if (_layout != null) {
			return;
		}

		_layout = new AdUnitRelativeLayout(_adUnitActivity.getContext());
		_layout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		ViewUtilities.setBackground(_layout, new ColorDrawable(Color.BLACK));
	}

	private IAdUnitViewHandler createViewHandler(String name) {
		return _adUnitViewHandlerFactory.createViewHandler(name);
	}
}
