package com.unity3d.services.analytics.core.configuration;

import com.unity3d.services.analytics.core.api.Analytics;
import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.configuration.ErrorState;
import com.unity3d.services.core.configuration.IModuleConfiguration;

public class AnalyticsModuleConfiguration implements IModuleConfiguration {

    private static final Class[] WEB_APP_API_CLASS_LIST = new Class[] {
            Analytics.class
    };

    @Override
    public Class[] getWebAppApiClassList() {
        return WEB_APP_API_CLASS_LIST;
    }

    @Override
    public boolean resetState(Configuration configuration) {
        return true;
    }

    @Override
    public boolean initModuleState(Configuration configuration) {
        return true;
    }

    @Override
    public boolean initErrorState(Configuration configuration, ErrorState state, String message) {
        return true;
    }

    @Override
    public boolean initCompleteState(Configuration configuration) {
        return true;
    }
}