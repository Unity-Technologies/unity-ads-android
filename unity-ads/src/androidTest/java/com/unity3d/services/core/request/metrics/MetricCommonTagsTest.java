package com.unity3d.services.core.request.metrics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.unity3d.ads.metadata.MediationMetaData;
import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.configuration.Experiments;
import com.unity3d.services.core.configuration.PrivacyConfig;
import com.unity3d.services.core.configuration.PrivacyConfigStatus;
import com.unity3d.services.core.configuration.PrivacyConfigStorage;
import com.unity3d.services.core.device.StorageManager;
import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.services.core.webview.WebViewApp;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class MetricCommonTagsTest {

	@Mock
	WebViewApp _webviewapp;

	@BeforeClass
	public static void prepare () throws Exception {
		ClientProperties.setApplicationContext(InstrumentationRegistry.getInstrumentation().getTargetContext());
	}

	@After
	public void after () {
		if (StorageManager.getStorage(StorageManager.StorageType.PUBLIC) != null) {
			StorageManager.getStorage(StorageManager.StorageType.PUBLIC).clearStorage();
			StorageManager.getStorage(StorageManager.StorageType.PUBLIC).initStorage();
		}
	}

	@Test
	public void testCommonTagsAllMapped() {
		WebViewApp.setCurrentApp(_webviewapp);
		Map<String, String> currentTags;

		PrivacyConfigStorage.getInstance().setPrivacyConfig(new PrivacyConfig(PrivacyConfigStatus.ALLOWED));
		MetricCommonTags commonTags = new MetricCommonTags();

		MediationMetaData mediationMetaData = new MediationMetaData( ClientProperties.getApplicationContext() );
		mediationMetaData.setName("MediationNetwork");
		mediationMetaData.setVersion("123");
		mediationMetaData.set("adapter_version", "456");
		mediationMetaData.commit();

		currentTags = commonTags.asMap();

		assertNull("Incorrect metricSampleRate value", currentTags.get("msr"));
		assertEquals("Incorrect mediation name value", "MediationNetwork", currentTags.get("m_name"));
		assertEquals("Incorrect mediation version value", "123", currentTags.get("m_ver"));
		assertEquals("Incorrect mediation adapter value", "456", currentTags.get("m_ad_ver"));
		assertEquals("Incorrect privacy config value", "allowed", currentTags.get("prvc"));
	}

	@Test
	public void testRefreshMediationMetaDataCommonTags() {
		WebViewApp.setCurrentApp(_webviewapp);
		Map<String, String> currentTags;

		MetricCommonTags commonTags = new MetricCommonTags();

		currentTags = commonTags.asMap();

		assertNull("Incorrect mediation name value", currentTags.get("m_name"));
		assertNull("Incorrect mediation version value", currentTags.get("m_ver"));
		assertNull("Incorrect mediation adapter value", currentTags.get("m_ad_ver"));

		MediationMetaData mediationMetaData = new MediationMetaData( ClientProperties.getApplicationContext() );
		mediationMetaData.setName("MediationNetwork");
		mediationMetaData.setVersion("123");
		mediationMetaData.set("adapter_version", "456");
		mediationMetaData.commit();

		currentTags = commonTags.asMap();

		assertEquals("Incorrect mediation name value", "MediationNetwork", currentTags.get("m_name"));
		assertEquals("Incorrect mediation version value", "123", currentTags.get("m_ver"));
		assertEquals("Incorrect mediation adapter value", "456", currentTags.get("m_ad_ver"));
	}

	@Test
	public void testRefreshNullMediationMetaDataCommonTags() {
		WebViewApp.setCurrentApp(_webviewapp);
		Map<String, String> currentTags;

		MetricCommonTags commonTags = new MetricCommonTags();

		currentTags = commonTags.asMap();

		assertNull("Incorrect mediation name value", currentTags.get("m_name"));
		assertNull("Incorrect mediation version value", currentTags.get("m_ver"));
		assertNull("Incorrect mediation adapter value", currentTags.get("m_ad_ver"));

		MediationMetaData mediationMetaData = new MediationMetaData( ClientProperties.getApplicationContext() );
		mediationMetaData.setName(null);
		mediationMetaData.setVersion("123");
		mediationMetaData.set("adapter_version", "456");
		mediationMetaData.commit();

		currentTags = commonTags.asMap();

		assertNull("Incorrect mediation name value", currentTags.get("m_name"));
		assertEquals("Incorrect mediation version value", "123", currentTags.get("m_ver"));
		assertEquals("Incorrect mediation adapter value", "456", currentTags.get("m_ad_ver"));
	}

	@Test
	public void testEmptyConfigurationMetaDataCommonTags() {
		WebViewApp.setCurrentApp(_webviewapp);
		Map<String, String> currentTags;

		MetricCommonTags commonTags = new MetricCommonTags();

		currentTags = commonTags.asMap();

		assertNull("Incorrect experiment tag tsi value", currentTags.get("tsi"));
		assertNull("Incorrect experiment tag fff value", currentTags.get("fff"));
		assertNull("Incorrect experiment tag tsi_upii value", currentTags.get("tsi_upii"));
		assertNull("Incorrect experiment tag tsi_nt value", currentTags.get("tsi_nt"));
		assertNull("Incorrect experiment tag tsi_prr value", currentTags.get("tsi_prr"));
		assertNull("Incorrect experiment tag tsi_prw value", currentTags.get("tsi_prw"));
		assertNull("Incorrect experiment tag nwc value", currentTags.get("nwc"));
	}

	@Test
	public void testNonEmptyConfigurationMetaDataCommonTags() throws JSONException {
		Configuration configMock = Mockito.mock(Configuration.class);
		Mockito.when(configMock.getExperiments()).thenReturn(new Experiments(new JSONObject("{\"fff\":true}")));

		WebViewApp.setCurrentApp(_webviewapp);
		Map<String, String> currentTags;

		MetricCommonTags commonTags = new MetricCommonTags();
		commonTags.updateWithConfig(configMock);

		currentTags = commonTags.asMap();

		assertNull("Incorrect experiment tag tsi value",currentTags.get("tsi"));
		assertEquals("Incorrect experiment tag fff value","true", currentTags.get("fff"));
		assertNull("Incorrect experiment tag tsi_upii value", currentTags.get("tsi_upii"));
		assertNull("Incorrect experiment tag tsi_nt value", currentTags.get("tsi_nt"));
		assertNull("Incorrect experiment tag tsi_prr value", currentTags.get("tsi_prr"));
		assertNull("Incorrect experiment tag tsi_prw value", currentTags.get("tsi_prw"));
		assertNull("Incorrect experiment tag nwc value", currentTags.get("nwc"));
	}
}