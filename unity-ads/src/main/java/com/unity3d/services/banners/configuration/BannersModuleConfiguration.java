package com.unity3d.services.banners.configuration;

import com.unity3d.services.banners.api.Banner;
import com.unity3d.services.banners.api.BannerListener;
import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.configuration.ErrorState;
import com.unity3d.services.core.configuration.IModuleConfiguration;

public class BannersModuleConfiguration implements IModuleConfiguration {
    private static final Class[] WEB_APP_API_CLASS_LIST = {
            Banner.class,
            BannerListener.class
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
