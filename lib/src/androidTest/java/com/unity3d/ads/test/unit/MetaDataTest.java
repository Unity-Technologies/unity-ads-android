package com.unity3d.ads.test.unit;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.unity3d.ads.device.Storage;
import com.unity3d.ads.device.StorageManager;
import com.unity3d.ads.metadata.InAppPurchaseMetaData;
import com.unity3d.ads.metadata.MediationMetaData;
import com.unity3d.ads.metadata.MetaData;
import com.unity3d.ads.metadata.PlayerMetaData;
import com.unity3d.ads.properties.ClientProperties;
import com.unity3d.ads.webview.WebViewApp;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;

@RunWith(AndroidJUnit4.class)
public class MetaDataTest {
	@BeforeClass
	public static void prepare () throws Exception {
		ClientProperties.setApplicationContext(InstrumentationRegistry.getTargetContext());
	}

	@After
	public void after () {
		StorageManager.getStorage(StorageManager.StorageType.PUBLIC).clearStorage();
		StorageManager.getStorage(StorageManager.StorageType.PUBLIC).initStorage();
	}

	@Test
	public void testMediationMetaData () throws Exception {
		WebViewApp.setCurrentApp(new MetaDataWebApp());
		MediationMetaData metaData = new MediationMetaData(ClientProperties.getApplicationContext());
		metaData.setName("MediationNetwork");
		metaData.setOrdinal(1);
		metaData.commit();

		JSONObject values = (JSONObject)((MetaDataWebApp)WebViewApp.getCurrentApp()).PARAMS[1];
		JSONObject mediationObject = values.getJSONObject("mediation");

		JSONObject nameObject = mediationObject.getJSONObject("name");
		assertEquals("Incorrect name value", nameObject.getString("value"), "MediationNetwork");
		assertNotNull("Timestamp for name is null", nameObject.getLong("ts"));

		JSONObject ordinalObject = mediationObject.getJSONObject("ordinal");
		assertEquals("Incorrect ordinal value", ordinalObject.getInt("value"), 1);
		assertNotNull("Timestamp for ordinal is null", ordinalObject.getLong("ts"));
	}

	@Test
	public void testPlayerMetaData () throws Exception {
		WebViewApp.setCurrentApp(new MetaDataWebApp());
		PlayerMetaData metaData = new PlayerMetaData(ClientProperties.getApplicationContext());
		metaData.setServerId("bulbasaur");

		metaData.commit();
		JSONObject values = (JSONObject)((MetaDataWebApp)WebViewApp.getCurrentApp()).PARAMS[1];
		JSONObject playerObject = values.getJSONObject("player");
		JSONObject serverIdObject = playerObject.getJSONObject("server_id");

		assertEquals("Incorrect server_id value", serverIdObject.getString("value"), "bulbasaur");
		assertNotNull("Timestamp for server_id is null", serverIdObject.getLong("ts"));
	}

	@Test
	public void testMetaDataBaseClassNoCategory () throws Exception {
		WebViewApp.setCurrentApp(new MetaDataWebApp());
		MetaData metaData = new MetaData(ClientProperties.getApplicationContext());
		metaData.set("test.one", 1);
		metaData.set("test.two", "2");
		metaData.set("test.three", 123.123);
		metaData.set("testNumber", 12345);
		metaData.commit();

		JSONObject values = (JSONObject)((MetaDataWebApp)WebViewApp.getCurrentApp()).PARAMS[1];
		JSONObject testObject = values.getJSONObject("test");

		JSONObject oneObject = testObject.getJSONObject("one");
		assertEquals("Incorrect 'one' value", 1, oneObject.getInt("value"));
		assertNotNull("Timestamp for 'one' is null", oneObject.getLong("ts"));

		JSONObject twoObject = testObject.getJSONObject("two");
		assertEquals("Incorrect 'two' value", "2", twoObject.getString("value"));
		assertNotNull("Timestamp for 'two' is null", twoObject.getLong("ts"));

		JSONObject threeObject = testObject.getJSONObject("three");
		assertEquals("Incorrect 'three' value", threeObject.getDouble("value"), 123.123, 0);
		assertNotNull("Timestamp for 'three' is null", threeObject.getLong("ts"));

		JSONObject testNumberObject = values.getJSONObject("testNumber");
		assertEquals("Incorrect 'testNumber' value", 12345, testNumberObject.getInt("value"));
		assertNotNull("Timestamp for 'testNumber' is null", testNumberObject.getLong("ts"));

		MetaData metaData2 = new MetaData(ClientProperties.getApplicationContext());
		metaData2.set("testNumber", 23456);
		metaData2.commit();

		JSONObject values2 = (JSONObject)((MetaDataWebApp)WebViewApp.getCurrentApp()).PARAMS[1];
		JSONObject testNumberObject2 = values2.getJSONObject("testNumber");
		assertEquals("Incorrect 'testNumber' second set value", 23456, testNumberObject2.getInt("value"));
		assertNotNull("Timestamp for 'testNumber' second set is null", testNumberObject2.getLong("ts"));

		Storage storage = StorageManager.getStorage(StorageManager.StorageType.PUBLIC);
		storage.clearData();
		storage.readStorage();

		JSONObject keysFromStorage = (JSONObject)storage.get("test");
		JSONObject storageOneObject = keysFromStorage.getJSONObject("one");

		assertEquals("Incorrect 'one' value", storageOneObject.getInt("value"), 1);
		assertNotNull("Timestamp for 'one' is null", storageOneObject.getLong("ts"));

		JSONObject storageTwoObject = keysFromStorage.getJSONObject("two");
		assertEquals("Incorrect 'two' value", storageTwoObject.getString("value"), "2");
		assertNotNull("Timestamp for 'two' is null", storageTwoObject.getLong("ts"));

		JSONObject storageThreeObject = keysFromStorage.getJSONObject("three");
		assertEquals("Incorrect 'three' value", storageThreeObject.getDouble("value"), 123.123, 0);
		assertNotNull("Timestamp for 'three' is null", storageThreeObject.getLong("ts"));

		JSONObject storageTestNumberObject2 = (JSONObject)storage.get("testNumber");
		assertEquals("Incorrect 'testNumber' second set value", 23456, storageTestNumberObject2.getInt("value"));
		assertNotNull("Timestamp for 'testNumber' second set is null", storageTestNumberObject2.getLong("ts"));
	}

	@Test
	public void testMetaDataBaseClassNoCategoryDiskWrite () throws Exception {
		MetaData metaData = new MetaData(ClientProperties.getApplicationContext());
		metaData.set("test.one", 1);
		metaData.set("test.two", "2");
		metaData.set("test.three", 123.123);
		metaData.set("testNumber", 12345);
		metaData.commit();

		MetaData metaData2 = new MetaData(ClientProperties.getApplicationContext());
		metaData2.set("testNumber", 23456);
		metaData2.set("test.four", 4);
		metaData2.commit();

		Storage storage = StorageManager.getStorage(StorageManager.StorageType.PUBLIC);
		storage.clearData();
		storage.readStorage();

		JSONObject keysFromStorage = (JSONObject)storage.get("test");

		JSONObject storageFourObject = keysFromStorage.getJSONObject("four");
		assertEquals("Incorrect 'four' value", storageFourObject.getInt("value"), 4);
		assertNotNull("Timestamp for 'three' is null", storageFourObject.getLong("ts"));

		JSONObject storageOneObject = keysFromStorage.getJSONObject("one");
		assertEquals("Incorrect 'one' value", storageOneObject.getInt("value"), 1);
		assertNotNull("Timestamp for 'one' is null", storageOneObject.getLong("ts"));

		JSONObject storageTwoObject = keysFromStorage.getJSONObject("two");
		assertEquals("Incorrect 'two' value", storageTwoObject.getString("value"), "2");
		assertNotNull("Timestamp for 'two' is null", storageTwoObject.getLong("ts"));

		JSONObject storageThreeObject = keysFromStorage.getJSONObject("three");
		assertEquals("Incorrect 'three' value", storageThreeObject.getDouble("value"), 123.123, 0);
		assertNotNull("Timestamp for 'three' is null", storageThreeObject.getLong("ts"));

		JSONObject storageTestNumberObject2 = (JSONObject)storage.get("testNumber");
		assertEquals("Incorrect 'testNumber' second set value", 23456, storageTestNumberObject2.getInt("value"));
		assertNotNull("Timestamp for 'testNumber' second set is null", storageTestNumberObject2.getLong("ts"));
	}

	@Test
	public void testMetaDataBaseClassWithCategory () throws Exception {
		WebViewApp.setCurrentApp(new MetaDataWebApp());
		MetaData metaData = new MetaData(ClientProperties.getApplicationContext());
		metaData.setCategory("test");
		metaData.set("one", 1);
		metaData.set("two", "2");
		metaData.set("three", 123.123);
		metaData.commit();

		JSONObject values = (JSONObject)((MetaDataWebApp)WebViewApp.getCurrentApp()).PARAMS[1];
		JSONObject testObject = values.getJSONObject("test");

		JSONObject oneObject = testObject.getJSONObject("one");
		assertEquals("Incorrect 'one' value", oneObject.getInt("value"), 1);
		assertNotNull("Timestamp for 'one' is null", oneObject.getLong("ts"));

		JSONObject twoObject = testObject.getJSONObject("two");
		assertEquals("Incorrect 'two' value", twoObject.getString("value"), "2");
		assertNotNull("Timestamp for 'two' is null", twoObject.getLong("ts"));

		JSONObject threeObject = testObject.getJSONObject("three");
		assertEquals("Incorrect 'three' value", threeObject.getDouble("value"), 123.123, 0);
		assertNotNull("Timestamp for 'three' is null", threeObject.getLong("ts"));

		Storage storage = StorageManager.getStorage(StorageManager.StorageType.PUBLIC);
		storage.clearData();
		storage.readStorage();

		JSONObject keysFromStorage = (JSONObject)storage.get("test");
		JSONObject storageOneObject = keysFromStorage.getJSONObject("one");

		assertEquals("Incorrect 'one' value", storageOneObject.getInt("value"), 1);
		assertNotNull("Timestamp for 'one' is null", storageOneObject.getLong("ts"));

		JSONObject storageTwoObject = keysFromStorage.getJSONObject("two");
		assertEquals("Incorrect 'two' value", storageTwoObject.getString("value"), "2");
		assertNotNull("Timestamp for 'two' is null", storageTwoObject.getLong("ts"));

		JSONObject storageThreeObject = keysFromStorage.getJSONObject("three");
		assertEquals("Incorrect 'three' value", storageThreeObject.getDouble("value"), 123.123, 0);
		assertNotNull("Timestamp for 'three' is null", storageThreeObject.getLong("ts"));
	}

	@Test
	public void testCommitWithoutMetaDataSet () throws Exception {
		WebViewApp.setCurrentApp(new MetaDataWebApp());
		MetaData metaData = new MetaData(ClientProperties.getApplicationContext());
		metaData.setCategory("test");
		metaData.commit();

		assertNull("Entries should still be null", metaData.getData());
	}

	@Test
	public void testInAppPurhcaseMetaData () throws Exception {
		WebViewApp.setCurrentApp(new MetaDataWebApp());
		InAppPurchaseMetaData metaData = new InAppPurchaseMetaData(ClientProperties.getApplicationContext());
		metaData.setCurrency("EUR");
		metaData.setProductId("testProductId1");
		metaData.setPrice(1.25);
		metaData.setReceiptPurchaseData("receiptPurchaseData1");
		metaData.setSignature("testSignature1");
		metaData.commit();

		InAppPurchaseMetaData metaData2 = new InAppPurchaseMetaData(ClientProperties.getApplicationContext());
		metaData2.setCurrency("USD");
		metaData2.setProductId("testProductId2");
		metaData2.setPrice(2.25);
		metaData2.setReceiptPurchaseData("receiptPurchaseData2");
		metaData2.setSignature("testSignature2");
		metaData2.commit();

		JSONArray purchases = (JSONArray)((MetaDataWebApp)WebViewApp.getCurrentApp()).PARAMS[1];
		JSONObject purchase1 = purchases.getJSONObject(0);
		JSONObject purchase2 = purchases.getJSONObject(1);

		assertEquals("Incorrect purchase1 value for currency", "EUR", purchase1.getString(InAppPurchaseMetaData.KEY_CURRENCY));
		assertEquals("Incorrect purchase1 value for productId", "testProductId1", purchase1.getString(InAppPurchaseMetaData.KEY_PRODUCT_ID));
		assertEquals("Incorrect purchase1 value for price", 1.25, purchase1.getDouble(InAppPurchaseMetaData.KEY_PRICE), 0);
		assertEquals("Incorrect purchase1 value for receiptPurchaseData", "receiptPurchaseData1", purchase1.getString(InAppPurchaseMetaData.KEY_RECEIPT_PURCHASE_DATA));
		assertEquals("Incorrect purchase1 value for signature", "testSignature1", purchase1.getString(InAppPurchaseMetaData.KEY_SIGNATURE));
		assertNotNull("Timestamp is null for purchase1", purchase1.getLong("ts"));

		assertEquals("Incorrect purchase2 value for currency", "USD", purchase2.getString(InAppPurchaseMetaData.KEY_CURRENCY));
		assertEquals("Incorrect purchase2 value for productId", "testProductId2", purchase2.getString(InAppPurchaseMetaData.KEY_PRODUCT_ID));
		assertEquals("Incorrect purchase2 value for price", 2.25, purchase2.getDouble(InAppPurchaseMetaData.KEY_PRICE), 0);
		assertEquals("Incorrect purchase2 value for receiptPurchaseData", "receiptPurchaseData2", purchase2.getString(InAppPurchaseMetaData.KEY_RECEIPT_PURCHASE_DATA));
		assertEquals("Incorrect purchase2 value for signature", "testSignature2", purchase2.getString(InAppPurchaseMetaData.KEY_SIGNATURE));
		assertNotNull("Timestamp is null for purchase2", purchase2.getLong("ts"));
	}

	private class MetaDataWebApp extends WebViewApp {
		public Object[] PARAMS = null;
		public Enum EVENT_CATEOGRY = null;
		public Enum EVENT_ID = null;

		@Override
		public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
			PARAMS = params;
			EVENT_CATEOGRY = eventCategory;
			EVENT_ID = eventId;
			return true;
		}
	}
}
