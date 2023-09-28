package com.unity3d.ads.test.instrumentation.services.ads.gmascar.adapters;

import com.unity3d.scar.adapter.common.IAdsErrorHandler;
import com.unity3d.scar.adapter.common.IScarAdapter;
import com.unity3d.scar.adapter.common.WebViewAdsError;
import com.unity3d.services.ads.gmascar.adapters.ScarAdapterFactory;
import com.unity3d.services.ads.gmascar.finder.ScarAdapterVersion;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ScarAdapterFactoryTest {
	@Mock
	private IAdsErrorHandler<WebViewAdsError> adsErrorHandlerMock;

	private final ScarAdapterFactory _scarAdapterFactory = new ScarAdapterFactory();

	@Test
	public void testScarAdapterFactory2000() {
		IScarAdapter adapter = _scarAdapterFactory.createScarAdapter(ScarAdapterVersion.V20, adsErrorHandlerMock);
		Assert.assertTrue(adapter instanceof com.unity3d.scar.adapter.v2000.ScarAdapter);
	}

	@Test
	public void testScarAdapterFactoryUnsupported() {
		IScarAdapter adapter = _scarAdapterFactory.createScarAdapter(ScarAdapterVersion.NA, adsErrorHandlerMock);
		Assert.assertNull(adapter);
	}
}
