package com.unity3d.services.analytics;

import com.unity3d.services.core.webview.WebViewApp;
import com.unity3d.services.core.webview.WebViewEventCategory;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;

public class UnityAnalytics {

    protected static JSONArray eventQueue = new JSONArray();

    private enum AnalyticsEventType {
        POSTEVENT
    }

    private static JSONObject createItemAcquired(String transactionContext, Float amount, String itemId, Float balance, String itemType, String level, String transactionId, AcquisitionType acquisitionType) {
        HashMap<String, Object> customParams = new HashMap<>();
        customParams.put("currency_type", acquisitionType.toString());
        customParams.put("transaction_context", transactionContext);
        customParams.put("amount", amount);
        customParams.put("item_id", itemId);
        customParams.put("balance", balance);
        customParams.put("item_type", itemType);
        customParams.put("level", level);
        customParams.put("transaction_id", transactionId);
        HashMap<String, Object> msg = new HashMap<>();
        msg.put("custom_params", customParams);
        msg.put("ts", 1533594423477L);
        msg.put("name", "item_acquired");
        HashMap<String, Object> map = new HashMap<>();
        map.put("type", "analytics.custom.v1");
        map.put("msg", msg);
        return new JSONObject(map);
    }

    private static JSONObject createItemSpent(String transactionContext, Float amount, String itemId, Float balance, String itemType, String level, String transactionId, AcquisitionType acquisitionType) {
        HashMap<String, Object> customParams = new HashMap<>();
        customParams.put("currency_type", acquisitionType.toString());
        customParams.put("transaction_context", transactionContext);
        customParams.put("amount", amount);
        customParams.put("item_id", itemId);
        customParams.put("balance", balance);
        customParams.put("item_type", itemType);
        customParams.put("level", level);
        customParams.put("transaction_id", transactionId);
        HashMap<String, Object> msg = new HashMap<>();
        msg.put("custom_params", customParams);
        msg.put("ts", new Date().getTime());
        msg.put("name", "item_spent");
        HashMap<String, Object> map = new HashMap<>();
        map.put("type", "analytics.custom.v1");
        map.put("msg", msg);
        return new JSONObject(map);
    }

    private static JSONObject createLevelFail(Integer levelIndex) {
        HashMap<String, Object> customParams = new HashMap<>();
        customParams.put("level_index", levelIndex);
        HashMap<String, Object> msg = new HashMap<>();
        msg.put("custom_params", customParams);
        msg.put("ts", new Date().getTime());
        msg.put("name", "level_fail");
        HashMap<String, Object> map = new HashMap<>();
        map.put("type", "analytics.custom.v1");
        map.put("msg", msg);
        return new JSONObject(map);
    }

    private static JSONObject createLevelUp(Integer newLevelIndex) {
        HashMap<String, Object> customParams = new HashMap<>();
        customParams.put("new_level_index", newLevelIndex);
        HashMap<String, Object> msg = new HashMap<>();
        msg.put("custom_params", customParams);
        msg.put("ts", new Date().getTime());
        msg.put("name", "level_up");
        HashMap<String, Object> map = new HashMap<>();
        map.put("type", "analytics.custom.v1");
        map.put("msg", msg);
        return new JSONObject(map);
    }

    private static JSONObject createAdComplete(String network, String placementId, Boolean rewarded) {
        HashMap<String, Object> customParams = new HashMap<>();
        customParams.put("rewarded", rewarded);
        customParams.put("network", network);
        customParams.put("placement_id", placementId);
        HashMap<String, Object> msg = new HashMap<>();
        msg.put("custom_params", customParams);
        msg.put("ts", new Date().getTime());
        msg.put("name", "ad_complete");
        HashMap<String, Object> map = new HashMap<>();
        map.put("type", "analytics.custom.v1");
        map.put("msg", msg);
        return new JSONObject(map);
    }

    private static JSONObject createIapTransaction(String productId, Float amount, String currency, Boolean isPromo, String receipt) {
        HashMap<String, Object> msg = new HashMap<>();
        msg.put("ts", new Date().getTime());
        msg.put("name", "ad_complete");
        msg.put("productid", productId);
        msg.put("amount", amount);
        msg.put("currency", currency);
        msg.put("promo", isPromo);
        msg.put("receipt", receipt);
        HashMap<String, Object> map = new HashMap<>();
        map.put("type", "analytics.transaction.v1");
        map.put("msg", msg);
        return new JSONObject(map);
    }

    public static void onItemAcquired(String transactionContext, Float amount, String itemId, Float balance, String itemType, String level, String transactionId, AcquisitionType acquisitionType) {
        JSONObject jsonObject = createItemAcquired(transactionContext, amount, itemId, balance, itemType, level, transactionId, acquisitionType);
        postEvent(jsonObject);
    }

    public static void onItemSpent(String transactionContext, Float amount, String itemId, Float balance, String itemType, String level, String transactionId, AcquisitionType acquisitionType) {
        JSONObject jsonObject = createItemSpent(transactionContext, amount, itemId, balance, itemType, level, transactionId, acquisitionType);
        postEvent(jsonObject);
    }

    public static void onLevelFail(Integer levelIndex) {
        JSONObject jsonObject = createLevelFail(levelIndex);
        postEvent(jsonObject);
    }

    public static void onLevelUp(Integer newLevelIndex) {
        JSONObject jsonObject = createLevelUp(newLevelIndex);
        postEvent(jsonObject);
    }

    public static void onAdComplete(String network, String placementId, Boolean rewarded) {
        JSONObject jsonObject = createAdComplete(network, placementId, rewarded);
        postEvent(jsonObject);
    }

    public static void onIapTransaction(String productId, Float amount, String currency, Boolean isPromo, String receipt) {
        JSONObject jsonObject = createIapTransaction(productId, amount, currency, isPromo, receipt);
        postEvent(jsonObject);
    }

    public static void onEvent(JSONObject jsonEvent) {
        postEvent(jsonEvent);
    }

    private synchronized static void postEvent(JSONObject event) {
        if (UnityAnalytics.eventQueue.length() < 200) {
            // maximum queued events is 200
            UnityAnalytics.eventQueue.put(event);
        }
        WebViewApp currentApp = WebViewApp.getCurrentApp();
        if (currentApp != null) {
            Boolean success = currentApp.sendEvent(WebViewEventCategory.ANALYTICS, AnalyticsEventType.POSTEVENT, UnityAnalytics.eventQueue.toString());
            if (success) {
                // clear queue
                UnityAnalytics.eventQueue = new JSONArray();
            }
        }
    }
}
