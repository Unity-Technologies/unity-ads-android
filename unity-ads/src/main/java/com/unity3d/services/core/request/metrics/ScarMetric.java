package com.unity3d.services.core.request.metrics;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ScarMetric {

	private static final String ASYNC_PREFIX = "async";

	private static final String SYNC_PREFIX = "sync";

	private static final String HB_SIGNALS_FETCH_START = "native_hb_signals_%s_fetch_start";

	private static final String HB_SIGNALS_FETCH_SUCCESS = "native_hb_signals_%s_fetch_success";

	private static final String HB_SIGNALS_FETCH_FAILURE = "native_hb_signals_%s_fetch_failure";

	private static final String HB_SIGNALS_UPLOAD_START = "native_hb_signals_%s_upload_start";

	private static final String HB_SIGNALS_UPLOAD_SUCCESS = "native_hb_signals_%s_upload_success";

	private static final String HB_SIGNALS_UPLOAD_FAILURE = "native_hb_signals_%s_upload_failure";

	private static final String REASON = "reason";

	private static long _fetchStartTime;
	private static long _uploadStartTime;

	private static long getTotalFetchTime() {
		return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - _fetchStartTime);
	}

	private static long getTotalUploadTime() {
		return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - _uploadStartTime);
	}

	public static Metric hbSignalsFetchStart(boolean isAsyncTokenCall) {
		_fetchStartTime = System.nanoTime();

		return new Metric(
			String.format(HB_SIGNALS_FETCH_START, isAsyncTokenCall ? ASYNC_PREFIX : SYNC_PREFIX),
			null
		);
	}

	public static Metric hbSignalsFetchSuccess(boolean isAsyncTokenCall) {
		return new Metric(
			String.format(HB_SIGNALS_FETCH_SUCCESS, isAsyncTokenCall ? ASYNC_PREFIX : SYNC_PREFIX),
			getTotalFetchTime()
		);
	}

	public static Metric hbSignalsFetchFailure(boolean isAsyncTokenCall, String errorMsg) {
		Map<String, String> tags = new HashMap<>();
		tags.put(REASON, errorMsg);

		return new Metric(
			String.format(HB_SIGNALS_FETCH_FAILURE, isAsyncTokenCall ? ASYNC_PREFIX : SYNC_PREFIX),
			getTotalFetchTime(),
			tags
		);
	}

	public static Metric hbSignalsUploadStart(boolean isAsyncTokenCall) {
		_uploadStartTime = System.nanoTime();
		return new Metric(
			String.format(HB_SIGNALS_UPLOAD_START, isAsyncTokenCall ? ASYNC_PREFIX : SYNC_PREFIX),
			null
		);
	}

	public static Metric hbSignalsUploadSuccess(boolean isAsyncTokenCall) {
		return new Metric(
			String.format(HB_SIGNALS_UPLOAD_SUCCESS, isAsyncTokenCall ? ASYNC_PREFIX : SYNC_PREFIX),
			getTotalUploadTime()
		);
	}

	public static Metric hbSignalsUploadFailure(boolean isAsyncTokenCall, String errorMsg) {
		Map<String, String> tags = new HashMap<>();
		tags.put(REASON, errorMsg);

		return new Metric(
			String.format(HB_SIGNALS_UPLOAD_FAILURE, isAsyncTokenCall ? ASYNC_PREFIX : SYNC_PREFIX),
			getTotalUploadTime(),
			tags
		);
	}
}
