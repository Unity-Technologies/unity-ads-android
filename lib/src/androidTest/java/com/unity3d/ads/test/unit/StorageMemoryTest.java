package com.unity3d.ads.test.unit;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.unity3d.ads.device.Storage;
import com.unity3d.ads.device.StorageManager;
import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.properties.ClientProperties;
import com.unity3d.ads.properties.SdkProperties;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class StorageMemoryTest {
	private static JSONObject _jsonObjectValue = null;
	private static JSONArray _jsonArrayValue = null;

	@BeforeClass
	public static void prepareStorageTests () throws Exception {
		_jsonObjectValue = new JSONObject("{\"testkey\":\"testvalue\"}");
		_jsonArrayValue = new JSONArray("[1, \"test1\"]");
		ClientProperties.setApplicationContext(InstrumentationRegistry.getTargetContext());
		File cacheDir = SdkProperties.getCacheDirectory(InstrumentationRegistry.getTargetContext());
		StorageManager.addStorageLocation(StorageManager.StorageType.PUBLIC, cacheDir.getAbsolutePath() + "/test.data");
		StorageManager.initStorage(StorageManager.StorageType.PUBLIC);
	}

	@Before
	public void clearStorage () {
		Storage s = StorageManager.getStorage(StorageManager.StorageType.PUBLIC);
		if (s != null) s.clearStorage();
		StorageManager.initStorage(StorageManager.StorageType.PUBLIC);
	}

	@Test
	public void testSetAndGetInteger () throws Exception {
		Storage s = StorageManager.getStorage(StorageManager.StorageType.PUBLIC);

		assertEquals(false, s.hasData());
		assertEquals(true, s.set("tests.integer", 12345));
		assertEquals(true, s.hasData());
		assertEquals(12345, s.get("tests.integer"));
	}


	@Test
	public void testSetAndGetBoolean () throws Exception {
		Storage s = StorageManager.getStorage(StorageManager.StorageType.PUBLIC);

		assertEquals("Storage should not have data", false, s.hasData());
		assertEquals("Setting data should have succeeded", true, s.set("tests.boolean", true));
		assertEquals("Storage should have data", true, s.hasData());
		assertEquals("Getting data should have succeeded", true, s.get("tests.boolean"));
	}

	@Test
	public void testSetAndGetLong () throws Exception {
		Storage s = StorageManager.getStorage(StorageManager.StorageType.PUBLIC);

		assertEquals(false, s.hasData());
		assertEquals(true, s.set("tests.long", 123451234512345L));
		assertEquals(true, s.hasData());
		assertEquals(123451234512345L, s.get("tests.long"));
	}

	@Test
	public void testSetAndGetDouble () throws Exception {
		Storage s = StorageManager.getStorage(StorageManager.StorageType.PUBLIC);

		assertEquals(false, s.hasData());
		assertEquals(true, s.set("tests.double", 12345.12345));
		assertEquals(true, s.hasData());
		assertEquals(12345.12345, s.get("tests.double"));
	}

	@Test
	public void testSetAndGetString () throws Exception {
		Storage s = StorageManager.getStorage(StorageManager.StorageType.PUBLIC);

		assertEquals(false, s.hasData());
		assertEquals(true, s.set("tests.string", "TestString"));
		assertEquals(true, s.hasData());
		assertEquals("TestString", s.get("tests.string"));
	}

	@Test
	public void testSetAndGetJSONObject () throws Exception {
		Storage s = StorageManager.getStorage(StorageManager.StorageType.PUBLIC);

		assertEquals(false, s.hasData());
		assertEquals(true, s.set("tests.jsonobject", _jsonObjectValue));
		assertEquals(true, s.hasData());
		assertEquals(_jsonObjectValue.toString(), s.get("tests.jsonobject").toString());
	}

	@Test
	public void testSetAndGetJSONArray () throws Exception {
		Storage s = StorageManager.getStorage(StorageManager.StorageType.PUBLIC);

		assertEquals(false, s.hasData());
		assertEquals(true, s.set("tests.jsonarray", _jsonArrayValue));
		assertEquals(true, s.hasData());
		assertEquals(_jsonArrayValue, s.get("tests.jsonarray"));
	}

	@Test
	public void testMultiLevelValue () throws Exception {
		Storage s = StorageManager.getStorage(StorageManager.StorageType.PUBLIC);

		assertEquals(false, s.hasData());
		assertEquals(true, s.set("level1.level2.level3.level4.level5", 12345));
		assertEquals(true, s.hasData());
		assertEquals(12345, s.get("level1.level2.level3.level4.level5"));
	}

	@Test
	public void testSingleLevelValue () throws Exception {
		Storage s = StorageManager.getStorage(StorageManager.StorageType.PUBLIC);

		assertEquals(false, s.hasData());
		assertEquals(true, s.set("level1", 12345));
		assertEquals(true, s.hasData());
		assertEquals(12345, s.get("level1"));
	}

	@Test
	public void testDelete () throws Exception {
		Storage s = StorageManager.getStorage(StorageManager.StorageType.PUBLIC);

		assertEquals(false, s.hasData());
		assertEquals(true, s.set("tests.deletethis", 12345));
		assertEquals(true, s.hasData());
		assertEquals(12345, s.get("tests.deletethis"));
		assertEquals(true, s.delete("tests.deletethis"));
		assertNull(s.get("tests.deletethis"));
		assertEquals(true, s.delete("tests"));
		assertEquals(false, s.hasData());
	}
}
