package com.unity3d.ads.test.instrumentation.services.ads.gmascar;

import com.unity3d.ads.test.instrumentation.services.ads.gmascar.adapters.ScarAdapterFactoryTest;
import com.unity3d.ads.test.instrumentation.services.ads.gmascar.finder.GMAInitializerTest;
import com.unity3d.ads.test.instrumentation.services.ads.gmascar.finder.PresenceDetectorTest;
import com.unity3d.ads.test.instrumentation.services.ads.gmascar.finder.ScarVersionFinderTest;
import com.unity3d.ads.test.instrumentation.services.ads.gmascar.bridges.AdapterStatusBridgeTest;
import com.unity3d.ads.test.instrumentation.services.ads.gmascar.bridges.InitializationStatusBridgeTest;
import com.unity3d.ads.test.instrumentation.services.ads.gmascar.bridges.InitializeListenerBridgeTest;
import com.unity3d.ads.test.instrumentation.services.ads.gmascar.bridges.MobileAdsBridgeTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	ScarAdapterFactoryTest.class,
	GMAInitializerTest.class,
	PresenceDetectorTest.class,
	ScarVersionFinderTest.class,
	AdapterStatusBridgeTest.class,
	InitializationStatusBridgeTest.class,
	InitializeListenerBridgeTest.class,
	MobileAdsBridgeTest.class
})
public class GmaScarTestSuite {
}
