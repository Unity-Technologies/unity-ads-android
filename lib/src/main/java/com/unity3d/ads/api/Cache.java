package com.unity3d.ads.api;

import android.annotation.TargetApi;
import android.media.MediaMetadataRetriever;
import android.util.Base64;
import android.util.SparseArray;

import com.unity3d.ads.cache.CacheDirectory;
import com.unity3d.ads.cache.CacheDirectoryType;
import com.unity3d.ads.cache.CacheError;
import com.unity3d.ads.cache.CacheThread;
import com.unity3d.ads.device.Device;
import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.misc.Utilities;
import com.unity3d.ads.properties.ClientProperties;
import com.unity3d.ads.properties.SdkProperties;
import com.unity3d.ads.request.WebRequestError;
import com.unity3d.ads.webview.bridge.WebViewCallback;
import com.unity3d.ads.webview.bridge.WebViewExposed;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;

public class Cache {

	@WebViewExposed
	public static void download(String url, String fileId, JSONArray headers, Boolean append, WebViewCallback callback) {
		if(CacheThread.isActive()) {
			callback.error(CacheError.FILE_ALREADY_CACHING);
			return;
		}

		if(!Device.isActiveNetworkConnected()) {
			callback.error(CacheError.NO_INTERNET);
			return;
		}

		HashMap<String, List<String>> mappedHeaders;
		try {
			mappedHeaders = Request.getHeadersMap(headers);
		}
		catch (Exception e) {
			DeviceLog.exception("Error mapping headers for the request", e);
			callback.error(WebRequestError.MAPPING_HEADERS_FAILED, url, fileId);
			return;
		}

		CacheThread.download(url, fileIdToFilename(fileId), mappedHeaders, append);
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
	public static void getFileContent(String fileId, String encoding, WebViewCallback callback) {
		String fileName = fileIdToFilename(fileId);
		File f = new File(fileName);

		if (!f.exists()) {
			callback.error(CacheError.FILE_NOT_FOUND, fileId, fileName);
			return;
		}
		byte[] byteData;

		try {
			byteData = Utilities.readFileBytes(f);
		}
		catch (IOException e) {
			callback.error(CacheError.FILE_IO_ERROR, fileId, fileName, e.getMessage() + ", " + e.getClass().getName());
			return;
		}

		String fileContents;
		if (encoding == null) {
			callback.error(CacheError.UNSUPPORTED_ENCODING, fileId, fileName, encoding);
			return;
		}

		if (encoding.equals("UTF-8")) {
			ByteBuffer buf = ByteBuffer.wrap(byteData);
			CharBuffer charbuffer = Charset.forName("UTF-8").decode(buf);
			fileContents = charbuffer.toString();
		}
		else if (encoding.equals("Base64")) {
			fileContents = Base64.encodeToString(byteData, Base64.NO_WRAP);
		}
		else {
			callback.error(CacheError.UNSUPPORTED_ENCODING, fileId, fileName, encoding);
			return;
		}
		callback.invoke(fileContents);
	}

	@WebViewExposed
	public static void setFileContent(String fileId, String encoding, String content, WebViewCallback callback) {
		String targetFileName = fileIdToFilename(fileId);
		FileOutputStream fileOutput = null;
		byte[] fileContents;
		boolean success = false;

		try {
			fileContents = content.getBytes("UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			callback.error(CacheError.UNSUPPORTED_ENCODING, fileId, targetFileName, encoding);
			return;
		}

		if (encoding != null && encoding.length() > 0) {
			if (encoding.equals("Base64")) {
				fileContents = Base64.decode(content, Base64.NO_WRAP);
			}
			else if (encoding.equals("UTF-8")) {
				// UTF-8 handled by default
			}
			else {
				callback.error(CacheError.UNSUPPORTED_ENCODING, fileId, targetFileName, encoding);
				return;
			}
		}

		try {
			fileOutput = new FileOutputStream(targetFileName);
			fileOutput.write(fileContents);
			fileOutput.flush();
			success = true;
		}
		catch (FileNotFoundException e) {
			callback.error(CacheError.FILE_NOT_FOUND, fileId, targetFileName, encoding);
		}
		catch (IOException e) {
			callback.error(CacheError.FILE_IO_ERROR, fileId, targetFileName, encoding);
		}
		finally {
			try {
				if (fileOutput != null)
					fileOutput.close();
			}
			catch (Exception e) {
				DeviceLog.exception("Error closing FileOutputStream", e);
			}
		}
		if (success) {
			callback.invoke();
		}
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

	@WebViewExposed
	public static void getMetaData (final String fileId, JSONArray requestedMetaDatas, final WebViewCallback callback) {
		SparseArray<String> returnValues;
		String videoFile = fileIdToFilename(fileId);

		try {
			returnValues = getMetaData(videoFile, requestedMetaDatas);
		}
		catch (JSONException e) {
			callback.error(CacheError.JSON_ERROR, e.getMessage());
			return;
		}
		catch (RuntimeException e) {
			callback.error(CacheError.INVALID_ARGUMENT, e.getMessage());
			return;
		}
		catch (IOException e) {
			callback.error(CacheError.FILE_IO_ERROR, e.getMessage());
			return;
		}

		JSONArray returnJsonArray = new JSONArray();

		for (int i = 0; i < returnValues.size(); i++) {
			JSONArray entryJsonArray = new JSONArray();
			entryJsonArray.put(returnValues.keyAt(i));
			entryJsonArray.put(returnValues.valueAt(i));
			returnJsonArray.put(entryJsonArray);
		}

		callback.invoke(returnJsonArray);
	}

	@WebViewExposed
	public static void getCacheDirectoryType (final WebViewCallback callback) {
		CacheDirectory cacheDir = SdkProperties.getCacheDirectoryObject();
		if (cacheDir == null || cacheDir.getCacheDirectory(ClientProperties.getApplicationContext()) == null) {
			callback.error(CacheError.CACHE_DIRECTORY_NULL);
			return;
		}

		if (!cacheDir.getCacheDirectory(ClientProperties.getApplicationContext()).exists()) {
			callback.error(CacheError.CACHE_DIRECTORY_DOESNT_EXIST);
			return;
		}

		CacheDirectoryType cacheDirType = cacheDir.getType();

		if (cacheDirType == null) {
			callback.error(CacheError.CACHE_DIRECTORY_TYPE_NULL);
			return;
		}

		callback.invoke(cacheDirType.name());
	}

	@WebViewExposed
	public static void getCacheDirectoryExists (final WebViewCallback callback) {
		File cacheDir = SdkProperties.getCacheDirectory();
		if (cacheDir == null) {
			callback.error(CacheError.CACHE_DIRECTORY_NULL);
			return;
		}

		callback.invoke(cacheDir.exists());
	}

	@WebViewExposed
	public static void recreateCacheDirectory (final WebViewCallback callback) {
		File cacheDir = SdkProperties.getCacheDirectory();
		if (cacheDir.exists()) {
			callback.error(CacheError.CACHE_DIRECTORY_EXISTS);
			return;
		}

		SdkProperties.setCacheDirectory(null);
		cacheDir = SdkProperties.getCacheDirectory();
		if (cacheDir == null) {
			callback.error(CacheError.CACHE_DIRECTORY_NULL);
			return;
		}

		callback.invoke();
	}

	@TargetApi(10)
	private static SparseArray<String> getMetaData (final String videoFile, JSONArray requestedMetaDatas) throws JSONException, IOException, RuntimeException {
		File f = new File(videoFile);
		SparseArray<String> returnArray = new SparseArray<>();

		if (f.exists()) {
			MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
			metadataRetriever.setDataSource(f.getAbsolutePath());

			for (int i = 0; i < requestedMetaDatas.length(); i++) {
				int metaDataKey = requestedMetaDatas.getInt(i);
				String metaDataValue = metadataRetriever.extractMetadata(metaDataKey);

				if (metaDataValue != null) {
					returnArray.put(metaDataKey, metaDataValue);
				}
			}
		}
		else {
			throw new IOException("File: " + f.getAbsolutePath() + " doesn't exist");
		}

		return returnArray;
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