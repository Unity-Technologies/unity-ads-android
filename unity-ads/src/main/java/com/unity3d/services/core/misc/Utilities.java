package com.unity3d.services.core.misc;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.unity3d.services.core.log.DeviceLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;

public class Utilities {
	public static void runOnUiThread(Runnable runnable) {
		runOnUiThread(runnable, 0);
	}

	public static void runOnUiThread(Runnable runnable, long delay) {
		Handler handler = new Handler(Looper.getMainLooper());

		if (delay > 0) {
			handler.postDelayed(runnable, delay);
		} else {
			handler.post(runnable);
		}
	}

	public static String Sha256(String input) {
		return Sha256(input.getBytes());
	}

	public static String Sha256(byte[] input) {
		if (input == null) {
			return null;
		}

		MessageDigest m;
		try {
			m = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			DeviceLog.exception("SHA-256 algorithm not found", e);
			return null;
		}

		m.update(input, 0, input.length);

		return toHexString(m.digest());
	}

	public static String Sha256(InputStream inputStream) throws IOException {
		if (inputStream == null) {
			return null;
		}

		MessageDigest m;
		try {
			m = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			DeviceLog.exception("SHA-256 algorithm not found", e);
			return null;
		}

		byte[] buffer = new byte[4096];
		int read;

		while ((read = inputStream.read(buffer)) != -1) {
			m.update(buffer, 0, read);
		}

		return toHexString(m.digest());
	}

	public static String toHexString(byte[] array) {
		String output = "";
		for (byte rawByte : array) {
			// convert to int (avoid sign issues)
			int b = 0xFF & rawByte;

			// if it is a single digit, make sure it have 0 in front (proper padding)
			if (b <= 0xF) {
				output += "0";
			}
			// add number to string
			output += Integer.toHexString(b);
		}
		// hex string to uppercase
		return output;
	}

	public static boolean writeFile (File fileToWrite, String content) {
		if (fileToWrite == null) {
			return false;
		}

		FileOutputStream fos = null;
		boolean success = true;

		try {
			fos = new FileOutputStream(fileToWrite);
			fos.write(content.getBytes());
			fos.flush();
		}
		catch (Exception e) {
			success = false;
			DeviceLog.exception("Could not write file", e);
		}
		finally {
			try {
				if (fos != null)
					fos.close();
			}
			catch (Exception e) {
				DeviceLog.exception("Error closing FileOutputStream", e);
			}
		}

		if (success) {
			DeviceLog.debug("Wrote file: " + fileToWrite.getAbsolutePath());
		}

		return success;
	}

	public static byte[] readFileBytes(File file) throws IOException {

		if (file == null || !file.exists()) {
			return null;
		}

		// If Device API Level is 26+, utilize built in functionality
		if (Build.VERSION.SDK_INT >= 26) {
			return Files.readAllBytes(file.toPath());
		}

		InputStream inputStream = null;
		byte[] buffer;

		try {
			inputStream = new FileInputStream(file);
			buffer = new byte[(int) file.length()];
			int readBytes = inputStream.read(buffer);
			if (readBytes != buffer.length) {
				throw new IOException("Failed to read all bytes from input file path: " + file.getPath());
			}
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}
		return buffer;
	}

	public static JSONObject mergeJsonObjects(JSONObject primary, JSONObject secondary) throws JSONException {
		if (primary == null) {
			return secondary;
		}
		if (secondary == null) {
			return primary;
		}

		JSONObject newJsonObject = new JSONObject();
		Iterator<String> keys = secondary.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			newJsonObject.put(key, secondary.get(key));
		}

		keys = primary.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			if (newJsonObject.has(key) && newJsonObject.get(key) instanceof JSONObject && primary.get(key) instanceof JSONObject) {
				newJsonObject.put(key, mergeJsonObjects(primary.getJSONObject(key), newJsonObject.getJSONObject(key)));
			}
			else {
				newJsonObject.put(key, primary.get(key));
			}
		}

		return newJsonObject;
	}
}
