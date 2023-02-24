package com.unity3d.services.core.request.metrics;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ScarMetric {

	public static final String HB_SIGNALS_FETCH_START = "native_hb_signals_fetch_start";
	private static final String HB_SIGNALS_FETCH_SUCCESS = "native_hb_signals_fetch_success";
	private static final String HB_SIGNALS_FETCH_FAILURE = "native_hb_signals_fetch_failure";

	private static final String HB_SIGNALS_UPLOAD_START = "native_hb_signals_upload_start";
	private static final String HB_SIGNALS_UPLOAD_SUCCESS = "native_hb_signals_upload_success";
	private static final String HB_SIGNALS_UPLOAD_FAILURE = "native_hb_signals_upload_failure";

	private static final String REASON = "reason";

	private static long _fetchStartTime;
	private static long _uploadStartTime;

	private static long getTotalFetchTime() {
		return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - _fetchStartTime);
	}

	private static long getTotalUploadTime() {
		return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - _uploadStartTime);
	}

	public static Metric hbSignalsFetchStart() {
		_fetchStartTime = System.nanoTime();
		return new Metric(
			HB_SIGNALS_FETCH_START,
			null,
			null
		);
	}

	public static Metric hbSignalsFetchSuccess() {
		return new Metric(
			HB_SIGNALS_FETCH_SUCCESS,
			getTotalFetchTime(),
			null
		);
	}

	public static Metric hbSignalsFetchFailure(String errorMsg) {
		Map<String, String> tags = new HashMap<>();
		tags.put(REASON, errorMsg);

		return new Metric(
			HB_SIGNALS_FETCH_FAILURE,
			getTotalFetchTime(),
			tags
		);
	}

	public static Metric hbSignalsUploadStart() {
		_uploadStartTime = System.nanoTime();
		return new Metric(
			HB_SIGNALS_UPLOAD_START,
			null,
			null
		);
	}

	public static Metric hbSignalsUploadSuccess() {
		return new Metric(
			HB_SIGNALS_UPLOAD_SUCCESS,
			getTotalUploadTime(),
			null
		);
	}

	public static Metric hbSignalsUploadFailure(String errorMsg) {
		Map<String, String> tags = new HashMap<>();
		tags.put(REASON, errorMsg);

		return new Metric(
			HB_SIGNALS_UPLOAD_FAILURE,
			getTotalUploadTime(),
			tags
		);
	}
}
