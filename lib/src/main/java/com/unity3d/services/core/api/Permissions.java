package com.unity3d.services.core.api;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;


import com.unity3d.services.ads.adunit.AdUnitError;
import com.unity3d.services.ads.api.AdUnit;
import com.unity3d.services.core.device.DeviceError;
import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.services.core.webview.bridge.WebViewCallback;
import com.unity3d.services.core.webview.bridge.WebViewExposed;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

enum PermissionsError {
	COULDNT_GET_PERMISSIONS,
	NO_REQUESTED_PERMISSIONS,
	ERROR_CHECKING_PERMISSION,
	ERROR_REQUESTING_PERMISSIONS,
	PERMISSION_NOT_GRANTED
}

public class Permissions {
	@WebViewExposed
	public static void getPermissions(WebViewCallback callback) {
		if (ClientProperties.getApplicationContext() == null) {
			callback.error(DeviceError.APPLICATION_CONTEXT_NULL);
			return;
		}

		try {
			JSONArray retArray = new JSONArray();
			Context context = ClientProperties.getApplicationContext();
			PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
			if (info.requestedPermissions != null) {
				for (String p : info.requestedPermissions) {
					retArray.put(p);
				}

				callback.invoke(retArray);
			}
			else {
				callback.error(PermissionsError.NO_REQUESTED_PERMISSIONS);
			}
		} catch (Exception e) {
			callback.error(PermissionsError.COULDNT_GET_PERMISSIONS, e.getMessage());
		}
	}

	@WebViewExposed
	public static void checkPermission(String permission, WebViewCallback callback) {
		if (ClientProperties.getApplicationContext() == null) {
			callback.error(DeviceError.APPLICATION_CONTEXT_NULL);
			return;
		}

		try {
			Context context = ClientProperties.getApplicationContext();
			int granted = context.getPackageManager().checkPermission(permission, context.getPackageName());
			callback.invoke(granted);
		}
		catch (Exception e) {
			callback.error(PermissionsError.ERROR_CHECKING_PERMISSION, e.getMessage());
		}
	}

	@TargetApi(23)
	@WebViewExposed
	public static void requestPermissions(JSONArray permissions, Integer requestCode, WebViewCallback callback) {
		if (AdUnit.getAdUnitActivity() == null) {
			callback.error(AdUnitError.ADUNIT_NULL);
			return;
		}

		if (permissions == null || permissions.length() < 1) {
			callback.error(PermissionsError.NO_REQUESTED_PERMISSIONS);
			return;
		}

		try {
			List<String> permissionsList = new ArrayList<>();
			for (int i = 0; i < permissions.length(); i++) {
				permissionsList.add(permissions.getString(i));
			}

			String[] permissionsArray = new String[permissionsList.size()];
			AdUnit.getAdUnitActivity().requestPermissions(permissionsList.toArray(permissionsArray), requestCode);
			callback.invoke();
		}
		catch (Exception e) {
			callback.error(PermissionsError.ERROR_REQUESTING_PERMISSIONS, e.getMessage());
		}
	}
}
