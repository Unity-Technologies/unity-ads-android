package com.unity3d.services.ar.view;

import android.os.Bundle;
import android.view.View;

import com.unity3d.services.ads.adunit.AdUnitActivity;
import com.unity3d.services.ads.adunit.IAdUnitViewHandler;
import com.unity3d.services.ar.ARCheck;
import com.unity3d.services.core.misc.ViewUtilities;

public class ARViewHandler implements IAdUnitViewHandler {
	private ARView _arView;

	public boolean create(AdUnitActivity activity) {
		if (!ARCheck.isFrameworkPresent()) {
			return true;
		}

		if (_arView == null) {
			_arView = new ARView(activity);
		}

		return true;
	}

	public boolean destroy() {
		if (_arView != null) {
			ViewUtilities.removeViewFromParent(_arView);
		}

		_arView = null;

		return true;
	}

	public View getView() {
		return _arView;
	}

	public void onCreate(AdUnitActivity activity, Bundle savedInstanceState) {
		this.create(activity);
	}

	public void onStart(AdUnitActivity activity) {
	}

	public void onStop(AdUnitActivity activity) {
	}

	public void onResume(AdUnitActivity activity) {
		if (_arView != null) {
			_arView.onResume();
		}
	}

	public void onPause(AdUnitActivity activity) {
		if (_arView != null) {
			_arView.onPause();
		}
	}

	public void onDestroy(AdUnitActivity activity) {
		if (activity.isFinishing()) {
			destroy();
		}
	}
}
