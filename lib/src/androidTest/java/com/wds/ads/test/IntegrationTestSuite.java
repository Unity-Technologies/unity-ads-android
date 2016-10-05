package com.wds.ads.test;

import com.wds.ads.test.integration.IntegrationTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  IntegrationTest.class,
})
public class IntegrationTestSuite {
}
