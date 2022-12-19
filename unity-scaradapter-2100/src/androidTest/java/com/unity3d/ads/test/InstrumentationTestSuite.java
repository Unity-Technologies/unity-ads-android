package com.unity3d.ads.test;

import com.unity3d.scar.adapter.v2100.ScarAdapterTest;
import com.unity3d.scar.adapter.v2100.signals.QueryInfoCallbackTest;
import com.unity3d.scar.adapter.v2100.signals.SignalsCollectorTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	SignalsCollectorTest.class,
	QueryInfoCallbackTest.class,
	ScarAdapterTest.class
})
public class InstrumentationTestSuite {
}
