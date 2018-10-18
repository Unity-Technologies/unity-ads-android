package com.unity3d.services.monetization.core.api;

import com.unity3d.ads.UnityAds;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.monetization.UnityMonetization;
import com.unity3d.services.monetization.placementcontent.ads.IShowAdListener;
import com.unity3d.services.monetization.placementcontent.ads.ShowAdPlacementContent;
import com.unity3d.services.monetization.placementcontent.core.PlacementContent;
import com.unity3d.services.core.webview.bridge.WebViewCallback;
import com.unity3d.services.core.webview.bridge.WebViewExposed;
import com.unity3d.services.monetization.core.placementcontent.PlacementContentResultFactory;
import com.unity3d.services.monetization.core.utilities.JSONUtilities;

import org.json.JSONObject;

public class PlacementContents {
    @WebViewExposed
    public static void createPlacementContent(String placementId, JSONObject params, WebViewCallback callback) {
        PlacementContent result = PlacementContentResultFactory.create(placementId, JSONUtilities.jsonObjectToMap(params));
        com.unity3d.services.monetization.core.placementcontent.PlacementContents.putPlacementContent(placementId, result);
        callback.invoke();
    }

    @WebViewExposed
    public static void setPlacementContentState(String placementId, String state, WebViewCallback callback) {
        com.unity3d.services.monetization.core.placementcontent.PlacementContents.setPlacementContentState(placementId, UnityMonetization.PlacementContentState.valueOf(state));
        callback.invoke();
    }

    @WebViewExposed
    public static void sendAdFinished(String placementId, final String finishState, WebViewCallback callback) {
        ShowAdPlacementContent.sendAdFinished(placementId, UnityAds.FinishState.valueOf(finishState));
        callback.invoke();
    }
    @WebViewExposed
    public static void sendAdStarted(String placementId, WebViewCallback callback) {
        ShowAdPlacementContent.sendAdStarted(placementId);
        callback.invoke();
    }
}
