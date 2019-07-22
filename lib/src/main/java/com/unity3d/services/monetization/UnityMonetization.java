package com.unity3d.services.monetization;

import android.app.Activity;

import com.unity3d.services.UnityServices;
import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.monetization.core.placementcontent.PlacementContents;
import com.unity3d.services.monetization.placementcontent.core.PlacementContent;
import com.unity3d.services.monetization.core.properties.ClientProperties;

public class UnityMonetization {

    /**
     * Sets the listener for PlacementContent events.
     * @param listener The listener for event callbacks.
     */
    public static void setListener(IUnityMonetizationListener listener) {
        ClientProperties.setListener(listener);
    }

    /**
     * Returns the current monetization listener.
     * @return The listener for event callbacks.
     */
    public static IUnityMonetizationListener getListener() {
        return ClientProperties.getListener();
    }

    /**
     * Checks if a placementcontent is ready for the given placement.
     * @param placementId Placement ID as configured in the Unity Ads dashboard.
     * @return True, if a placementcontent is ready, or else false.
     */
    public static boolean isReady(String placementId) {
        return PlacementContents.isReady(placementId);
    }

    /**
     * Returns the placementcontent for the given placement.
     * @param placementId Placement ID as configured in the Unity Ads dashboard.
     * @return PlacementContent, if one is available, or else null.
     */
    public static PlacementContent getPlacementContent(String placementId) {
        return PlacementContents.getPlacementContent(placementId);
    }

    /**
     *
     * @param placementId Placement ID as configured in the Unity Ads dashboard.
     * @param asClass The class to cast the placementcontent to.
     * @param <T> The type of placementcontent to be casted to.
     * @return The placementcontent as T, if it is of that type. If it isn't, this function returns null.
     */
    public static <T extends PlacementContent> T getPlacementContent(String placementId, Class<T> asClass) {
        return PlacementContents.getPlacementContent(placementId, asClass);
    }

    public static void initialize(Activity activity, String gameId, IUnityMonetizationListener listener) {
        boolean testMode = false;
        initialize(activity, gameId, listener, testMode);
    }

    public static void initialize(Activity activity, String gameId, IUnityMonetizationListener listener, boolean testMode) {
        DeviceLog.entered();
        if (listener != null) {
            setListener(listener);
        }
        ClientProperties.setMonetizationEnabled(true);
        boolean usePerPlacementLoad = false;
        UnityServices.initialize(activity, gameId, listener, testMode, usePerPlacementLoad);
    }

    /**
     * PlacementContentState is an enum representing the state of a placementcontent. All states other than READY describe
     * that the placementcontent should not be used.
     */
    public enum PlacementContentState {
        /**
         * Placement is ready to show ads. You can call show method and ad unit will open.
         */
        READY,

        /**
         * Current placement state is not available. SDK is not initialized or this placement has not been configured in Unity Ads admin tools.
         */
        NOT_AVAILABLE,

        /**
         * Placement is disabled. Placement can be enabled via Unity Ads admin tools.
         */
        DISABLED,

        /**
         * Placement is not yet ready but it will be ready in the future. Most likely reason is caching.
         */
        WAITING,

        /**
         * Placement is properly configured but there are currently no ads available for the placement.
         */
        NO_FILL
    }
}
