package com.unity3d.services.core.properties;

import com.unity3d.services.core.log.DeviceLog;

public class MadeWithUnityDetector {

	// This class should be present in an application made with Unity
	public static final String UNITY_PLAYER_CLASS_NAME = "com.unity3d.player.UnityPlayer";

	public MadeWithUnityDetector() {}

	public static boolean isMadeWithUnity() {
		try {
			Class cls = Class.forName(UNITY_PLAYER_CLASS_NAME);
			if (cls != null) {
				return true;
			}
		} catch(ClassNotFoundException e) {
			// Do nothing
		} finally {
			return false;
		}
	}
}
