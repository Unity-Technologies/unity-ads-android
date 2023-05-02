package com.unity3d.services.ads.adunit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import java.util.ArrayList;
import java.util.Map;

public interface IAdUnitActivity {
	void finish();

	void addContentView(View view, ViewGroup.LayoutParams params);

	Intent getIntent();

	boolean isFinishing();

	void setRequestedOrientation(int orientation);

	Window getWindow();

	Context getContext();

	void requestPermissions(String[] permissions, int requestCode);

	String[] getViews();

	int getRequestedOrientation();

	void setViews(String[] viewList);

	void setOrientation(int orientation);

	boolean setKeepScreenOn(boolean screenOn);

	boolean setSystemUiVisibility(int systemUiVisibility);

	void setKeyEventList(ArrayList<Integer> keyEventList);

	void setViewFrame(String view, int x, int y, int width, int height);

	Map<String, Integer> getViewFrame(String view);

	AdUnitRelativeLayout getLayout();

	void setLayoutInDisplayCutoutMode(int displayCutoutMode);

	Activity getActivity();
}
