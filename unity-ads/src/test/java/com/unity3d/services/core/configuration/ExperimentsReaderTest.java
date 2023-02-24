package com.unity3d.services.core.configuration;

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
		Mockito.when(_localExperimentsMock.isNewInitFlowEnabled()).thenReturn(true);
		Assert.assertEquals(_localExperimentsMock, experimentsReader.getCurrentlyActiveExperiments());
		Assert.assertTrue(experimentsReader.getCurrentlyActiveExperiments().isNewInitFlowEnabled());
		validateDefaultExperiments(experimentsReader.getCurrentlyActiveExperiments());
	}

	@Test
	public void testExperimentsReaderWithRemoteOnly() throws JSONException {
		Mockito.lenient().when(_remoteExperimentsMock.getNextSessionExperiments()).thenReturn(new JSONObject("{\"s_init\":true}"));
		Mockito.lenient().when(_remoteExperimentsMock.getCurrentSessionExperiments()).thenReturn(new JSONObject("{\"wac\":true}"));

		ExperimentsReader experimentsReader = new ExperimentsReader();
		experimentsReader.updateRemoteExperiments(_remoteExperimentsMock);

		IExperiments resultExperiments = experimentsReader.getCurrentlyActiveExperiments();
		Assert.assertFalse("Expected S_INIT flag to be false", resultExperiments.isNewInitFlowEnabled());
		Assert.assertTrue("Expected WAC flag to be true", resultExperiments.isWebAssetAdCaching());

	}

	@Test
	public void testExperimentsReaderWithLocalTsiEnabledRemoteTsiDisabled() throws JSONException {
		Mockito.lenient().when(_localExperimentsMock.getNextSessionExperiments()).thenReturn(new JSONObject("{\"s_init\":true}"));
		Mockito.lenient().when(_remoteExperimentsMock.getNextSessionExperiments()).thenReturn(new JSONObject("{\"s_init\":false}"));

		ExperimentsReader experimentsReader = new ExperimentsReader();
		experimentsReader.updateLocalExperiments(_localExperimentsMock);
		experimentsReader.updateRemoteExperiments(_remoteExperimentsMock);

		IExperiments resultExperiments = experimentsReader.getCurrentlyActiveExperiments();
		Assert.assertTrue("Expected S_INIT flag to be true", resultExperiments.isNewInitFlowEnabled());
	}

	@Test
	public void testExperimentsReaderWithLocalTsiDisabledRemoteTsiEnabled() throws JSONException {
		Mockito.lenient().when(_localExperimentsMock.getNextSessionExperiments()).thenReturn(new JSONObject("{\"s_init\":false}"));
		Mockito.lenient().when(_remoteExperimentsMock.getNextSessionExperiments()).thenReturn(new JSONObject("{\"s_init\":true}"));

		ExperimentsReader experimentsReader = new ExperimentsReader();
		experimentsReader.updateLocalExperiments(_localExperimentsMock);
		experimentsReader.updateRemoteExperiments(_remoteExperimentsMock);

		IExperiments resultExperiments = experimentsReader.getCurrentlyActiveExperiments();
		Assert.assertFalse("Expected S_INIT flag to be false", resultExperiments.isNewInitFlowEnabled());
	}

	@Test
	public void testExperimentsReaderWithLocalTsiEnabledAndRemoteFffEnabled() throws JSONException {
		Mockito.lenient().when(_localExperimentsMock.getNextSessionExperiments()).thenReturn(new JSONObject("{\"s_init\":true}"));
		Mockito.lenient().when(_localExperimentsMock.getCurrentSessionExperiments()).thenReturn(new JSONObject("{\"wac\":false}"));
		Mockito.lenient().when(_remoteExperimentsMock.getNextSessionExperiments()).thenReturn(new JSONObject("{\"s_init\":true}"));
		Mockito.lenient().when(_remoteExperimentsMock.getCurrentSessionExperiments()).thenReturn(new JSONObject("{\"wac\":true}"));

		ExperimentsReader experimentsReader = new ExperimentsReader();
		experimentsReader.updateLocalExperiments(_localExperimentsMock);
		experimentsReader.updateRemoteExperiments(_remoteExperimentsMock);

		IExperiments resultExperiments = experimentsReader.getCurrentlyActiveExperiments();
		Assert.assertTrue("Expected S_INIT flag to be true", resultExperiments.isNewInitFlowEnabled());
		Assert.assertTrue("Expected WAC flag to be true", resultExperiments.isWebAssetAdCaching());
	}

	private void validateDefaultExperiments(IExperiments experiments) {
		Assert.assertFalse(experiments.shouldNativeTokenAwaitPrivacy());
		Assert.assertFalse(experiments.isNativeWebViewCacheEnabled());
		Assert.assertFalse(experiments.isWebAssetAdCaching());
	}

}
