package com.unity3d.services.ads.adunit;

import android.os.Bundle;
import android.view.View;

public interface IAdUnitViewHandler {
	boolean create(IAdUnitActivity activity);
	boolean destroy();
	View getView();

	void onCreate(IAdUnitActivity activity, Bundle savedInstanceState);
	void onStart(IAdUnitActivity activity);
	void onStop(IAdUnitActivity activity);
	void onResume(IAdUnitActivity activity);
	void onPause(IAdUnitActivity activity);
	void onDestroy(IAdUnitActivity activity);
}
