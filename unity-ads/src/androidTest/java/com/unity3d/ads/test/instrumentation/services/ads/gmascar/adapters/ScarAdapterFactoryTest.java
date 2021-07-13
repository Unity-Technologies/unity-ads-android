package com.unity3d.ads.test.instrumentation.services.ads.gmascar.adapters;

import com.unity3d.scar.adapter.common.IAdsErrorHandler;
import com.unity3d.scar.adapter.common.IScarAdapter;
import com.unity3d.services.ads.gmascar.adapters.ScarAdapterFactory;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ScarAdapterFactoryTest {
	@Mock
	private IAdsErrorHandler adsErrorHandlerMock;

	private ScarAdapterFactory _scarAdapterFactory = new ScarAdapterFactory();

	@Test
	public void testScarAdapterFactory1920() {
		IScarAdapter adapter = _scarAdapterFactory.createScarAdapter(ScarAdapterFactory.CODE_19_2, adsErrorHandlerMock);
		Assert.assertTrue(adapter instanceof com.unity3d.scar.adapter.v1920.ScarAdapter);
	}

	@Test
	public void testScarAdapterFactory1950() {
		IScarAdapter adapter = _scarAdapterFactory.createScarAdapter(ScarAdapterFactory.CODE_19_5, adsErrorHandlerMock);
		Assert.assertTrue(adapter instanceof com.unity3d.scar.adapter.v1950.ScarAdapter);
	}

	@Test
	public void testScarAdapterFactory2000() {
		IScarAdapter adapter = _scarAdapterFactory.createScarAdapter(ScarAdapterFactory.CODE_20_0, adsErrorHandlerMock);
		Assert.assertTrue(adapter instanceof com.unity3d.scar.adapter.v2000.ScarAdapter);
	}

	@Test
	public void testScarAdapterFactoryUnsupported() {
		IScarAdapter adapter = _scarAdapterFactory.createScarAdapter(-1, adsErrorHandlerMock);
		Assert.assertNull(adapter);
	}
}
