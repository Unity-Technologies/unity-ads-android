package com.unity3d.services.core.misc;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;

import com.unity3d.services.core.log.DeviceLog;

import java.lang.reflect.Method;

public class ViewUtilities {
	public static void removeViewFromParent(View view) {
		if (view != null && view.getParent() != null) {
			try {
				((ViewGroup)view.getParent()).removeView(view);
			}
			catch (Exception e) {
				DeviceLog.exception("Error while removing view from it's parent", e);
			}
		}
	}

	public static void setBackground(View view, Drawable drawable) {
		Class<View> cl = View.class;
		Method setBackground;
		String methodName = "setBackground";

		int sdk = android.os.Build.VERSION.SDK_INT;
		if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
			methodName = "setBackgroundDrawable";
		}

		try {
			setBackground = cl.getMethod(methodName, Drawable.class);
			setBackground.invoke(view, drawable);
		}
		catch (Exception e) {
			DeviceLog.exception("Couldn't run" + methodName, e);
		}
	}

	public static float dpFromPx(final Context context, final float px) {
		return px / context.getResources().getDisplayMetrics().density;
	}

	public static float pxFromDp(final Context context, final float dp) {
		return dp * context.getResources().getDisplayMetrics().density;
	}
}
