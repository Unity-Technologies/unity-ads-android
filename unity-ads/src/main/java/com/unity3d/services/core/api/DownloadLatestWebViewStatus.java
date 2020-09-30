package com.unity3d.services.core.api;

public enum DownloadLatestWebViewStatus {
	INIT_QUEUE_NULL(0),
	INIT_QUEUE_NOT_EMPTY(1),
	MISSING_LATEST_CONFIG(2),
	BACKGROUND_DOWNLOAD_STARTED(3);

	private final int value;

	DownloadLatestWebViewStatus(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
