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
import android.widget.RelativeLayout;

import com.unity3d.ads.api.AdUnit;
import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.misc.ViewUtilities;
import com.unity3d.ads.api.VideoPlayer;
import com.unity3d.ads.video.VideoPlayerView;
import com.unity3d.ads.webview.WebViewApp;
import com.unity3d.ads.webview.WebViewEventCategory;

import java.util.ArrayList;
import java.util.Arrays;

public class AdUnitActivity extends Activity {

	public static final String EXTRA_VIEWS = "views";
	public static final String EXTRA_ORIENTATION = "orientation";
	public static final String EXTRA_SYSTEM_UI_VISIBILITY = "systemUiVisibility";
	public static final String EXTRA_KEY_EVENT_LIST = "keyEvents";
	public static final String EXTRA_KEEP_SCREEN_ON = "keepScreenOn";

	private RelativeLayout _layout;
	private String[] _views;
	private int _orientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
	private int _systemUiVisibility;
	private ArrayList<Integer> _keyEventList;
	boolean _keepScreenOn;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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
			event = AdUnitEvent.ON_CREATE;
		} else {
			_views = savedInstanceState.getStringArray(EXTRA_VIEWS);
			_orientation = savedInstanceState.getInt(EXTRA_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
			_systemUiVisibility = savedInstanceState.getInt(EXTRA_SYSTEM_UI_VISIBILITY, 0);
			_keyEventList = savedInstanceState.getIntegerArrayList(EXTRA_KEY_EVENT_LIST);
			_keepScreenOn = savedInstanceState.getBoolean(EXTRA_KEEP_SCREEN_ON);
			setKeepScreenOn(_keepScreenOn);
			event = AdUnitEvent.ON_RESTORE;
		}

		setOrientation(_orientation);
		setSystemUiVisibility(_systemUiVisibility);

		if (_views != null && Arrays.asList(_views).contains("videoplayer")) {
			createVideoPlayer();
		}

		WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.ADUNIT, event);
	}

	@Override
	protected void onStart() {
		super.onStart();
		WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.ADUNIT, AdUnitEvent.ON_START);
	}

	@Override
	protected void onStop() {
		super.onStop();
		WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.ADUNIT, AdUnitEvent.ON_STOP);
	}

	@Override
	protected void onResume() {
		super.onResume();
		setViews(_views);

		WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.ADUNIT, AdUnitEvent.ON_RESUME);
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (isFinishing()) {
			ViewUtilities.removeViewFromParent(WebViewApp.getCurrentApp().getWebView());
		}

		destroyVideoPlayer();
		WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.ADUNIT, AdUnitEvent.ON_PAUSE, isFinishing());
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(EXTRA_ORIENTATION, _orientation);
		outState.putInt(EXTRA_SYSTEM_UI_VISIBILITY, _systemUiVisibility);
		outState.putIntegerArrayList(EXTRA_KEY_EVENT_LIST, _keyEventList);
		outState.putBoolean(EXTRA_KEEP_SCREEN_ON, _keepScreenOn);
		outState.putStringArray(EXTRA_VIEWS, _views);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		AdUnit.setAdUnitActivity(null);
		WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.ADUNIT, AdUnitEvent.ON_DESTROY, isFinishing());
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (_keyEventList != null) {
			if (_keyEventList.contains(keyCode)) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.ADUNIT, AdUnitEvent.KEY_DOWN, keyCode, event.getEventTime(), event.getDownTime(), event.getRepeatCount());
				return true;
			}
		}

		return false;
	}

	/* API */

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
				handleViewPlacement(VideoPlayer.getVideoPlayerView());
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

	private void createLayout () {
		if (_layout != null) {
			return;
		}

		_layout = new RelativeLayout(this);
		_layout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		ViewUtilities.setBackground(_layout, new ColorDrawable(Color.BLACK));
	}

	/* VIDEOPLAYER */

	private void createVideoPlayer () {
		if (VideoPlayer.getVideoPlayerView() == null) {
			VideoPlayer.setVideoPlayerView(new VideoPlayerView(this));
		}
	}

	private void destroyVideoPlayer () {
		if (VideoPlayer.getVideoPlayerView() != null) {
			ViewUtilities.removeViewFromParent(VideoPlayer.getVideoPlayerView());
			VideoPlayer.getVideoPlayerView().stop();
		}

		VideoPlayer.setVideoPlayerView(null);
	}
}
