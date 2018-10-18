package com.unity3d.services.ar;

public class ARCheck {
	public static boolean isFrameworkPresent() {
		try {
			Class arClass = Class.forName("com.google.ar.core.Session");
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
