package com.unity3d.services.ads.adunit;

import android.os.Bundle;
import android.view.View;

public interface IAdUnitViewHandler {
	boolean create(AdUnitActivity activity);
	boolean destroy();
	View getView();

	void onCreate(AdUnitActivity activity, Bundle savedInstanceState);
	void onStart(AdUnitActivity activity);
	void onStop(AdUnitActivity activity);
	void onResume(AdUnitActivity activity);
	void onPause(AdUnitActivity activity);
	void onDestroy(AdUnitActivity activity);
}
