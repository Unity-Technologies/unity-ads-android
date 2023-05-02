package com.unity3d.services.ads.adunit;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;

import com.unity3d.services.core.webview.bridge.SharedInstances;

import java.util.ArrayList;
import java.util.Map;

public class AdUnitActivity extends Activity implements IAdUnitActivity {

	public static final String EXTRA_VIEWS = "views";
	public static final String EXTRA_ACTIVITY_ID = "activityId";
	public static final String EXTRA_ORIENTATION = "orientation";
	public static final String EXTRA_SYSTEM_UI_VISIBILITY = "systemUiVisibility";
	public static final String EXTRA_KEY_EVENT_LIST = "keyEvents";
	public static final String EXTRA_KEEP_SCREEN_ON = "keepScreenOn";
	public static final String EXTRA_DISPLAY_CUTOUT_MODE = "displayCutoutMode";

	protected AdUnitActivityController _controller;

	protected AdUnitActivityController createController() {
		return new AdUnitActivityController(this, SharedInstances.INSTANCE.getWebViewEventSender(), new AdUnitViewHandlerFactory());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		_controller = createController();
		_controller.onCreate(savedInstanceState);
	}

	public AdUnitRelativeLayout getLayout() {
		return _controller.getLayout();
	}

	@Override
	protected void onStart() {
		super.onStart();

		_controller.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();

		_controller.onStop();
	}

	@Override
	protected void onResume() {
		super.onResume();

		_controller.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();

		_controller.onPause();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		_controller.onSaveInstanceState(outState);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		_controller.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return _controller.onKeyDown(keyCode, event);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		_controller.onWindowFocusChanged(hasFocus);
		super.onWindowFocusChanged(hasFocus);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		_controller.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	/* API */

	@Override
	public void setViewFrame (String view, int x, int y, int width, int height) {
		_controller.setViewFrame(view, x, y, width, height);
	}

	@Override
	public Map<String, Integer> getViewFrame (String view) {
		return _controller.getViewFrame(view);
	}

	@Override
	public void setViews (String[] views) {
		_controller.setViews(views);
	}

	@Override
	public Context getContext() {
		return this;
	}

	@Override
	public String[] getViews () {
		return _controller.getViews();
	}

	@Override
	public void setOrientation (int orientation) {
		_controller.setOrientation(orientation);
	}

	// Returns true if successfully set, false if error
	@Override
	public boolean setKeepScreenOn(boolean keepScreenOn) {
		return _controller.setKeepScreenOn(keepScreenOn);
	}

	@Override
	public boolean setSystemUiVisibility (int flags) {
		return _controller.setSystemUiVisibility(flags);
	}

	@Override
	public void setKeyEventList (ArrayList<Integer> keyevents) {
		_controller.setKeyEventList(keyevents);
	}

	public IAdUnitViewHandler getViewHandler(String name) {
		return _controller.getViewHandler(name);
	}

	@Override
	public void setLayoutInDisplayCutoutMode(int flags) {
		_controller.setLayoutInDisplayCutoutMode(flags);
	}

	@Override
	public Activity getActivity() {
		return this;
	}
}
