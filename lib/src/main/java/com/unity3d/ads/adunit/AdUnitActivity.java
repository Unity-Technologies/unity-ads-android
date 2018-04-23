package com.unity3d.ads.adunit;

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

import com.unity3d.ads.api.AdUnit;
import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.misc.ViewUtilities;
import com.unity3d.ads.api.VideoPlayer;
import com.unity3d.ads.video.VideoPlayerView;
import com.unity3d.ads.webplayer.WebPlayer;
import com.unity3d.ads.webview.WebView;
import com.unity3d.ads.webview.WebViewApp;
import com.unity3d.ads.webview.WebViewEventCategory;

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

	private WebPlayer _webPlayer;
	protected AdUnitRelativeLayout _layout;
	private RelativeLayout _videoContainer;
	private String[] _views;
	private int _orientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
	private int _systemUiVisibility;
	private int _activityId;
	private ArrayList<Integer> _keyEventList;
	boolean _keepScreenOn;

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
			event = AdUnitEvent.ON_CREATE;
		} else {
			_views = savedInstanceState.getStringArray(EXTRA_VIEWS);
			_orientation = savedInstanceState.getInt(EXTRA_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
			_systemUiVisibility = savedInstanceState.getInt(EXTRA_SYSTEM_UI_VISIBILITY, 0);
			_keyEventList = savedInstanceState.getIntegerArrayList(EXTRA_KEY_EVENT_LIST);
			_keepScreenOn = savedInstanceState.getBoolean(EXTRA_KEEP_SCREEN_ON);
			_activityId = savedInstanceState.getInt(EXTRA_ACTIVITY_ID, -1);
			setKeepScreenOn(_keepScreenOn);
			event = AdUnitEvent.ON_RESTORE;
		}

		setOrientation(_orientation);
		setSystemUiVisibility(_systemUiVisibility);

		if(_views != null) {
			if(Arrays.asList(_views).contains("videoplayer")) {
				createVideoPlayer();
			}

			if(Arrays.asList(_views).contains("webplayer")) {
				createWebPlayer();
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

		if (isFinishing()) {
			ViewUtilities.removeViewFromParent(WebViewApp.getCurrentApp().getWebView());
		}

		destroyVideoPlayer();
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

		if (AdUnit.getCurrentAdUnitActivityId() == _activityId) {
			AdUnit.setAdUnitActivity(null);
		}
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

	/* API */

	public WebPlayer getWebPlayer() {
		return _webPlayer;
	}

	public void setViewFrame (String view, int x, int y, int width, int height) {
		View targetView = null;

		if (view.equals("adunit")) {
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
			params.setMargins(x, y, 0, 0);
			_layout.setLayoutParams(params);
		}
		else if (view.equals("videoplayer")) {
			targetView = _videoContainer;
		}
		else if (view.equals("webview")) {
			targetView = WebViewApp.getCurrentApp().getWebView();
		} else if (view.equals("webplayer")) {
			targetView = _webPlayer;
		}

		if (targetView != null) {
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
			params.setMargins(x, y, 0, 0);
			targetView.setLayoutParams(params);
		}
	}

	public Map<String, Integer> getViewFrame (String view) {
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
		else if (view.equals("videoplayer")) {
			targetView = _videoContainer;
		}
		else if (view.equals("webview")) {
			targetView = WebViewApp.getCurrentApp().getWebView();
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
			switch (view) {
				case "videoplayer":
					destroyVideoPlayer();
					break;
				case "webview":
					ViewUtilities.removeViewFromParent(WebViewApp.getCurrentApp().getWebView());
					break;
				case "webplayer":
					destroyWebPlayer();
				default:
					break;
			}
		}

		_views = actualViews;

		for (String view : actualViews) {
			if (view == null) {
				continue;
			}
			if (view.equals("videoplayer")) {
				createVideoPlayer();
				handleViewPlacement(_videoContainer);
			}
			else if (view.equals("webview")) {
				if (WebViewApp.getCurrentApp() != null) {
					handleViewPlacement(WebViewApp.getCurrentApp().getWebView());
				}
				else {
					// TODO: Have seen this crashing once when coming from home screen back to the app using the application list. Try to reproduce and fix if possible.
					DeviceLog.error("WebApp IS NULL!");
					throw new NullPointerException();
				}
			}
			else if (view.equals("webplayer")) {
				createWebPlayer();
				handleViewPlacement(_webPlayer);
			}
		}
	}

	private void handleViewPlacement (View view) {
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

	/* LAYOUT */

	protected void createLayout () {
		if (_layout != null) {
			return;
		}

		_layout = new AdUnitRelativeLayout(this);
		_layout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		ViewUtilities.setBackground(_layout, new ColorDrawable(Color.BLACK));
	}

	/* WEBPLAYER */

	private void createWebPlayer() {
		if (_webPlayer == null) {
			_webPlayer = new WebPlayer(this, com.unity3d.ads.api.WebPlayer.getWebSettings(), com.unity3d.ads.api.WebPlayer.getWebPlayerSettings());
			_webPlayer.setEventSettings(com.unity3d.ads.api.WebPlayer.getWebPlayerEventSettings());
		}
	}

	private void destroyWebPlayer() {
		if (_webPlayer != null) {
			ViewUtilities.removeViewFromParent(_webPlayer);
		}

		_webPlayer = null;
	}

	/* VIDEOPLAYER */

	private void createVideoPlayer () {
		if (_videoContainer == null) {
			_videoContainer = new RelativeLayout(this);
		}

		if (VideoPlayer.getVideoPlayerView() == null) {
			VideoPlayer.setVideoPlayerView(new VideoPlayerView(this));
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
			params.addRule(RelativeLayout.CENTER_IN_PARENT);
			VideoPlayer.getVideoPlayerView().setLayoutParams(params);
			_videoContainer.addView(VideoPlayer.getVideoPlayerView());
		}
	}

	private void destroyVideoPlayer () {
		if (VideoPlayer.getVideoPlayerView() != null) {
			VideoPlayer.getVideoPlayerView().stopVideoProgressTimer();
			VideoPlayer.getVideoPlayerView().stopPlayback();
			ViewUtilities.removeViewFromParent(VideoPlayer.getVideoPlayerView());
		}

		VideoPlayer.setVideoPlayerView(null);

		if (_videoContainer != null) {
			ViewUtilities.removeViewFromParent(_videoContainer);
			_videoContainer = null;
		}
	}
}
