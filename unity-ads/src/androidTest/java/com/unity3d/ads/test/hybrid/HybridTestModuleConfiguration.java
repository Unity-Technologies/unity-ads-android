package com.unity3d.ads.test.hybrid;

import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.configuration.IModuleConfiguration;

public class HybridTestModuleConfiguration implements IModuleConfiguration {
  public Class[] getWebAppApiClassList() {
    Class[] apiClassList = {
      com.unity3d.ads.test.hybrid.HybridTest.class
    };
    return apiClassList;
  }
  public boolean resetState(Configuration configuration) {
    return true;
  }
  public boolean initModuleState(Configuration configuration) {
    return true;
  }
  public boolean initErrorState(Configuration configuration, String state, String message) {
    return true;
  }
  public boolean initCompleteState(Configuration configuration) {
    return true;
  }
}