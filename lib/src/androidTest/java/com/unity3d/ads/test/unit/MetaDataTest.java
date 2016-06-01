package com.unity3d.ads.test.unit;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.unity3d.ads.device.StorageManager;
import com.unity3d.ads.metadata.MediationMetaData;
import com.unity3d.ads.metadata.MetaData;
import com.unity3d.ads.metadata.PlayerMetaData;
import com.unity3d.ads.properties.ClientProperties;
import com.unity3d.ads.webview.WebViewApp;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
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

		HashMap<String, Object> params = (HashMap<String, Object>)((MetaDataWebApp)WebViewApp.getCurrentApp()).PARAMS[1];

		assertEquals("Metadata doesn't have correct amount of values", params.size(), 2 * 2);

		HashMap<String, Object> metadataEntries = (HashMap<String, Object>)metaData.getEntries();
		for (String k : params.keySet()) {
			assertTrue("Metadata doesn't contain key: " + k, metadataEntries.containsKey(k));
			assertEquals("Metadata key " + k + " doesn't contain value: " + params.get(k), metadataEntries.get(k), params.get(k));
		}
	}

	@Test
	public void testPlayerMetaData () throws Exception {
		WebViewApp.setCurrentApp(new MetaDataWebApp());
		PlayerMetaData metaData = new PlayerMetaData(ClientProperties.getApplicationContext());
		metaData.setServerId("bulbasaur");
		metaData.commit();

		HashMap<String, Object> params = (HashMap<String, Object>)((MetaDataWebApp)WebViewApp.getCurrentApp()).PARAMS[1];

		assertEquals("Metadata doesn't have correct amount of values", params.size(), 4 * 2);

		HashMap<String, Object> metadataEntries = (HashMap<String, Object>)metaData.getEntries();
		for (String k : params.keySet()) {
			assertTrue("Metadata doesn't contain key: " + k, metadataEntries.containsKey(k));
			assertEquals("Metadata key " + k + " doesn't contain value: " + params.get(k), metadataEntries.get(k), params.get(k));
		}
	}

	@Test
	public void testMetaDataBaseClassNoCategory () throws Exception {
		WebViewApp.setCurrentApp(new MetaDataWebApp());
		MetaData metaData = new MetaData(ClientProperties.getApplicationContext());
		metaData.set("test.one", 1);
		metaData.set("test.two", "2");
		metaData.set("test.three", 3.33f);
		metaData.commit();

		HashMap<String, Object> params = (HashMap<String, Object>)((MetaDataWebApp)WebViewApp.getCurrentApp()).PARAMS[1];

		assertEquals("Metadata doesn't have correct amount of values", params.size(), 3 * 2);

		HashMap<String, Object> metadataEntries = (HashMap<String, Object>)metaData.getEntries();
		for (String k : params.keySet()) {
			assertTrue("Metadata doesn't contain key: " + k, metadataEntries.containsKey(k));
			assertEquals("Metadata key " + k + " doesn't contain value: " + params.get(k), metadataEntries.get(k), params.get(k));
		}
	}

	@Test
	public void testMetaDataBaseClassWithCategory () throws Exception {
		WebViewApp.setCurrentApp(new MetaDataWebApp());
		MetaData metaData = new MetaData(ClientProperties.getApplicationContext());
		metaData.setCategory("test");
		metaData.set("one", 1);
		metaData.set("two", "2");
		metaData.set("three", 3.33f);
		metaData.commit();

		HashMap<String, Object> params = (HashMap<String, Object>)((MetaDataWebApp)WebViewApp.getCurrentApp()).PARAMS[1];

		assertEquals("Metadata doesn't have correct amount of values", params.size(), 3 * 2);

		HashMap<String, Object> metadataEntries = (HashMap<String, Object>)metaData.getEntries();
		for (String k : params.keySet()) {
			assertTrue("Metadata doesn't contain key: " + k, metadataEntries.containsKey(k));
			assertEquals("Metadata key " + k + " doesn't contain value: " + params.get(k), metadataEntries.get(k), params.get(k));
		}
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
