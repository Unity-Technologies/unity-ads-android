package com.wds.ads.api;

import com.wds.ads.cache.CacheError;
import com.wds.ads.cache.CacheThread;
import com.wds.ads.device.Device;
import com.wds.ads.log.DeviceLog;
import com.wds.ads.misc.Utilities;
import com.wds.ads.properties.SdkProperties;
import com.wds.ads.webview.bridge.WebViewCallback;
import com.wds.ads.webview.bridge.WebViewExposed;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;

public class Cache {

	@WebViewExposed
	public static void download(String url, String fileId, WebViewCallback callback) {
		if(CacheThread.isActive()) {
			callback.error(CacheError.FILE_ALREADY_CACHING);
			return;
		}

		if(!Device.isActiveNetworkConnected()) {
			callback.error(CacheError.NO_INTERNET);
			return;
		}

		CacheThread.download(url, fileIdToFilename(fileId));
		callback.invoke();
	}

	@WebViewExposed
	public static void stop(WebViewCallback callback) {
		if(!CacheThread.isActive()) {
			callback.error(CacheError.NOT_CACHING);
			return;
		}
		CacheThread.cancel();
		callback.invoke();
	}

	@WebViewExposed
	public static void isCaching(WebViewCallback callback) {
		callback.invoke(CacheThread.isActive());
	}

	@WebViewExposed
	public static void getFiles(WebViewCallback callback) {
		File[] fileList;
		File cacheDirectory = SdkProperties.getCacheDirectory();

		if (cacheDirectory == null)
			return;

		DeviceLog.debug("Unity Ads cache: checking app directory for Unity Ads cached files");
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.startsWith(SdkProperties.getCacheFilePrefix());
			}
		};

		fileList = cacheDirectory.listFiles(filter);

		if (fileList == null || fileList.length == 0) {
			callback.invoke(new JSONArray());
		}

		try {
			JSONArray files = new JSONArray();

			for(File f : fileList) {
				String name = f.getName().substring(SdkProperties.getCacheFilePrefix().length());
				DeviceLog.debug("Unity Ads cache: found " + name + ", " + f.length() + " bytes");
				files.put(getFileJson(name));
			}

			callback.invoke(files);
		}
		catch (JSONException e) {
			DeviceLog.exception("Error creating JSON", e);
			callback.error(CacheError.JSON_ERROR);
		}
	}

	@WebViewExposed
	public static void getFileInfo(String fileId, WebViewCallback callback) {
		try {
			JSONObject result = getFileJson(fileId);
			callback.invoke(result);
		}
		catch(JSONException e) {
			DeviceLog.exception("Error creating JSON", e);
			callback.error(CacheError.JSON_ERROR);
		}
	}

	@WebViewExposed
	public static void getFilePath(String fileId, WebViewCallback callback) {
		File f = new File(fileIdToFilename(fileId));
		if(f.exists()) {
			callback.invoke(fileIdToFilename(fileId));
		} else {
			callback.error(CacheError.FILE_NOT_FOUND);
		}
	}

	@WebViewExposed
	public static void deleteFile(String fileId, WebViewCallback callback) {
		File file = new File(fileIdToFilename(fileId));
		if(file.delete()) {
			callback.invoke();
		} else {
			callback.error(CacheError.FILE_IO_ERROR);
		}
	}

	@WebViewExposed
	public static void getHash(String fileId, WebViewCallback callback) {
		callback.invoke(Utilities.Sha256(fileId));
	}

	@WebViewExposed
	public static void setTimeouts(Integer connectTimeout, Integer readTimeout, WebViewCallback callback) {
		CacheThread.setConnectTimeout(connectTimeout);
		CacheThread.setReadTimeout(readTimeout);
		callback.invoke();
	}

	@WebViewExposed
	public static void getTimeouts(WebViewCallback callback) {
		callback.invoke(Integer.valueOf(CacheThread.getConnectTimeout()), Integer.valueOf(CacheThread.getReadTimeout()));
	}

	@WebViewExposed
	public static void setProgressInterval(Integer interval, WebViewCallback callback) {
		CacheThread.setProgressInterval(interval);
		callback.invoke();
	}

	@WebViewExposed
	public static void getProgressInterval(WebViewCallback callback) {
		callback.invoke(CacheThread.getProgressInterval());
	}

	@WebViewExposed
	public static void getFreeSpace(WebViewCallback callback) {
		callback.invoke(Device.getFreeSpace(SdkProperties.getCacheDirectory()));
	}

	@WebViewExposed
	public static void getTotalSpace(WebViewCallback callback) {
		callback.invoke(Device.getTotalSpace(SdkProperties.getCacheDirectory()));
	}

	private static String fileIdToFilename(String fileId) {
		return SdkProperties.getCacheDirectory() + "/" + SdkProperties.getCacheFilePrefix() + fileId;
	}

	private static JSONObject getFileJson(String fileId) throws JSONException {
		JSONObject fileJson = new JSONObject();
		fileJson.put("id", fileId);

		File f = new File(fileIdToFilename(fileId));

		if(f.exists()) {
			fileJson.put("found", true);
			fileJson.put("size", f.length());
			fileJson.put("mtime", f.lastModified());
		} else {
			fileJson.put("found", false);
		}

		return fileJson;
	}
}