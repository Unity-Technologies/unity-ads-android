package com.unity3d.services.monetization.placementcontent.core;

import java.util.Map;

public class RewardablePlacementContent extends PlacementContent {
    private boolean isRewarded;
    private String rewardId;

    public RewardablePlacementContent(String placementId, Map<String, Object> params) {
        super(placementId, params);

        if (params.containsKey("rewarded")) {
            isRewarded = (boolean) params.get("rewarded");
        }
        if (params.containsKey("rewardId")) {
            rewardId = (String) params.get("rewardId");
        }
    }

    public boolean isRewarded() {
        return isRewarded;
    }

    public String getRewardId() {
        return rewardId;
    }
}
