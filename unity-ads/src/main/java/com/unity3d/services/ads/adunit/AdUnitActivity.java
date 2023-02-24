package com.unity3d.services.ads.adunit;

import android.app.Activity;
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

import org.json.JSONArray;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AdUnitActivity extends Activity {

	public static final String EXTRA_VIEWS = "views";
	public static final String EXTRA_ACTIVITY_ID = "activityId";
	public static final String EXTRA_ORIENTATION = "orientation";
	public static final String EXTRA_SYSTEM_UI_VISIBILITY = "systemUiVisibility";
	public static final String EXTRA_KEY_EVENT_LIST = "keyEvents";
	public static final String EXTRA_KEEP_SCREEN_ON = "keepScreenOn";
	public static final String EXTRA_DISPLAY_CUTOUT_MODE = "displayCutoutMode";

	protected AdUnitRelativeLayout _layout;
	private String[] _views;
	private int _orientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
	private int _systemUiVisibility;
	private int _activityId;
	private ArrayList<Integer> _keyEventList;
	boolean _keepScreenOn;
	private Map<String, IAdUnitViewHandler> _viewHandlers;
	private int _displayCutoutMode;

	private final IAdUnitViewHandlerFactory _adUnitViewHandlerFactory = new AdUnitViewHandlerFactory();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// This error condition will trigger if activity is backgrounded while activity is in foreground,
		// app process is killed while app is in background and then app is yet again launched to foreground
		if(WebViewApp.getCurrentApp() == null) {
			DeviceLog.error("Unity Ads web app is null, closing Unity Ads activity from onCreate");
			finish();
			return;
		}

		AdUnit.setAdUnitActivity(this);
		Intent.setActiveActivity(this);

		createLayout();

		ViewUtilities.removeViewFromParent(_layout);
		addContentView(_layout, _layout.getLayoutParams());

		AdUnitEvent event;

		if (savedInstanceState == null) {
			_views = getIntent().getStringArrayExtra(EXTRA_VIEWS);
			_keyEventList = getIntent().getIntegerArrayListExtra(EXTRA_KEY_EVENT_LIST);

			if (getIntent().hasExtra(EXTRA_ORIENTATION)) {
				_orientation = getIntent().getIntExtra(EXTRA_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
			}
			if (getIntent().hasExtra(EXTRA_SYSTEM_UI_VISIBILITY)) {
				_systemUiVisibility = getIntent().getIntExtra(EXTRA_SYSTEM_UI_VISIBILITY, 0);
			}
			if (getIntent().hasExtra(EXTRA_ACTIVITY_ID)) {
				_activityId = getIntent().getIntExtra(EXTRA_ACTIVITY_ID, -1);
			}
			if (getIntent().hasExtra(EXTRA_DISPLAY_CUTOUT_MODE)) {
				_displayCutoutMode = getIntent().getIntExtra(EXTRA_DISPLAY_CUTOUT_MODE, 0);
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
					handler.onCreate(this, savedInstanceState);
				}
			}
		}

		WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.ADUNIT, event, _activityId);
	}

	public AdUnitRelativeLayout getLayout() {
		return _layout;
	}

	@Override
	protected void onStart() {
		super.onStart();

		if(WebViewApp.getCurrentApp() == null) {
			if(!isFinishing()) {
				DeviceLog.error("Unity Ads web app is null, closing Unity Ads activity from onStart");
				finish();
			}
			return;
		}

		if (_viewHandlers != null) {
			for (Map.Entry<String, IAdUnitViewHandler> entry : _viewHandlers.entrySet()) {
				if (entry.getValue() != null) {
					entry.getValue().onStart(this);
				}
			}
		}

		WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.ADUNIT, AdUnitEvent.ON_START, _activityId);
	}

	@Override
	protected void onStop() {
		super.onStop();

		if(WebViewApp.getCurrentApp() == null) {
			if(!isFinishing()) {
				DeviceLog.error("Unity Ads web app is null, closing Unity Ads activity from onStop");
				finish();
			}
			return;
		}

		if (_viewHandlers != null) {
			for (Map.Entry<String, IAdUnitViewHandler> entry : _viewHandlers.entrySet()) {
				if (entry.getValue() != null) {
					entry.getValue().onStop(this);
				}
			}
		}

		WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.ADUNIT, AdUnitEvent.ON_STOP, _activityId);
	}

	@Override
	protected void onResume() {
		super.onResume();

		if(WebViewApp.getCurrentApp() == null) {
			if(!isFinishing()) {
				DeviceLog.error("Unity Ads web app is null, closing Unity Ads activity from onResume");
				finish();
			}
			return;
		}

		setViews(_views);

		if (_viewHandlers != null) {
			for (Map.Entry<String, IAdUnitViewHandler> entry : _viewHandlers.entrySet()) {
				if (entry.getValue() != null) {
					entry.getValue().onResume(this);
				}
			}
		}

		WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.ADUNIT, AdUnitEvent.ON_RESUME, _activityId);
	}

	@Override
	protected void onPause() {
		super.onPause();

		if(WebViewApp.getCurrentApp() == null) {
			if(!isFinishing()) {
				DeviceLog.error("Unity Ads web app is null, closing Unity Ads activity from onPause");
				finish();
			}
			return;
		}

		if (WebViewApp.getCurrentApp().getWebView() == null) {
			DeviceLog.warning("Unity Ads web view is null, from onPause");
		} else if (isFinishing()) {
			ViewUtilities.removeViewFromParent(WebViewApp.getCurrentApp().getWebView());
		}

		if (_viewHandlers != null) {
			for (Map.Entry<String, IAdUnitViewHandler> entry : _viewHandlers.entrySet()) {
				if (entry.getValue() != null) {
					entry.getValue().onPause(this);
				}
			}
		}

		WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.ADUNIT, AdUnitEvent.ON_PAUSE, isFinishing(), _activityId);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(EXTRA_ORIENTATION, _orientation);
		outState.putInt(EXTRA_SYSTEM_UI_VISIBILITY, _systemUiVisibility);
		outState.putIntegerArrayList(EXTRA_KEY_EVENT_LIST, _keyEventList);
		outState.putBoolean(EXTRA_KEEP_SCREEN_ON, _keepScreenOn);
		outState.putStringArray(EXTRA_VIEWS, _views);
		outState.putInt(EXTRA_ACTIVITY_ID, _activityId);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if(WebViewApp.getCurrentApp() == null) {
			if(!isFinishing()) {
				DeviceLog.error("Unity Ads web app is null, closing Unity Ads activity from onDestroy");
				finish();
			}
			return;
		}

		WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.ADUNIT, AdUnitEvent.ON_DESTROY, isFinishing(), _activityId);

		if (_viewHandlers != null) {
			for (Map.Entry<String, IAdUnitViewHandler> entry : _viewHandlers.entrySet()) {
				if (entry.getValue() != null) {
					entry.getValue().onDestroy(this);
				}
			}
		}

		if (AdUnit.getCurrentAdUnitActivityId() == _activityId) {
			AdUnit.setAdUnitActivity(null);
		}

		Intent.removeActiveActivity(this);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (_keyEventList != null) {
			if (_keyEventList.contains(keyCode)) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.ADUNIT, AdUnitEvent.KEY_DOWN, keyCode, event.getEventTime(), event.getDownTime(), event.getRepeatCount(), _activityId);
				return true;
			}
		}

		return false;
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if (hasFocus) {
			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.ADUNIT, AdUnitEvent.ON_FOCUS_GAINED, _activityId);
		} else {
			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.ADUNIT, AdUnitEvent.ON_FOCUS_LOST, _activityId);
		}
		super.onWindowFocusChanged(hasFocus);
	}

	@Override
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

			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.PERMISSIONS, PermissionsEvent.PERMISSIONS_RESULT, requestCode, permissionsArray, grantResultsArray);
		}
		catch (Exception e) {
			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.PERMISSIONS, PermissionsEvent.PERMISSIONS_ERROR, e.getMessage());
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
			handler.create(this);
			if (!handleViewPlacement(handler.getView())) {
				return;
			}
		}
	}

	private boolean handleViewPlacement (View view) {
		if (view == null) {
			finish();
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
		setRequestedOrientation(orientation);
	}

	// Returns true if successfully set, false if error
	public boolean setKeepScreenOn(boolean keepScreenOn) {
		_keepScreenOn = keepScreenOn;

		// If activity is non-visual there is no window
		if(getWindow() == null)
			return false;

		if(keepScreenOn) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		} else {
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

		return true;
	}

	public boolean setSystemUiVisibility (int flags) {
		_systemUiVisibility = flags;

		if (Build.VERSION.SDK_INT >= 11) {
			try {
				getWindow().getDecorView().setSystemUiVisibility(flags);
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
			viewHandler = _adUnitViewHandlerFactory.createViewHandler(name);

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
		if (Build.VERSION.SDK_INT >= 28 && getWindow() != null) {
			WindowManager.LayoutParams lp = getWindow().getAttributes();
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

		_layout = new AdUnitRelativeLayout(this);
		_layout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		ViewUtilities.setBackground(_layout, new ColorDrawable(Color.BLACK));
	}
}
