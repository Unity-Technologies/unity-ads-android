package com.unity3d.services.ar;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.FatalException;
import com.google.ar.core.exceptions.UnavailableException;
import com.unity3d.services.core.log.DeviceLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ARUtils {
	public static final int AR_CHECK_NOT_SUPPORTED = 0x0;
	public static final int AR_CHECK_SUPPORTED = 0x1;
	public static final int AR_CHECK_TRANSIENT = 0x2;

	private static Config.PlaneFindingMode[] planeFindingModes;
	private static Config.LightEstimationMode[] lightEstimationModes;
	private static Config.UpdateMode[] updateModes;

	public static int isSupported(Context context) {
		if (!ARCheck.isFrameworkPresent()) {
			return AR_CHECK_NOT_SUPPORTED;
		}

		if (planeFindingModes == null) {
			planeFindingModes = Config.PlaneFindingMode.values();
			lightEstimationModes = Config.LightEstimationMode.values();
			updateModes = Config.UpdateMode.values();
		}

		ArCoreApk.Availability availability = ArCoreApk.getInstance().checkAvailability(context);
		if (availability.isTransient()) {
			return AR_CHECK_TRANSIENT;
		}

		// ARCore APK might be side loaded onto the device. In that case, the availability check
		// will return SUPPORTED_INSTALLED but session creation will still fail. We try creating a
		// session here, just to make sure the device supports ARCore.
		// Anything other than SUPPORTED_INSTALLED is considered unsupported in our case, because
		// we don't want to prompt an install dialog before showing an ad.
		if (availability == ArCoreApk.Availability.SUPPORTED_INSTALLED) {
			try {
				//noinspection unused
				Session session = new Session(context);
			} catch (FatalException | UnavailableException e) {
				return AR_CHECK_NOT_SUPPORTED;
			} catch (SecurityException ignored) {
				// Session creation failed because we don't have camera permission yet.
				// This can be ignored.
			}

			return AR_CHECK_SUPPORTED;
		}

		return AR_CHECK_NOT_SUPPORTED;
	}

	public static Config createConfiguration(final JSONObject properties, final Session session) {
		Config config = new Config(session);

		if (properties.has("lightEstimationMode")) {
			try {
				String lightEstimationMode = properties.getString("lightEstimationMode");
				for (Config.LightEstimationMode lem: lightEstimationModes) {
					if (lightEstimationMode.equalsIgnoreCase(lem.name())) {
						config.setLightEstimationMode(lem);
						break;
					}
				}
			} catch (JSONException e) {
				DeviceLog.warning("lightEstimationEnabled is not a string.");
			}
		}

		if (properties.has("planeFindingMode")) {
			try {
				String planeFindingMode = properties.getString("planeFindingMode");
				for (Config.PlaneFindingMode pfm: planeFindingModes) {
					if (planeFindingMode.equalsIgnoreCase(pfm.name())) {
						config.setPlaneFindingMode(pfm);
						break;
					}
				}
			} catch (JSONException e) {
				DeviceLog.warning("planeFindingMode is not a string.");
			}
		}

		if (properties.has("updateMode")) {
			try {
				String updateMode = properties.getString("updateMode");
				for (Config.UpdateMode um: updateModes) {
					if (updateMode.equalsIgnoreCase(um.name())) {
						config.setUpdateMode(um);
						break;
					}
				}
			} catch (JSONException e) {
				DeviceLog.warning("updateMode is not a string.");
			}
		}

		return config;
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	public static JSONObject getConfigEnums() {
		JSONObject enums = new JSONObject();

		try {
			ArrayList<String> values = new ArrayList<>();
			for (Config.LightEstimationMode lem: Config.LightEstimationMode.values()) {
				values.add(lem.name());
			}
			enums.put("lightEstimationMode", new JSONArray(values.toArray()));

			values.clear();
			for (Config.PlaneFindingMode pfm: Config.PlaneFindingMode.values()) {
				values.add(pfm.name());
			}
			enums.put("planeFindingMode", new JSONArray(values.toArray()));

			values.clear();
			for (Config.UpdateMode um: Config.UpdateMode.values()) {
				values.add(um.name());
			}
			enums.put("updateMode", new JSONArray(values.toArray()));
		} catch (JSONException ignored) {}

		return enums;
	}
}



