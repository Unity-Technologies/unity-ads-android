package com.unity3d.services.monetization.core.api;

import com.unity3d.services.monetization.IUnityMonetizationListener;
import com.unity3d.services.core.webview.bridge.WebViewCallback;
import com.unity3d.services.core.webview.bridge.WebViewExposed;
import com.unity3d.services.monetization.UnityMonetization;
import com.unity3d.services.monetization.core.placementcontent.PlacementContentListenerError;
import com.unity3d.services.monetization.placementcontent.core.PlacementContent;
import com.unity3d.services.monetization.core.placementcontent.PlacementContents;
import com.unity3d.services.monetization.core.properties.ClientProperties;

public class MonetizationListener {

    @WebViewExposed
    public static void isMonetizationEnabled(WebViewCallback callback) {
        callback.invoke(ClientProperties.isMonetizationEnabled());
    }

    @WebViewExposed
    public static void sendPlacementContentReady(String placementId, WebViewCallback callback) {
        IUnityMonetizationListener placementContentListener = ClientProperties.getListener();
        if (placementContentListener != null) {
            try {
                PlacementContent placementContent = PlacementContents.getPlacementContent(placementId);
                placementContentListener.onPlacementContentReady(placementId, placementContent);
                callback.invoke();
            } catch (Exception e) {
                callback.error(PlacementContentListenerError.PLACEMENTCONTENT_LISTENER_ERROR, e);
            }
        } else {
            callback.error(PlacementContentListenerError.PLACEMENTCONTENT_LISTENER_NULL);
        }
    }

    @WebViewExposed
    public static void sendPlacementContentStateChanged(String placementId, String previousState, String newState, WebViewCallback callback) {
        IUnityMonetizationListener placementContentListener = ClientProperties.getListener();
        if (placementContentListener != null) {
            try {
                PlacementContent placementContent = PlacementContents.getPlacementContent(placementId);
                placementContentListener.onPlacementContentStateChange(placementId,
                        placementContent,
                        UnityMonetization.PlacementContentState.valueOf(previousState),
                        UnityMonetization.PlacementContentState.valueOf(newState));
                callback.invoke();
            } catch (Exception e) {
                callback.error(PlacementContentListenerError.PLACEMENTCONTENT_LISTENER_ERROR, e);
            }
        } else {
            callback.error(PlacementContentListenerError.PLACEMENTCONTENT_LISTENER_NULL);
        }
    }
}
