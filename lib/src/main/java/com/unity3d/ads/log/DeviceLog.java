package com.unity3d.ads.log;

import android.util.Log;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;

public class DeviceLog {

	private static boolean LOG_ERROR = true;
	private static boolean LOG_WARNING = true;
	private static boolean LOG_INFO = true;
	private static boolean LOG_DEBUG = true;
	private static boolean FORCE_DEBUG_LOG = false;

	private static final int MAX_DEBUG_MSG_LENGTH = 3072;
	private static final int LOGLEVEL_ERROR = 1;
	private static final int LOGLEVEL_WARNING = 2;
	public static final int LOGLEVEL_INFO = 4;
	public static final int LOGLEVEL_DEBUG = 8;

	public enum UnityAdsLogLevel {
		INFO, DEBUG, WARNING, ERROR
	}

	private static final HashMap<UnityAdsLogLevel, DeviceLogLevel> _deviceLogLevel = new HashMap<>();

	static {
		if (_deviceLogLevel.size() == 0) {
			_deviceLogLevel.put(UnityAdsLogLevel.INFO, new DeviceLogLevel("i"));
			_deviceLogLevel.put(UnityAdsLogLevel.DEBUG, new DeviceLogLevel("d"));
			_deviceLogLevel.put(UnityAdsLogLevel.WARNING, new DeviceLogLevel("w"));
			_deviceLogLevel.put(UnityAdsLogLevel.ERROR, new DeviceLogLevel("e"));
		}

		File forceDebugMode = new File("/data/local/tmp/UnityAdsForceDebugMode");
		if (forceDebugMode.exists()) {
			FORCE_DEBUG_LOG = true;
		}
	}

	public static void setLogLevel(int newLevel) {
		if(newLevel >= LOGLEVEL_DEBUG) {
			LOG_ERROR = true;
			LOG_WARNING = true;
			LOG_INFO = true;
			LOG_DEBUG = true;
		} else if(newLevel >= LOGLEVEL_INFO) {
			LOG_ERROR = true;
			LOG_WARNING = true;
			LOG_INFO = true;
			LOG_DEBUG = false;
		} else if(newLevel >= LOGLEVEL_WARNING) {
			LOG_ERROR = true;
			LOG_WARNING = true;
			LOG_INFO = false;
			LOG_DEBUG = false;
		} else if(newLevel >= LOGLEVEL_ERROR) {
			LOG_ERROR = true;
			LOG_WARNING = false;
			LOG_INFO = false;
			LOG_DEBUG = false;
		} else {
			LOG_ERROR = false;
			LOG_WARNING = false;
			LOG_INFO = false;
			LOG_DEBUG = false;
		}
	}



	public static void entered() {
		debug("ENTERED METHOD");
	}

	public static void info(String message) {
		write(UnityAdsLogLevel.INFO, checkMessage(message));
	}

	@SuppressWarnings({"unused"})
	public static void info(String format, Object... args) {
		info(String.format(format, args));
	}

	public static void debug(String message) {
		if(!LOG_DEBUG && !FORCE_DEBUG_LOG) {
			return;
		}

		if(message.length() > MAX_DEBUG_MSG_LENGTH) {
			debug(message.substring(0, MAX_DEBUG_MSG_LENGTH));

			if(message.length() < 10 * MAX_DEBUG_MSG_LENGTH) {
				debug(message.substring(MAX_DEBUG_MSG_LENGTH));
			}

			return;
		}

		write(UnityAdsLogLevel.DEBUG, checkMessage(message));
	}

	@SuppressWarnings("unused")
	public static void debug(String format, Object... args) {
		debug(String.format(format, args));
	}

	public static void warning(String message) {
		write(UnityAdsLogLevel.WARNING, checkMessage(message));
	}

	@SuppressWarnings({"unused"})
	public static void warning(String format, Object... args) {
		warning(String.format(format, args));
	}

	public static void error(String message) {
		write(UnityAdsLogLevel.ERROR, checkMessage(message));
	}

	public static void exception(String message, Exception exception) {
		String finalMessage = "";
		if (message != null) {
			finalMessage += message;
		}
		if (exception != null) {
			finalMessage += ": " + exception.getMessage();
		}
		if (exception != null && exception.getCause() != null) {
			finalMessage += ": " + exception.getCause().getMessage();
		}

		write(UnityAdsLogLevel.ERROR, finalMessage);
	}

	@SuppressWarnings("unused")
	public static void error(String format, Object... args) {
		error(String.format(format, args));
	}

	private static void write(UnityAdsLogLevel level, String message) {
		boolean logThisMessage = true;

		switch (level) {
			case INFO:
				logThisMessage = LOG_INFO;
				break;
			case DEBUG:
				logThisMessage = LOG_DEBUG;
				break;
			case WARNING:
				logThisMessage = LOG_WARNING;
				break;
			case ERROR:
				logThisMessage = LOG_ERROR;
				break;
			default:
				break;
		}

		if (FORCE_DEBUG_LOG) {
			logThisMessage = true;
		}

		if (logThisMessage) {
			DeviceLogEntry logEntry = createLogEntry(level, message);
			writeToLog(logEntry);
		}
	}

	private static String checkMessage (String message) {
		String finalMessage;

		if (message == null || message.length() == 0) {
			finalMessage = "DO NOT USE EMPTY MESSAGES, use DeviceLog.entered() instead";
		}
		else {
			finalMessage = message;
		}
		
		return finalMessage;
	}
	
	private static DeviceLogLevel getLogLevel(UnityAdsLogLevel logLevel) {
		return _deviceLogLevel.get(logLevel);
	}

	private static DeviceLogEntry createLogEntry(UnityAdsLogLevel level, String message) {
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		StackTraceElement e;
		DeviceLogLevel logLevel = getLogLevel(level);
		DeviceLogEntry logEntry = null;

		if (logLevel != null) {
			int callerIndex;
			boolean markedIndex = false;

			for (callerIndex = 0; callerIndex < stack.length; callerIndex++) {
				e = stack[callerIndex];
				if (e.getClassName().equals(DeviceLog.class.getName())) {
					markedIndex = true;
				}
				if (!e.getClassName().equals(DeviceLog.class.getName()) && markedIndex) {
					break;
				}
			}

			e = null;

			if (callerIndex < stack.length) {
				e = stack[callerIndex];
			}

			if (e != null) {
				logEntry = new DeviceLogEntry(logLevel, message, e);
			}
		}

		return logEntry;
	}

	private static void writeToLog(DeviceLogEntry logEntry) {
		Method receivingMethod = null;

		if (logEntry != null && logEntry.getLogLevel() != null) {
			try {
				receivingMethod = Log.class.getMethod(logEntry.getLogLevel().getReceivingMethodName(), String.class, String.class);
			}
			catch (Exception e) {
				Log.e("UnityAds", "Writing to log failed!", e);
			}

			if (receivingMethod != null) {
				try {
					receivingMethod.invoke(null, logEntry.getLogLevel().getLogTag(), logEntry.getParsedMessage());
				}
				catch (Exception e) {
					Log.e("UnityAds", "Writing to log failed!", e);
				}
			}
		}
	}
}