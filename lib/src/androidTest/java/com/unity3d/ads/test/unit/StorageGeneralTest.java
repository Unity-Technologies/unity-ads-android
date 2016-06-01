package com.unity3d.ads.test.unit;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.unity3d.ads.device.Storage;
import com.unity3d.ads.device.StorageManager;
import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.properties.ClientProperties;
import com.unity3d.ads.properties.SdkProperties;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class StorageGeneralTest {
	@BeforeClass
	public static void prepareStorageTests () throws Exception {
		StorageManager.removeStorage(StorageManager.StorageType.PRIVATE);
		StorageManager.removeStorage(StorageManager.StorageType.PUBLIC);
		ClientProperties.setApplicationContext(InstrumentationRegistry.getTargetContext());
		File cacheDir = SdkProperties.getCacheDirectory(InstrumentationRegistry.getTargetContext());
		StorageManager.addStorageLocation(StorageManager.StorageType.PUBLIC, cacheDir.getAbsolutePath() + "/test.data");
		StorageManager.initStorage(StorageManager.StorageType.PUBLIC);
	}

	@Before
	public void initStorage () throws Exception {
		Storage s = StorageManager.getStorage(StorageManager.StorageType.PUBLIC);
		if (s != null) s.clearStorage();
		StorageManager.initStorage(StorageManager.StorageType.PUBLIC);
	}

	@Test
	public void testNullKey () throws Exception {
		Storage s = StorageManager.getStorage(StorageManager.StorageType.PUBLIC);
		assertEquals("Storage should hold no data in the beginning of the test", false, s.hasData());
		assertEquals("Should not be able to set value with null key", false, s.set(null, 12345));
	}

	@Test
	public void testEmptyKey () throws Exception {
		Storage s = StorageManager.getStorage(StorageManager.StorageType.PUBLIC);
		assertEquals("Storage should hold no data in the beginning of the test", false, s.hasData());
		assertEquals("Should not be able to set value with an empty key", false, s.set("", 12345));
	}

	@Test
	public void testNullValue () throws Exception {
		Storage s = StorageManager.getStorage(StorageManager.StorageType.PUBLIC);
		assertEquals("Storage should hold no data in the beginning of the test", false, s.hasData());
		assertEquals("Should not be able to set null value", false, s.set("tests.properkey", null));
	}

	@Test
	public void testStorageManager () throws Exception {
		assertFalse("Private storage should not exist.", StorageManager.hasStorage(StorageManager.StorageType.PRIVATE));

		File cacheDir = SdkProperties.getCacheDirectory(InstrumentationRegistry.getTargetContext());
		StorageManager.addStorageLocation(StorageManager.StorageType.PRIVATE, cacheDir.getAbsolutePath() + "/private-test.data");
		StorageManager.initStorage(StorageManager.StorageType.PRIVATE);
		assertTrue("Private storage should exist now.", StorageManager.hasStorage(StorageManager.StorageType.PRIVATE));
	}

	@Test
	public void testGetKeys () throws Exception {
		Storage s = StorageManager.getStorage(StorageManager.StorageType.PUBLIC);
		assertEquals("Storage should hold no data in the beginning of the test", false, s.hasData());
		s.set("test.subtest.value1", 1);
		s.set("test.subtest.value2", 2);
		s.set("test.subtest.value3", 3);
		s.set("test.subtest.value4", 4);
		s.set("test.subtest.value5", 5);
		s.set("test.subtest.recursivetest.1", 1);
		s.set("test.subtest.recursivetest.2", 2);
		s.set("test.subtest.recursivetest.3", 3);
		s.set("test.subtest.recursivetest.even_deeper.subnode.1", 1);
		s.set("test.subtest.recursivetest.even_deeper.subnode.2", 2);
		s.set("test.subtest.recursivetest.even_deeper.subnode.3", 3);

		List<String> nonRecursiveKeys = s.getKeys("test.subtest", false);
		List<String> nonRecursiveCompareList = new ArrayList<>();
		nonRecursiveCompareList.add("value1");
		nonRecursiveCompareList.add("value2");
		nonRecursiveCompareList.add("value3");
		nonRecursiveCompareList.add("value4");
		nonRecursiveCompareList.add("value5");
		nonRecursiveCompareList.add("recursivetest");

		assertEquals("Compare list should be the same size as the actual nonRecursive keyList", nonRecursiveCompareList.size(), nonRecursiveKeys.size());

		for (int i = 0; i < nonRecursiveCompareList.size(); i++) {
			String removedKey = nonRecursiveKeys.remove(nonRecursiveKeys.indexOf(nonRecursiveCompareList.get(i)));
			assertEquals("Expected key was not found from storage", nonRecursiveCompareList.get(i), removedKey);
		}

		List<String> recursiveKeys = s.getKeys("test.subtest", true);
		List<String> recursiveCompareList = new ArrayList<>();
		recursiveCompareList.add("value1");
		recursiveCompareList.add("value2");
		recursiveCompareList.add("value3");
		recursiveCompareList.add("value4");
		recursiveCompareList.add("value5");
		recursiveCompareList.add("recursivetest");
		recursiveCompareList.add("recursivetest.1");
		recursiveCompareList.add("recursivetest.2");
		recursiveCompareList.add("recursivetest.3");
		recursiveCompareList.add("recursivetest.even_deeper");
		recursiveCompareList.add("recursivetest.even_deeper.subnode");
		recursiveCompareList.add("recursivetest.even_deeper.subnode.1");
		recursiveCompareList.add("recursivetest.even_deeper.subnode.2");
		recursiveCompareList.add("recursivetest.even_deeper.subnode.3");

		assertEquals("Compare list should be the same size as the actual recursive keyList", recursiveCompareList.size(), recursiveKeys.size());

		for (int i = 0; i < recursiveCompareList.size(); i++) {
			String removedKey = recursiveKeys.remove(recursiveKeys.indexOf(recursiveCompareList.get(i)));
			assertEquals("Expected key was not found from storage", recursiveCompareList.get(i), removedKey);
		}
	}
}
