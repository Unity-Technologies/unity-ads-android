package com.unity3d.services.ads.adunit;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import com.unity3d.services.core.misc.ViewUtilities;
import com.unity3d.services.core.webview.bridge.IEventSender;

public class AdUnitTransparentActivityController extends AdUnitActivityController {
	public AdUnitTransparentActivityController(IAdUnitActivity activity, IEventSender eventSender, IAdUnitViewHandlerFactory adUnitViewHandlerFactory) {
		super(activity, eventSender, adUnitViewHandlerFactory);
	}

	@Override
	protected void createLayout() {
		super.createLayout();
		ViewUtilities.setBackground(_layout, new ColorDrawable(Color.TRANSPARENT));
	}
}
