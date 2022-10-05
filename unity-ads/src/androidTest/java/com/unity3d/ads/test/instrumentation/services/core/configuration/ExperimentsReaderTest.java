package com.unity3d.ads.test.instrumentation.services.core.configuration;

import com.unity3d.services.core.configuration.Experiments;
import com.unity3d.services.core.configuration.ExperimentsReader;
import com.unity3d.services.core.configuration.IExperiments;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExperimentsReaderTest {

	@Mock
	private Experiments _localExperimentsMock;

	@Mock
	private Experiments _remoteExperimentsMock;

	@Test
	public void testExperimentsReaderWithDefaults() {
		ExperimentsReader experimentsReader = new ExperimentsReader();
		validateDefaultExperiments(experimentsReader.getCurrentlyActiveExperiments());
	}

	@Test
	public void testExperimentsReaderWithLocalOnly() {
		ExperimentsReader experimentsReader = new ExperimentsReader();
		experimentsReader.updateLocalExperiments(_localExperimentsMock);
		Mockito.when(_localExperimentsMock.isTwoStageInitializationEnabled()).thenReturn(true);
		Assert.assertEquals(_localExperimentsMock, experimentsReader.getCurrentlyActiveExperiments());
		validateDefaultExperiments(experimentsReader.getCurrentlyActiveExperiments());
	}

	@Test
	public void testExperimentsReaderWithRemoteOnly() throws JSONException {
		Mockito.when(_remoteExperimentsMock.getNextSessionExperiments()).thenReturn(new JSONObject("{\"tsi_upii\":true}"));
		Mockito.when(_remoteExperimentsMock.getCurrentSessionExperiments()).thenReturn(new JSONObject("{\"fff\":true}"));

		ExperimentsReader experimentsReader = new ExperimentsReader();
		experimentsReader.updateRemoteExperiments(_remoteExperimentsMock);

		IExperiments resultExperiments = experimentsReader.getCurrentlyActiveExperiments();
		Assert.assertFalse("Expected TSI_UPII flag to be false", resultExperiments.isUpdatePiiFields());
		Assert.assertTrue("Expected FFF flag to be true", resultExperiments.isForwardExperimentsToWebViewEnabled());

	}

	@Test
	public void testExperimentsReaderWithLocalTsiEnabledRemoteTsiDisabled() throws JSONException {
		Mockito.when(_localExperimentsMock.getNextSessionExperiments()).thenReturn(new JSONObject("{\"tsi\":true}"));
		Mockito.when(_remoteExperimentsMock.getNextSessionExperiments()).thenReturn(new JSONObject("{\"tsi\":false}"));

		ExperimentsReader experimentsReader = new ExperimentsReader();
		experimentsReader.updateLocalExperiments(_localExperimentsMock);
		experimentsReader.updateRemoteExperiments(_remoteExperimentsMock);

		IExperiments resultExperiments = experimentsReader.getCurrentlyActiveExperiments();
		Assert.assertTrue("Expected TSI flag to be true", resultExperiments.isTwoStageInitializationEnabled());
	}

	@Test
	public void testExperimentsReaderWithLocalTsiDisabledRemoteTsiEnabled() throws JSONException {
		Mockito.when(_localExperimentsMock.getNextSessionExperiments()).thenReturn(new JSONObject("{\"tsi\":false}"));
		Mockito.when(_remoteExperimentsMock.getNextSessionExperiments()).thenReturn(new JSONObject("{\"tsi\":true}"));

		ExperimentsReader experimentsReader = new ExperimentsReader();
		experimentsReader.updateLocalExperiments(_localExperimentsMock);
		experimentsReader.updateRemoteExperiments(_remoteExperimentsMock);

		IExperiments resultExperiments = experimentsReader.getCurrentlyActiveExperiments();
		Assert.assertFalse("Expected TSI flag to be true", resultExperiments.isTwoStageInitializationEnabled());
	}

	@Test
	public void testExperimentsReaderWithLocalTsiEnabledAndRemoteFffEnabled() throws JSONException {
		Mockito.when(_localExperimentsMock.getNextSessionExperiments()).thenReturn(new JSONObject("{\"tsi\":true}"));
		Mockito.when(_localExperimentsMock.getCurrentSessionExperiments()).thenReturn(new JSONObject("{\"fff\":false}"));
		Mockito.when(_remoteExperimentsMock.getNextSessionExperiments()).thenReturn(new JSONObject("{\"tsi\":true}"));
		Mockito.when(_remoteExperimentsMock.getCurrentSessionExperiments()).thenReturn(new JSONObject("{\"fff\":true}"));

		ExperimentsReader experimentsReader = new ExperimentsReader();
		experimentsReader.updateLocalExperiments(_localExperimentsMock);
		experimentsReader.updateRemoteExperiments(_remoteExperimentsMock);

		IExperiments resultExperiments = experimentsReader.getCurrentlyActiveExperiments();
		Assert.assertTrue("Expected TSI flag to be true", resultExperiments.isTwoStageInitializationEnabled());
		Assert.assertTrue("Expected FFF flag to be true", resultExperiments.isForwardExperimentsToWebViewEnabled());
	}

	private void validateDefaultExperiments(IExperiments experiments) {
		Assert.assertFalse(experiments.shouldNativeTokenAwaitPrivacy());
		Assert.assertTrue(experiments.isTwoStageInitializationEnabled());
		Assert.assertFalse(experiments.isNativeWebViewCacheEnabled());
		Assert.assertFalse(experiments.isWebAssetAdCaching());
	}

}
