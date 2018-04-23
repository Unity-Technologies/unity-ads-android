package com.unity3d.ads.cache;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import com.unity3d.ads.log.DeviceLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class CacheDirectory {
	private static final String TEST_FILE_NAME = "UnityAdsTest.txt";

	private String _cacheDirName;
	private boolean _initialized = false;
	private File _cacheDirectory = null;
	private CacheDirectoryType _type = null;

	public CacheDirectory(String cacheDirName) {
		_cacheDirName = cacheDirName;
	}

	public File getCacheDirectory(Context context) {
		if(_initialized) {
			return _cacheDirectory;
		} else {
			_initialized = true;

			if(Build.VERSION.SDK_INT > 18) {
				if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
					File externalCache = null;
					try {
						externalCache = createCacheDirectory(context.getExternalCacheDir(), _cacheDirName);
					}
					catch (Exception e) {
						DeviceLog.exception("Creating external cache directory failed", e);
					}

					if(testCacheDirectory(externalCache)) {
						_cacheDirectory = externalCache;
						_type = CacheDirectoryType.EXTERNAL;
						DeviceLog.debug("Unity Ads is using external cache directory: " + externalCache.getAbsolutePath());
						return _cacheDirectory;
					}
				} else {
					DeviceLog.debug("External media not mounted");
				}
			}

			File internalCache = context.getFilesDir();
			if(testCacheDirectory(internalCache)) {
				_cacheDirectory = internalCache;
				_type = CacheDirectoryType.INTERNAL;
				DeviceLog.debug("Unity Ads is using internal cache directory: " + internalCache.getAbsolutePath());
				return _cacheDirectory;
			}

			DeviceLog.error("Unity Ads failed to initialize cache directory");
			return null;
		}
	}

	public CacheDirectoryType getType() {
		return _type;
	}

	public File createCacheDirectory(File baseDir, String newDir) {
		if(baseDir == null) {
			return null;
		}

		File directory = new File(baseDir, newDir);

		directory.mkdirs();

		if(directory.isDirectory()) {
			return directory;
		} else {
			return null;
		}
	}

	public boolean testCacheDirectory(File directory) {
		if(directory == null || !directory.isDirectory()) {
			return false;
		}

		try {
			final byte[] inData = "test".getBytes("UTF-8");
			byte[] outData = new byte[inData.length];
			File testFile = new File(directory, TEST_FILE_NAME);

			FileOutputStream fos = new FileOutputStream(testFile);
			fos.write(inData);
			fos.flush();
			fos.close();

			FileInputStream fis = new FileInputStream(testFile);
			int readCount = fis.read(outData, 0, outData.length);
			fis.close();

			if(!testFile.delete()) {
				DeviceLog.debug("Failed to delete testfile " + testFile.getAbsoluteFile());
				return false;
			}

			if(readCount != outData.length) {
				DeviceLog.debug("Read buffer size mismatch");
				return false;
			}

			String result = new String(outData, "UTF-8");

			if(result.equals("test")) {
				return true;
			} else {
				DeviceLog.debug("Read buffer content mismatch");
				return false;
			}
		} catch(Exception e) {
			DeviceLog.debug("Unity Ads exception while testing cache directory " + directory.getAbsolutePath() + ": " + e.getMessage());
			return false;
		}
	}
}