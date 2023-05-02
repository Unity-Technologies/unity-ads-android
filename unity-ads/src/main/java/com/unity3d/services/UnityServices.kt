package com.unity3d.services

import android.app.Activity
import android.app.Application
import android.content.Context
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.UnityAds
import com.unity3d.ads.UnityAds.UnityAdsInitializationError
import com.unity3d.services.UnityAdsSDK.initialize
import com.unity3d.services.core.configuration.ConfigurationReader
import com.unity3d.services.core.configuration.EnvironmentCheck
import com.unity3d.services.core.configuration.InitializeEventsMetricSender
import com.unity3d.services.core.configuration.InitializeThread
import com.unity3d.services.core.device.Device
import com.unity3d.services.core.log.DeviceLog
import com.unity3d.services.core.properties.ClientProperties
import com.unity3d.services.core.properties.SdkProperties
import com.unity3d.services.core.properties.SdkProperties.InitializationState
import com.unity3d.services.core.properties.Session.Default.id
import com.unity3d.services.core.request.metrics.InitMetric

object UnityServices {
    /**
     * Initializes Unity Ads. Unity Ads should be initialized when app starts.
     * @param context Current Android application context of calling app
     * @param gameId Unique identifier for a game, given by Unity Ads admin tools or Unity editor
     * @param testMode If true, only test ads are shown
     * @param initializationListener Listener for IUnityAdsInitializationListener callbacks
     */
	@JvmStatic
	fun initialize(
        context: Context?,
        gameId: String?,
        testMode: Boolean,
        initializationListener: IUnityAdsInitializationListener?
    ) {
        DeviceLog.entered()

        gameId?.toIntOrNull() ?: run {
            initializationListener?.onInitializationFailed(
                UnityAdsInitializationError.INVALID_ARGUMENT,
                "gameId \"$gameId\" should be a number."
            )
            return
        }

        if (SdkProperties.getCurrentInitializationState() != InitializationState.NOT_INITIALIZED) {
            val previousGameId = ClientProperties.getGameId()
            val previousTestMode = SdkProperties.isTestMode()

            val differingParameters = buildString {
                if (previousGameId != null && previousGameId != gameId) {
                    appendLine(createExpectedParametersString("Game ID", previousGameId, gameId))
                }

                if (previousTestMode != testMode) {
                    appendLine(createExpectedParametersString("Test Mode", previousTestMode, testMode))
                }
            }

            val params = if (differingParameters.isNotEmpty()) {
                InitializeEventsMetricSender.getInstance().sendMetric(InitMetric.newInitDiffParams())
                "different parameters: \n$differingParameters"
            } else {
                InitializeEventsMetricSender.getInstance().sendMetric(InitMetric.newInitSameParams())
                "the same Game ID: $gameId and Test Mode: $testMode values."
            }

            DeviceLog.warning("Unity Ads SDK initialize has already been called with $params Responding with first initialization result.")
        }

        SdkProperties.addInitializationListener(initializationListener)

        when (SdkProperties.getCurrentInitializationState()) {
            InitializationState.INITIALIZED_SUCCESSFULLY -> {
                SdkProperties.notifyInitializationComplete()
                return
            }
            InitializationState.INITIALIZED_FAILED -> {
                SdkProperties.notifyInitializationFailed(
                    UnityAds.UnityAdsInitializationError.INTERNAL_ERROR,
                    "Unity Ads SDK failed to initialize due to previous failed reason"
                )
                return
            }
            InitializationState.INITIALIZING -> return
            else -> SdkProperties.setInitializeState(InitializationState.INITIALIZING)
        }

        ClientProperties.setGameId(gameId)
        SdkProperties.setTestMode(testMode)

        if (!isSupported) {
            DeviceLog.error("Error while initializing Unity Services: device is not supported")
            SdkProperties.notifyInitializationFailed(
                UnityAds.UnityAdsInitializationError.INTERNAL_ERROR,
                "Unity Ads SDK failed to initialize due to device is not supported"
            )
            return
        }

        SdkProperties.setInitializationTime(Device.getElapsedRealtime())
        SdkProperties.setInitializationTimeSinceEpoch(System.currentTimeMillis())

        if (gameId.isNullOrEmpty()) {
            DeviceLog.error("Error while initializing Unity Services: empty game ID, halting Unity Ads init")
            SdkProperties.notifyInitializationFailed(
                UnityAds.UnityAdsInitializationError.INVALID_ARGUMENT,
                "Unity Ads SDK failed to initialize due to empty game ID"
            )
            return
        }

        if (context == null) {
            DeviceLog.error("Error while initializing Unity Services: null context, halting Unity Ads init")
            SdkProperties.notifyInitializationFailed(
                UnityAds.UnityAdsInitializationError.INVALID_ARGUMENT,
                "Unity Ads SDK failed to initialize due to null context"
            )
            return
        }

        if (context is Application) {
            ClientProperties.setApplication(context)
        } else if (context is Activity) {
            if (context.application != null) {
                ClientProperties.setApplication(context.application)
            } else {
                DeviceLog.error("Error while initializing Unity Services: cannot retrieve application from context, halting Unity Ads init")
                SdkProperties.notifyInitializationFailed(
                    UnityAds.UnityAdsInitializationError.INVALID_ARGUMENT,
                    "Unity Ads SDK failed to initialize due to inability to retrieve application from context"
                )
                return
            }
        } else {
            DeviceLog.error("Error while initializing Unity Services: invalid context, halting Unity Ads init")
            SdkProperties.notifyInitializationFailed(
                UnityAds.UnityAdsInitializationError.INVALID_ARGUMENT,
                "Unity Ads SDK failed to initialize due to invalid context"
            )
            return
        }

        val mode = if (testMode) "test mode" else "production mode"
        DeviceLog.info("Initializing Unity Services ${SdkProperties.getVersionName()} (${SdkProperties.getVersionCode()}) with game id $gameId in $mode, session $id")

        SdkProperties.setDebugMode(SdkProperties.getDebugMode())

        if (context.applicationContext != null) {
            ClientProperties.setApplicationContext(context.applicationContext)
        } else {
            DeviceLog.error("Error while initializing Unity Services: cannot retrieve application context, halting Unity Ads init")
            SdkProperties.notifyInitializationFailed(
                UnityAds.UnityAdsInitializationError.INVALID_ARGUMENT,
                "Unity Ads SDK failed to initialize due to inability to retrieve application context"
            )
            return
        }

        if (!EnvironmentCheck.isEnvironmentOk()) {
            DeviceLog.error("Error during Unity Services environment check, halting Unity Services init")
            SdkProperties.notifyInitializationFailed(
                UnityAds.UnityAdsInitializationError.INTERNAL_ERROR,
                "Unity Ads SDK failed to initialize due to environment check failed"
            )
            return
        }

        DeviceLog.info("Unity Services environment check OK")

        initialize()
    }

    @JvmStatic
	val isSupported: Boolean = true

    @JvmStatic
	val isInitialized: Boolean
        get() = SdkProperties.isInitialized()

    @JvmStatic
	val version: String
        get() = SdkProperties.getVersionName()

    @JvmStatic
	var debugMode: Boolean
        /**
         * Get current debug mode status
         *
         * @return If true, debug mode is on. If false, debug mode is off.
         */
        get() = SdkProperties.getDebugMode()
        /**
         * Toggles debug mode on/off
         *
         * @param debugMode If true, debug mode is on and there will be lots of debug output from Unity Services. If false, there will be only some short log messages from Unity Services.
         */
        set(debugMode) {
            SdkProperties.setDebugMode(debugMode)
        }

    private fun createExpectedParametersString(fieldName: String, current: Any?, received: Any?): String =
        "- $fieldName Current: $current | Received: $received"

    enum class UnityServicesError {
        INVALID_ARGUMENT,
        INIT_SANITY_CHECK_FAIL
    }
}
