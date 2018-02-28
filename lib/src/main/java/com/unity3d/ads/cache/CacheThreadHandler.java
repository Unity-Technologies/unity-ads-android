package com.unity3d.ads.cache;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

import com.unity3d.ads.api.Request;
import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.device.Device;
import com.unity3d.ads.request.IWebRequestProgressListener;
import com.unity3d.ads.request.NetworkIOException;
import com.unity3d.ads.request.WebRequest;
import com.unity3d.ads.webview.WebViewApp;
import com.unity3d.ads.webview.WebViewEventCategory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class 	CacheThreadHandler extends Handler {
	private WebRequest _currentRequest = null;
	private boolean _canceled = false;
	private boolean _active = false;

	@Override
	public void handleMessage(Message msg) {
		Bundle data = msg.getData();
		String source = data.getString("source");
		data.remove("source");
		String target = data.getString("target");
		data.remove("target");
		int connectTimeout = data.getInt("connectTimeout");
		data.remove("connectTimeout");
		int readTimeout = data.getInt("readTimeout");
		data.remove("readTimeout");
		int progressInterval = data.getInt("progressInterval");
		data.remove("progressInterval");
		boolean append = data.getBoolean("append", false);
		data.remove("append");

		HashMap<String, List<String>> headers = null;
		if (data.size() > 0) {
			DeviceLog.debug("There are headers left in data, reading them");
			headers = new HashMap<>();
			List<String> values;

			for (String k : data.keySet()) {
				values = Arrays.asList(data.getStringArray(k));
				headers.put(k, values);
			}
		}

		File targetFile = new File(target);

		if ((append && !targetFile.exists()) || (!append && targetFile.exists())) {
			_active = false;
			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.CACHE, CacheEvent.DOWNLOAD_ERROR, CacheError.FILE_STATE_WRONG, source, target, append, targetFile.exists());
			return;
		}

		switch (msg.what) {
			case CacheThread.MSG_DOWNLOAD:
				downloadFile(source, target, connectTimeout, readTimeout, progressInterval, headers, append);
				break;

			default:
				break;
		}
	}

	public void setCancelStatus(boolean canceled) {
		_canceled = canceled;

		if(canceled && _currentRequest != null) {
			_active = false;
			_currentRequest.cancel();
		}
	}

	public boolean isActive() {
		return _active;
	}

	private void downloadFile(String source, String target, int connectTimeout, int readTimeout, final int progressInterval, HashMap<String, List<String>> headers, boolean append) {
		if (_canceled || source == null || target == null) {
			return;
		}

		final File targetFile = new File(target);

		if (append) {
			DeviceLog.debug("Unity Ads cache: resuming download " + source + " to " + target + " at " + targetFile.length() + " bytes");
		}
		else {
			DeviceLog.debug("Unity Ads cache: start downloading " + source + " to " + target);
		}

		if (!Device.isActiveNetworkConnected()) {
			DeviceLog.debug("Unity Ads cache: download cancelled, no internet connection available");
			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.CACHE, CacheEvent.DOWNLOAD_ERROR, CacheError.NO_INTERNET, source);
			return;
		}

		_active = true;

		long startTime = SystemClock.elapsedRealtime();
		FileOutputStream fileOutput = null;

		try {
			fileOutput = new FileOutputStream(targetFile, append);
			_currentRequest = getWebRequest(source, connectTimeout, readTimeout, headers);
			_currentRequest.setProgressListener(new IWebRequestProgressListener() {
				private long lastProgressEventTime = System.currentTimeMillis();

				@Override
				public void onRequestStart(String url, long total, int responseCode, Map<String, List<String>> headers) {
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.CACHE, CacheEvent.DOWNLOAD_STARTED, url, targetFile.length(), (total + targetFile.length()), responseCode, Request.getResponseHeadersMap(headers));
				}

				@Override
				public void onRequestProgress(String url, long bytes, long total) {
					if(progressInterval > 0 && System.currentTimeMillis() - lastProgressEventTime > progressInterval) {
						lastProgressEventTime = System.currentTimeMillis();
						WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.CACHE, CacheEvent.DOWNLOAD_PROGRESS, url, bytes, total);
					}
				}
			});

			long total = _currentRequest.makeStreamRequest(fileOutput);

			// Note: active must be set to false before sending end/error event back to webview to allow webview to start next download or other operation immediately after receiving the event
			_active = false;
			postProcessDownload(startTime, source, targetFile, total, _currentRequest.getContentLength(), _currentRequest.isCanceled(), _currentRequest.getResponseCode(), _currentRequest.getResponseHeaders());
		}
		catch (FileNotFoundException e) {
			DeviceLog.exception("Couldn't create target file", e);
			_active = false;
			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.CACHE, CacheEvent.DOWNLOAD_ERROR, CacheError.FILE_IO_ERROR, source, e.getMessage());
		}
		catch (MalformedURLException e) {
			DeviceLog.exception("Malformed URL", e);
			_active = false;
			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.CACHE, CacheEvent.DOWNLOAD_ERROR, CacheError.MALFORMED_URL, source, e.getMessage());
		}
		catch (IOException e) {
			DeviceLog.exception("Couldn't request stream", e);
			_active = false;
			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.CACHE, CacheEvent.DOWNLOAD_ERROR, CacheError.FILE_IO_ERROR, source, e.getMessage());
		}
		catch (IllegalStateException e) {
			DeviceLog.exception("Illegal state", e);
			_active = false;
			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.CACHE, CacheEvent.DOWNLOAD_ERROR, CacheError.ILLEGAL_STATE, source, e.getMessage());
		}
		catch (NetworkIOException e) {
			DeviceLog.exception("Network error", e);
			_active = false;
			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.CACHE, CacheEvent.DOWNLOAD_ERROR, CacheError.NETWORK_ERROR, source, e.getMessage());
		}
		finally {
			_currentRequest = null;
			try {
				if (fileOutput != null) {
					fileOutput.close();
				}
			} catch (Exception e) {
				DeviceLog.exception("Error closing stream", e);
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.CACHE, CacheEvent.DOWNLOAD_ERROR, CacheError.FILE_IO_ERROR, source, e.getMessage());
			}
		}
	}

	private void postProcessDownload(long startTime, String source, File targetFile, long byteCount, long totalBytes, boolean canceled, int responseCode, Map<String, List<String>> responseHeaders) {
		long duration = SystemClock.elapsedRealtime() - startTime;

		// With some old Androids the MediaPlayer cannot play the file unless it's set to readable for all
		boolean result = targetFile.setReadable(true, false);
		if (!result) {
			DeviceLog.debug("Unity Ads cache: could not set file readable!");
		}

		if (!canceled) {
			DeviceLog.debug("Unity Ads cache: File " + targetFile.getName() + " of " + byteCount + " bytes downloaded in " + duration + "ms");
			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.CACHE, CacheEvent.DOWNLOAD_END, source, byteCount, totalBytes, duration, responseCode, Request.getResponseHeadersMap(responseHeaders));
		} else {
			DeviceLog.debug("Unity Ads cache: downloading of " + source + " stopped");
			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.CACHE, CacheEvent.DOWNLOAD_STOPPED, source, byteCount, totalBytes, duration, responseCode, Request.getResponseHeadersMap(responseHeaders));
		}
	}

	private WebRequest getWebRequest(String source, int connectTimeout, int readTimeout, HashMap<String, List<String>> headers) throws MalformedURLException {
		HashMap<String, List<String>> requestHeaders = new HashMap<>();
		if (headers != null) {
			requestHeaders.putAll(headers);
		}

		return new WebRequest(source, "GET", requestHeaders, connectTimeout, readTimeout);
	}
}