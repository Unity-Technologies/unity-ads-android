package com.unity3d.services.ads.gmascar.managers;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.unity3d.ads.IUnityAdsTokenListener;
import com.unity3d.services.ads.gmascar.GMA;
import com.unity3d.services.core.configuration.IExperiments;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


@RunWith(PowerMockRunner.class)
@PrepareForTest({GMA.class})
public class BiddingManagerFactoryTest {

	BiddingManagerFactory biddingManagerFactory = BiddingManagerFactory.getInstance();
	IExperiments experiments = mock(IExperiments.class);
	GMA gma = mock(GMA.class);

	@Before
	public void setup() throws Exception {
		PowerMockito.whenNew(GMA.class).withAnyArguments().thenReturn(gma);
		when(gma.hasSCARBiddingSupport()).thenReturn(true);
	}

	@Test
	public void createManagerNoExperiment() {
		BiddingBaseManager manager = biddingManagerFactory.createManager(new IUnityAdsTokenListener() {
			@Override
			public void onUnityAdsTokenReady(String token) {

			}
		}, null);
		assertThat(manager, instanceOf(BiddingEagerManager.class));
	}

	@Test
	public void createManagerNoListener() {
		BiddingBaseManager manager = biddingManagerFactory
			.createManager(null, experiments);
		assertThat(manager, instanceOf(BiddingEagerManager.class));
	}

	@Test
	public void createManagerExperimentEager() {
		when(experiments.getScarBiddingManager()).thenReturn("eag");

		BiddingBaseManager manager = biddingManagerFactory
			.createManager(mock(IUnityAdsTokenListener.class), experiments);
		assertThat(manager, instanceOf(BiddingEagerManager.class));
	}

	@Test
	public void createManagerExperimentLazy() {
		when(experiments.getScarBiddingManager()).thenReturn("laz");

		BiddingBaseManager manager = biddingManagerFactory
			.createManager(mock(IUnityAdsTokenListener.class), experiments);
		assertThat(manager, instanceOf(BiddingLazyManager.class));
	}

	@Test
	public void createManagerExperimentHybrid() {
		when(experiments.getScarBiddingManager()).thenReturn("hyb");

		BiddingBaseManager manager = biddingManagerFactory
			.createManager(mock(IUnityAdsTokenListener.class), experiments);
		assertThat(manager, instanceOf(BiddingOnDemandManager.class));
	}

	@Test
	public void createManagerExperimentDisabled() {
		when(experiments.getScarBiddingManager()).thenReturn("dis");

		BiddingBaseManager manager = biddingManagerFactory
			.createManager(mock(IUnityAdsTokenListener.class), experiments);
		assertThat(manager, instanceOf(BiddingDisabledManager.class));
	}
}
