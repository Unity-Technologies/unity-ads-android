package com.unity3d.services.core.configuration;

public enum ErrorState {
	CreateWebApp("create_webapp"),
	NetworkConfigRequest("network_config"),
	NetworkWebviewRequest("network_webview"),
	InvalidHash("invalid_hash"),
	CreateWebview("create_webview"),
	MalformedWebviewRequest("malformed_webview"),
	ResetWebApp("reset_webapp"),
	LoadCache("load_cache"),
	InitModules("init_modules"),
	CreateWebviewTimeout("create_webview_timeout"),
	CreateWebviewGameIdDisabled("create_webview_game_id_disabled"),
	CreateWebviewConfigError("create_webview_config_error"),
	CreateWebviewInvalidArgument("create_webview_invalid_arg");

	private String _stateMetricName;

	ErrorState(String stateMetricName) {
		_stateMetricName = stateMetricName;
	}

	public String getMetricName() {
		return _stateMetricName;
	}
}