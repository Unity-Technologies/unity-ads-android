package com.unity3d.ads.log;

class DeviceLogEntry {

	private DeviceLogLevel _logLevel = null;
	private String _originalMessage = null;
	private StackTraceElement _stackTraceElement = null;

	public DeviceLogEntry(DeviceLogLevel logLevel, String originalMessage, StackTraceElement stackTraceElement) {
		_logLevel = logLevel;
		_originalMessage = originalMessage;
		_stackTraceElement = stackTraceElement;
	}

	public DeviceLogLevel getLogLevel () {
		return _logLevel;
	}

	public String getParsedMessage () {
		String message = _originalMessage;
		String className = "UnknownClass";
		String methodName = "unknownMethod";
		int lineNumber = -1;

		if (_stackTraceElement != null) {
			className = _stackTraceElement.getClassName();
			methodName = _stackTraceElement.getMethodName();
			lineNumber = _stackTraceElement.getLineNumber();
		}

		if (message != null && !message.isEmpty()) message = " :: " + message;
		if (message == null) message = "";

		String lineNumberPart =  " (line:" + lineNumber + ")";

		return className + "." + methodName + "()" + lineNumberPart + message;
	}
}
