package com.unity3d.ads.test;

import com.unity3d.ads.log.DeviceLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

public class TestUtilities {

	private static String testServerAddress = "http://10.0.2.2:8080";

	private static String convertStreamToString(InputStream is) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			sb.append(line);
		}
		reader.close();
		return sb.toString();
	}

	private static String getStringFromFile(String filePath) throws IOException {
		File fl = new File(filePath);
		FileInputStream fin = new FileInputStream(fl);
		String ret = convertStreamToString(fin);
		fin.close();
		return ret;
	}

	private static boolean exists(String filePath) {
		File fl = new File(filePath);
		return fl.exists() && fl.isFile();
	}

	public static String getTestServerAddress() {
		String address = testServerAddress;
		String testServerAddressPath = "/data/local/tmp/testServerAddress.txt";
		if (exists(testServerAddressPath)) {
			try {
				address = getStringFromFile(testServerAddressPath);
				if (address.isEmpty() || address.length() == 0) {
					address = testServerAddress;
				}
			} catch (IOException e) {
				DeviceLog.exception("Cannot read test server address from file. Path: " + testServerAddressPath, e);
			}
		}
		return address;
	}

	public static <T> T[] concat(T[] first, T[] second) {
		T[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

}
