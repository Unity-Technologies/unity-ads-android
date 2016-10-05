package com.wds.ads.misc;

import android.os.Handler;
import android.os.Looper;

import com.wds.ads.log.DeviceLog;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

	public static String readFile (File fileToRead) {
		if (fileToRead == null) {
			return null;
		}

		String fileContent = "";
		BufferedReader br = null;
		FileReader fr = null;

		if (fileToRead.exists() && fileToRead.canRead()) {
			try {
				fr = new FileReader(fileToRead);
				br = new BufferedReader(fr);
				String line;

				while ((line = br.readLine()) != null) {
					fileContent = fileContent.concat(line);
				}
			}
			catch (Exception e) {
				DeviceLog.exception("Problem reading file", e);
				fileContent = null;
			}
			try {
				if (br != null) {
					br.close();
				}
			}
			catch (Exception e) {
				DeviceLog.exception("Couldn't close BufferedReader", e);
			}
			try {
				if (fr != null) {
					fr.close();
				}
			}
			catch (Exception e) {
				DeviceLog.exception("Couldn't close FileReader", e);
			}

			return fileContent;
		}
		else {
			DeviceLog.error("File did not exist or couldn't be read");
		}

		return null;
	}

	public static byte[] readFileBytes(File file) throws IOException {
		if (file == null) {
			return null;
		}

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		InputStream inputStream = new FileInputStream(file);
		byte[] buffer = new byte[4096];
		int read;

		while ((read = inputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, read);
		}

		outputStream.close();
		inputStream.close();

		return outputStream.toByteArray();
	}
}
