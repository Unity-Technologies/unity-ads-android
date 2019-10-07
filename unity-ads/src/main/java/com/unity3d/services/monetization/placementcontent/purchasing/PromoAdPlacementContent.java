package com.unity3d.services.monetization.placementcontent.purchasing;

import com.unity3d.services.monetization.placementcontent.ads.ShowAdPlacementContent;

import java.util.Map;

public class PromoAdPlacementContent extends ShowAdPlacementContent {
    private final PromoMetadata promoMetadata;

    public PromoAdPlacementContent(String placementId, Map<String, Object> params) {
        super(placementId, params);

        promoMetadata = PromoMetadataUtilities.createPromoMetadataFromParamsMap(params);
    }

    public PromoMetadata getMetadata() {
        return promoMetadata;
    }
    protected String getDefaultEventCategory() {
        return "PROMO";
    }
}
