package com.unity3d.ads.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.properties.ClientProperties;

public class AndroidPreferences {
	public static boolean hasKey(String name, String key) {
		SharedPreferences settings = ClientProperties.getApplicationContext().getSharedPreferences(name, Context.MODE_PRIVATE);
		if(settings != null) {
			if (settings.contains(key)) {
				return true;
			}
		}

		return false;
	}

	public static String getString(String name, String key) {
		SharedPreferences settings = ClientProperties.getApplicationContext().getSharedPreferences(name, Context.MODE_PRIVATE);
		if(settings != null) {
			if(settings.contains(key)) {
				try {
					return settings.getString(key, "");
				} catch(ClassCastException e) {
					DeviceLog.error("Unity Ads failed to cast " + key + ": " + e.getMessage());
					return null;
				}
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public static Integer getInteger(String name, String key) {
		SharedPreferences settings = ClientProperties.getApplicationContext().getSharedPreferences(name, Context.MODE_PRIVATE);
		if(settings != null) {
			if(settings.contains(key)) {
				try {
					return settings.getInt(key, -1);
				} catch(ClassCastException e) {
					DeviceLog.error("Unity Ads failed to cast " + key + ": " + e.getMessage());
					return null;
				}
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public static Long getLong(String name, String key) {
		SharedPreferences settings = ClientProperties.getApplicationContext().getSharedPreferences(name, Context.MODE_PRIVATE);
		if(settings != null) {
			if(settings.contains(key)) {
				try {
					return settings.getLong(key, -1);
				} catch(ClassCastException e) {
					DeviceLog.error("Unity Ads failed to cast " + key + ": " + e.getMessage());
					return null;
				}
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public static Boolean getBoolean(String name, String key) {
		SharedPreferences settings = ClientProperties.getApplicationContext().getSharedPreferences(name, Context.MODE_PRIVATE);
		if(settings != null) {
			if(settings.contains(key)) {
				try {
					return settings.getBoolean(key, false);
				} catch(ClassCastException e) {
					DeviceLog.error("Unity Ads failed to cast " + key + ": " + e.getMessage());
					return null;
				}
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public static Float getFloat(String name, String key) {
		SharedPreferences settings = ClientProperties.getApplicationContext().getSharedPreferences(name, Context.MODE_PRIVATE);
		if(settings != null) {
			if(settings.contains(key)) {
				try {
					return settings.getFloat(key, Float.NaN);
				} catch(ClassCastException e) {
					DeviceLog.error("Unity Ads failed to cast " + key + ": " + e.getMessage());
					return null;
				}
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public static void setString(String name, String key, String value) {
		SharedPreferences settings = ClientProperties.getApplicationContext().getSharedPreferences(name, Context.MODE_PRIVATE);
		if (settings != null) {
			SharedPreferences.Editor editor = settings.edit();
			editor.putString(key,value);
			editor.commit();
		}
	}

	public static void setInteger(String name, String key, Integer value) {
		SharedPreferences settings = ClientProperties.getApplicationContext().getSharedPreferences(name, Context.MODE_PRIVATE);
		if (settings != null) {
			SharedPreferences.Editor editor = settings.edit();
			editor.putInt(key,value);
			editor.commit();
		}
	}

	public static void setLong(String name, String key, Long value) {
		SharedPreferences settings = ClientProperties.getApplicationContext().getSharedPreferences(name, Context.MODE_PRIVATE);
		if (settings != null) {
			SharedPreferences.Editor editor = settings.edit();
			editor.putLong(key,value);
			editor.commit();
		}
	}

	public static void setBoolean(String name, String key, Boolean value) {
		SharedPreferences settings = ClientProperties.getApplicationContext().getSharedPreferences(name, Context.MODE_PRIVATE);
		if (settings != null) {
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean(key,value);
			editor.commit();
		}
	}

	public static void setFloat(String name, String key, Double value) {
		SharedPreferences settings = ClientProperties.getApplicationContext().getSharedPreferences(name, Context.MODE_PRIVATE);
		if (settings != null) {
			SharedPreferences.Editor editor = settings.edit();
			editor.putFloat(key,value.floatValue());
			editor.commit();
		}
	}

	public static void removeKey(String name, String key) {
		SharedPreferences settings = ClientProperties.getApplicationContext().getSharedPreferences(name, Context.MODE_PRIVATE);
		if (settings != null) {
			SharedPreferences.Editor editor = settings.edit();
			editor.remove(key);
			editor.commit();
		}
	}
}
