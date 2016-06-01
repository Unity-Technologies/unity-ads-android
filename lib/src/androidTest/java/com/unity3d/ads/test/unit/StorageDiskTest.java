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

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class StorageDiskTest {
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
	public void initStorage () throws Exception {
		Storage s = StorageManager.getStorage(StorageManager.StorageType.PUBLIC);
		if (s != null) s.clearStorage();
		StorageManager.initStorage(StorageManager.StorageType.PUBLIC);
	}

	@Test
	public void testSetAndGetInteger () throws Exception {
		Storage s = StorageManager.getStorage(StorageManager.StorageType.PUBLIC);
		assertEquals("Storage should hold no data in the beginning of the test", false, s.hasData());
		assertEquals("Should have been able to set key \"tests.integer\" with value: 12345", true, s.set("tests.integer", 12345));
		assertEquals("Write storage should have succeeded", true, s.writeStorage());
		s.clearData();
		assertEquals("Storage was reset, should hold no data", false, s.hasData());
		assertEquals("Reading storage failed for some reason even though it should have succeeded", true, s.readStorage());
		assertEquals("Storage should contain data since one key with value was written into it", true, s.hasData());
		assertEquals("\"tests.integer\" key should hold 12345 but for some reason didn't", 12345, s.get("tests.integer"));
	}

	@Test
	public void testSetAndGetBoolean () throws Exception {
		Storage s = StorageManager.getStorage(StorageManager.StorageType.PUBLIC);

		assertEquals("Storage should hold no data in the beginning of the test", false, s.hasData());
		assertEquals("Should have been able to set key \"tests.boolean\" with value: true", true, s.set("tests.boolean", true));
		assertEquals("Write storage should have succeeded", true, s.writeStorage());
		s.clearData();
		assertEquals("Storage was reset, should hold no data", false, s.hasData());
		assertEquals("Reading storage failed for some reason even though it should have succeeded", true, s.readStorage());
		assertEquals("Storage should contain data since one key with value was written into it", true, s.hasData());
		assertEquals("\"tests.boolean\" key should hold \"true\" but for some reason didn't", true, s.get("tests.boolean"));
	}

	@Test
	public void testSetAndGetLong () throws Exception {
		Storage s = StorageManager.getStorage(StorageManager.StorageType.PUBLIC);

		assertEquals("Storage should hold no data in the beginning of the test", false, s.hasData());
		assertEquals("Should have been able to set key \"tests.long\" with value: 123451234512345", true, s.set("tests.long", 123451234512345L));
		assertEquals("Write storage should have succeeded", true, s.writeStorage());
		s.clearData();
		assertEquals("Storage was reset, should hold no data", false, s.hasData());
		assertEquals("Reading storage failed for some reason even though it should have succeeded", true, s.readStorage());
		assertEquals("Storage should contain data since one key with value was written into it", true, s.hasData());
		assertEquals("\"tests.long\" key should hold \"123451234512345\" but for some reason didn't", 123451234512345L, s.get("tests.long"));
	}

	@Test
	public void testSetAndGetDouble () throws Exception {
		Storage s = StorageManager.getStorage(StorageManager.StorageType.PUBLIC);

		assertEquals("Storage should hold no data in the beginning of the test", false, s.hasData());
		assertEquals("Should have been able to set key \"tests.double\" with value: 12345.12345", true, s.set("tests.double", 12345.12345));
		assertEquals("Write storage should have succeeded", true, s.writeStorage());
		s.clearData();
		assertEquals("Storage was reset, should hold no data", false, s.hasData());
		assertEquals("Reading storage failed for some reason even though it should have succeeded", true, s.readStorage());
		assertEquals("Storage should contain data since one key with value was written into it", true, s.hasData());
		assertEquals("\"tests.double\" key should hold \"12345.12345\" but for some reason didn't", 12345.12345, s.get("tests.double"));
	}

	@Test
	public void testSetAndGetString () throws Exception {
		Storage s = StorageManager.getStorage(StorageManager.StorageType.PUBLIC);

		assertEquals("Storage should hold no data in the beginning of the test", false, s.hasData());
		assertEquals("Should have been able to set key \"tests.string\" with value: TestString", true, s.set("tests.string", "TestString"));
		assertEquals("Write storage should have succeeded", true, s.writeStorage());
		s.clearData();
		assertEquals("Storage was reset, should hold no data", false, s.hasData());
		assertEquals("Reading storage failed for some reason even though it should have succeeded", true, s.readStorage());
		assertEquals("Storage should contain data since one key with value was written into it", true, s.hasData());
		assertEquals("\"tests.string\" key should hold \"TestString\" but for some reason didn't", "TestString", s.get("tests.string"));
	}

	@Test
	public void testSetAndGetJSONObject () throws Exception {
		Storage s = StorageManager.getStorage(StorageManager.StorageType.PUBLIC);

		assertEquals("Storage should hold no data in the beginning of the test", false, s.hasData());
		assertEquals("Should have been able to set key \"tests.jsonobject\" with value: " + _jsonObjectValue.toString(), true, s.set("tests.jsonobject", _jsonObjectValue));
		assertEquals("Write storage should have succeeded", true, s.writeStorage());
		s.clearData();
		assertEquals("Storage was reset, should hold no data", false, s.hasData());
		assertEquals("Reading storage failed for some reason even though it should have succeeded", true, s.readStorage());
		assertEquals("Storage should contain data since one key with value was written into it", true, s.hasData());
		assertEquals("\"tests.jsonobject\" key should hold \"" + _jsonObjectValue.toString() + "\" but for some reason didn't", _jsonObjectValue.toString(), s.get("tests.jsonobject").toString());
	}

	@Test
	public void testSetAndGetJSONArray () throws Exception {
		Storage s = StorageManager.getStorage(StorageManager.StorageType.PUBLIC);

		assertEquals("Storage should hold no data in the beginning of the test", false, s.hasData());
		assertEquals("Should have been able to set key \"tests.jsonarray\" with value: " + _jsonArrayValue.toString(), true, s.set("tests.jsonarray", _jsonArrayValue));
		assertEquals("Write storage should have succeeded", true, s.writeStorage());
		s.clearData();
		assertEquals("Storage was reset, should hold no data", false, s.hasData());
		assertEquals("Reading storage failed for some reason even though it should have succeeded", true, s.readStorage());
		assertEquals("Storage should contain data since one key with value was written into it", true, s.hasData());
		assertEquals("\"tests.jsonarray\" key should hold \"" + _jsonArrayValue.toString() + "\" but for some reason didn't", _jsonArrayValue, s.get("tests.jsonarray"));
	}

	@Test
	public void testWriteAndRead () throws Exception {
		Storage s = StorageManager.getStorage(StorageManager.StorageType.PUBLIC);

		assertEquals("Storage should hold no data in the beginning of the test", false, s.hasData());
		assertEquals("Should have been able to set key \"tests.random1\" with value: 12345", true, s.set("tests.random1", 12345));
		assertEquals("Should have been able to set key \"tests.random2\" with value: Testing", true, s.set("tests.random2", "Testing"));
		assertEquals("Should have been able to set key \"tests.random3\" with value: 12345.12345", true, s.set("tests.random3", 12345.12345));
		assertEquals("Write storage should have succeeded", true, s.writeStorage());
		s.clearData();
		assertEquals("Storage was reset, should hold no data", false, s.hasData());
		assertEquals("Reading storage failed for some reason even though it should have succeeded", true, s.readStorage());
		assertEquals("Storage should contain data since three keys with values were written into it", true, s.hasData());
		assertEquals("\"tests.random1\" key should contain: 12345, but didn't", 12345, s.get("tests.random1"));
		assertEquals("\"tests.random2\" key should contain: Testing, but didn't", "Testing", s.get("tests.random2"));
		assertEquals("\"tests.random3\" key should contain: 12345.12345, but didn't", 12345.12345, s.get("tests.random3"));
	}

	@Test
	public void testMultiLevelValue () throws Exception {
		Storage s = StorageManager.getStorage(StorageManager.StorageType.PUBLIC);

		assertEquals("Storage should hold no data in the beginning of the test", false, s.hasData());
		assertEquals("Should have been able to set key \"level1.level2.level3.level4.level5\" with value: 12345", true, s.set("level1.level2.level3.level4.level5", 12345));
		assertEquals("Write storage should have succeeded", true, s.writeStorage());
		s.clearData();
		assertEquals("Storage was reset, should hold no data", false, s.hasData());
		assertEquals("Reading storage failed for some reason even though it should have succeeded", true, s.readStorage());
		assertEquals("Storage should contain data since three keys with values were written into it", true, s.hasData());
		assertEquals("\"level1.level2.level3.level4.level5\" key should contain: 12345, but didn't", 12345, s.get("level1.level2.level3.level4.level5"));
	}

	@Test
	public void testSingleLevelValue () throws Exception {
		Storage s = StorageManager.getStorage(StorageManager.StorageType.PUBLIC);

		assertEquals("Storage should hold no data in the beginning of the test", false, s.hasData());
		assertEquals("Should have been able to set key \"level1\" with value: 12345", true, s.set("level1", 12345));
		assertEquals("Write storage should have succeeded", true, s.writeStorage());
		s.clearData();
		assertEquals("Storage was reset, should hold no data", false, s.hasData());
		assertEquals("Reading storage failed for some reason even though it should have succeeded", true, s.readStorage());
		assertEquals("Storage should contain data since three keys with values were written into it", true, s.hasData());
		assertEquals("\"level1\" key should contain: 12345, but didn't", 12345, s.get("level1"));
	}

	@Test
	public void testDelete () throws Exception {
		Storage s = StorageManager.getStorage(StorageManager.StorageType.PUBLIC);

		assertEquals("Storage should hold no data in the beginning of the test", false, s.hasData());
		assertEquals("Should have been able to set key \"tests.deletethis\" with value: 12345", true, s.set("tests.deletethis", 12345));
		assertEquals("Write storage should have succeeded", true, s.writeStorage());
		s.clearData();
		assertEquals("Storage was reset, should hold no data", false, s.hasData());
		assertEquals("Reading storage failed for some reason even though it should have succeeded", true, s.readStorage());
		assertEquals("\"tests.deletethis\" key should contain: 12345, but didn't", 12345, s.get("tests.deletethis"));
		assertEquals("Should've been able to delete the key \"tests.deletethis\", but for some reason couldn't", true, s.delete("tests.deletethis"));
		assertEquals("Write storage should have succeededm but didn't", true, s.writeStorage());
		s.clearData();
		assertEquals("Storage was reset, should hold no data", false, s.hasData());
		assertEquals("Reading storage failed for some reason even though it should have succeeded", true, s.readStorage());
		assertNull("Storage should be empty and \"tests.deletethis\" key should be empty, but for some reason it was containing data", s.get("tests.deletethis"));
	}
}
