package com.unity3d.ads.adunit;


import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import com.unity3d.ads.misc.ViewUtilities;

public class AdUnitTransparentActivity extends AdUnitActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtilities.setBackground(super._layout, new ColorDrawable(Color.TRANSPARENT));
    }

    @Override
    protected void createLayout() {
        super.createLayout();
        ViewUtilities.setBackground(_layout, new ColorDrawable(Color.TRANSPARENT));
    }
}