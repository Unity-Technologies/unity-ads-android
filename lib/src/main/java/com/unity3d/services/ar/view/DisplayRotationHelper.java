package com.unity3d.services.ar.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

import com.google.ar.core.Session;

@SuppressWarnings("ConstantConditions")
@TargetApi(Build.VERSION_CODES.M)
public class DisplayRotationHelper implements DisplayManager.DisplayListener {
	private boolean viewportChanged;
	private int viewportWidth;
	private int viewportHeight;
	private final Context context;
	private final Display display;

	DisplayRotationHelper(Context context) {
		this.context = context;
		display = context.getSystemService(WindowManager.class).getDefaultDisplay();
	}

	void onResume() {
		context.getSystemService(DisplayManager.class).registerDisplayListener(this, null);
	}

	void onPause() {
		context.getSystemService(DisplayManager.class).unregisterDisplayListener(this);
	}

	void onSurfaceChanged(int width, int height) {
		viewportWidth = width;
		viewportHeight = height;
		viewportChanged = true;
	}

	void updateSessionIfNeeded(Session session) {
		if (viewportChanged) {
			int displayRotation = display.getRotation();
			session.setDisplayGeometry(displayRotation, viewportWidth, viewportHeight);
			viewportChanged = false;
		}
	}

	public int getRotation() {
		return display.getRotation();
	}

	@Override
	public void onDisplayAdded(int displayId) {}

	@Override
	public void onDisplayRemoved(int displayId) {}

	@Override
	public void onDisplayChanged(int displayId) {
		viewportChanged = true;
	}
}

