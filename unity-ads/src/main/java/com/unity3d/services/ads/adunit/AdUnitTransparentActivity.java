package com.unity3d.services.ads.adunit;


import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import com.unity3d.services.core.misc.ViewUtilities;
import com.unity3d.services.core.webview.bridge.SharedInstances;

public class AdUnitTransparentActivity extends AdUnitActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtilities.setBackground(super._controller._layout, new ColorDrawable(Color.TRANSPARENT));
    }

	@Override
	protected AdUnitActivityController createController() {
		return new AdUnitTransparentActivityController(this, SharedInstances.INSTANCE.getWebViewEventSender(), new AdUnitViewHandlerFactory());
	}
}