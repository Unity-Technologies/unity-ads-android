package com.unity3d.ads.test.instrumentation.services.core.configuration;

import com.unity3d.services.core.configuration.PrivacyConfig;
import com.unity3d.services.core.configuration.PrivacyConfigStatus;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PrivacyConfigTest {
	@Test
	public void testPrivacyConfigDefault() {
		PrivacyConfig privacyConfig = new PrivacyConfig();
		Assert.assertEquals(PrivacyConfigStatus.UNKNOWN, privacyConfig.getPrivacyStatus());
		Assert.assertFalse(privacyConfig.allowedToSendPii());
	}

	@Test
	public void testPrivacyConfigWithPasTrue() throws JSONException {
		PrivacyConfig privacyConfig = new PrivacyConfig(new JSONObject("{\"pas\":true}"));
		Assert.assertEquals(PrivacyConfigStatus.ALLOWED, privacyConfig.getPrivacyStatus());
		Assert.assertTrue(privacyConfig.allowedToSendPii());
	}

	@Test
	public void testPrivacyConfigWithPasFalse() throws JSONException {
		PrivacyConfig privacyConfig = new PrivacyConfig(new JSONObject("{\"pas\":false}"));
		Assert.assertEquals(PrivacyConfigStatus.DENIED, privacyConfig.getPrivacyStatus());
		Assert.assertFalse(privacyConfig.allowedToSendPii());
	}

	@Test
	public void testPrivacyConfigWithPasInvalid() throws JSONException {
		PrivacyConfig privacyConfig = new PrivacyConfig(new JSONObject("{\"pas\":\"someThing\"}"));
		Assert.assertEquals(PrivacyConfigStatus.DENIED, privacyConfig.getPrivacyStatus());
		Assert.assertFalse(privacyConfig.allowedToSendPii());
	}

	@Test
	public void testPrivacyConfigWithExplicitStatus() {
		PrivacyConfig privacyConfig = new PrivacyConfig(PrivacyConfigStatus.ALLOWED);
		Assert.assertEquals(PrivacyConfigStatus.ALLOWED, privacyConfig.getPrivacyStatus());
		Assert.assertTrue(privacyConfig.allowedToSendPii());
	}
}
