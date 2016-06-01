package com.unity3d.ads.cache;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

import com.unity3d.ads.api.Cache;
import com.unity3d.ads.api.Request;
import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.device.Device;
import com.unity3d.ads.request.IWebRequestProgressListener;
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

class CacheThreadHandler extends Handler {
	private WebRequest _currentRequest = null;
	private boolean _canceled = false;
	private boolean _active = false;

	@Override
	public void handleMessage(Message msg) {
		Bundle data = msg.getData();
		String source = data.getString("source");
		String target = data.getString("target");
		int connectTimeout = data.getInt("connectTimeout");
		int readTimeout = data.getInt("readTimeout");
		int progressInterval = data.getInt("progressInterval");

		switch (msg.what) {
			case CacheThread.MSG_DOWNLOAD:
				File targetFile = new File(target);
				downloadFile(source, target, targetFile.length(), connectTimeout, readTimeout, progressInterval);
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

	private void downloadFile(String source, String target, final long position, int connectTimeout, int readTimeout, final int progressInterval) {
		if (_canceled || source == null || target == null) {
			return;
		}

		if (position > 0) {
			DeviceLog.debug("Unity Ads cache: resuming download " + source + " to " + target + " at " + position + " bytes");
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
		File targetFile = new File(target);
		FileOutputStream fileOutput = null;

		try {
			fileOutput = new FileOutputStream(targetFile, position > 0);
			_currentRequest = getWebRequest(source, position, connectTimeout, readTimeout);
			_currentRequest.setProgressListener(new IWebRequestProgressListener() {
				private long lastProgressEventTime = System.currentTimeMillis();

				@Override
				public void onRequestStart(String url, long total, int responseCode, Map<String, List<String>> headers) {
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.CACHE, CacheEvent.DOWNLOAD_STARTED, url, position, total, responseCode, Request.getResponseHeadersMap(headers));
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
		} catch (FileNotFoundException e) {
			DeviceLog.exception("Couldn't create target file", e);
			_active = false;
			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.CACHE, CacheEvent.DOWNLOAD_ERROR, CacheError.FILE_IO_ERROR, source, e.getMessage());
		} catch (IOException e) {
			DeviceLog.exception("Couldn't request stream", e);
			_active = false;
			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.CACHE, CacheEvent.DOWNLOAD_ERROR, CacheError.FILE_IO_ERROR, source, e.getMessage());
		} finally {
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

		if (Build.VERSION.SDK_INT < 19) {
			// With some old Androids the MediaPlayer cannot play the file unless it's set to readable for all
			boolean result = targetFile.setReadable(true, false);
			if (!result) {
				DeviceLog.debug("Unity Ads cache: could not set file readable!");
			}
		}

		if (!canceled) {
			DeviceLog.debug("Unity Ads cache: File " + targetFile.getName() + " of " + byteCount + " bytes downloaded in " + duration + "ms");
			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.CACHE, CacheEvent.DOWNLOAD_END, source, byteCount, totalBytes, duration, responseCode, Request.getResponseHeadersMap(responseHeaders));
		} else {
			DeviceLog.debug("Unity Ads cache: downloading of " + source + " stopped");
			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.CACHE, CacheEvent.DOWNLOAD_STOPPED, source, byteCount, totalBytes, duration, responseCode, Request.getResponseHeadersMap(responseHeaders));
		}
	}

	private WebRequest getWebRequest(String source, long position, int connectTimeout, int readTimeout) {
		HashMap<String, List<String>> headers = new HashMap<>();
		if (position > 0) {
			ArrayList list = new ArrayList(Arrays.asList(new String[]{"bytes=" + position + "-"}));
			headers.put("Range", list);
		}

		WebRequest request = null;

		try {
			request = new WebRequest(source, "GET", headers, connectTimeout, readTimeout);
		}
		catch (MalformedURLException e) {
			DeviceLog.exception("Malformed URL", e);
		}

		return request;
	}
}