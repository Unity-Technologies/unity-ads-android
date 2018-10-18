package com.unity3d.services.analytics;

import com.unity3d.services.analytics.mocks.UnityAnalyticsMock;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class UnityAnalyticsTest {

    @Before
    public void before() {
        UnityAnalyticsMock.clearEventQueue();
    }

    @After
    public void after() {
        UnityAnalyticsMock.clearEventQueue();
    }

    @Test
    public void testCreateItemAcquired() throws Exception {
        UnityAnalyticsMock.onItemAcquired("context", new Float(3), "itemId", new Float(4), "itemType", "level_one", "transactionId", AcquisitionType.SOFT);

        JSONObject jsonEvent = (JSONObject) UnityAnalyticsMock.getEventQueue().get(0);

        assertEquals("analytics.custom.v1", jsonEvent.get("type"));
        JSONObject msg = (JSONObject) jsonEvent.get("msg");
        assertEquals("item_acquired", msg.get("name"));
        assertNotNull(msg.get("ts"));
        JSONObject customParams = (JSONObject) msg.get("custom_params");
        assertEquals("soft", customParams.get("currency_type"));
        assertEquals("context", customParams.get("transaction_context"));
        assertEquals(new Float(3), customParams.get("amount"));
        assertEquals("itemId", customParams.get("item_id"));
        assertEquals(new Float(4), customParams.get("balance"));
        assertEquals("itemType", customParams.get("item_type"));
        assertEquals("level_one", customParams.get("level"));
        assertEquals("transactionId", customParams.get("transaction_id"));
    }

    @Test
    public void testCreateItemSpent() throws Exception {
        UnityAnalyticsMock.onItemSpent("context", new Float(3), "itemId", new Float(4), "itemType", "level_one", "transactionId", AcquisitionType.SOFT);

        JSONObject jsonEvent = (JSONObject) UnityAnalyticsMock.getEventQueue().get(0);

        assertEquals("analytics.custom.v1", jsonEvent.get("type"));
        JSONObject msg = (JSONObject) jsonEvent.get("msg");
        assertEquals("item_spent", msg.get("name"));
        assertNotNull(msg.get("ts"));
        JSONObject customParams = (JSONObject) msg.get("custom_params");
        assertEquals("soft", customParams.get("currency_type"));
        assertEquals("context", customParams.get("transaction_context"));
        assertEquals(new Float(3), customParams.get("amount"));
        assertEquals("itemId", customParams.get("item_id"));
        assertEquals(new Float(4), customParams.get("balance"));
        assertEquals("itemType", customParams.get("item_type"));
        assertEquals("level_one", customParams.get("level"));
        assertEquals("transactionId", customParams.get("transaction_id"));
    }

    @Test
    public void testCreateLevelFail() throws Exception {
        UnityAnalyticsMock.onLevelFail(7);

        JSONObject jsonEvent = (JSONObject) UnityAnalyticsMock.getEventQueue().get(0);

        assertEquals("analytics.custom.v1", jsonEvent.get("type"));
        JSONObject msg = (JSONObject) jsonEvent.get("msg");
        assertEquals("level_fail", msg.get("name"));
        assertNotNull(msg.get("ts"));
        JSONObject customParams = (JSONObject) msg.get("custom_params");
        assertEquals(7, customParams.get("level_index"));
    }

    @Test
    public void testCreateLevelUp() throws Exception {
        UnityAnalyticsMock.onLevelUp(88);

        JSONObject jsonEvent = (JSONObject) UnityAnalyticsMock.getEventQueue().get(0);

        assertEquals("analytics.custom.v1", jsonEvent.get("type"));
        JSONObject msg = (JSONObject) jsonEvent.get("msg");
        assertEquals("level_up", msg.get("name"));
        assertNotNull(msg.get("ts"));
        JSONObject customParams = (JSONObject) msg.get("custom_params");
        assertEquals(88, customParams.get("new_level_index"));
    }

    @Test
    public void testAdComplete() throws Exception {
        UnityAnalyticsMock.onAdComplete("admob", "myCoolPlacement", true);

        JSONObject jsonEvent = (JSONObject) UnityAnalyticsMock.getEventQueue().get(0);

        assertEquals("analytics.custom.v1", jsonEvent.get("type"));
        JSONObject msg = (JSONObject) jsonEvent.get("msg");
        assertEquals("ad_complete", msg.get("name"));
        assertNotNull(msg.get("ts"));
        JSONObject customParams = (JSONObject) msg.get("custom_params");
        assertEquals(true, customParams.get("rewarded"));
        assertEquals("admob", customParams.get("network"));
        assertEquals("myCoolPlacement", customParams.get("placement_id"));
    }

    @Test
    public void testIapTransaction() throws Exception {
        UnityAnalyticsMock.onIapTransaction("myCoolProduct", 34.5F, "USD", true, "test receipt");

        JSONObject jsonEvent = (JSONObject) UnityAnalyticsMock.getEventQueue().get(0);

        assertEquals("analytics.transaction.v1", jsonEvent.get("type"));
        JSONObject msg = (JSONObject) jsonEvent.get("msg");
        assertNotNull(msg.get("ts"));
        assertEquals("myCoolProduct", msg.get("productid"));
        assertEquals(34.5F, msg.get("amount"));
        assertEquals("USD", msg.get("currency"));
        assertEquals(3151341L, msg.get("transactionid"));
        assertEquals(false, msg.get("iap_service"));
        assertEquals(true, msg.get("promo"));
        assertEquals("test receipt", msg.get("receipt"));
    }

    @Test
    public void testOnEvent() throws Exception {
        Date currentDate = new Date();
        HashMap<String, Object> customParams = new HashMap<>();
        customParams.put("level_index", 455);
        HashMap<String, Object> msg = new HashMap<>();
        msg.put("custom_params", customParams);
        msg.put("ts", currentDate.getTime());
        msg.put("name", "level_fail");
        HashMap<String, Object> map = new HashMap<>();
        map.put("type", "analytics.custom.v1");
        map.put("msg", msg);
        JSONObject event = new JSONObject(map);

        UnityAnalyticsMock.onEvent(event);
        JSONObject jsonEvent = (JSONObject) UnityAnalyticsMock.getEventQueue().get(0);

        assertEquals("analytics.custom.v1", jsonEvent.get("type"));
        JSONObject expectedMsg = (JSONObject) jsonEvent.get("msg");
        assertEquals("level_fail", expectedMsg.get("name"));
        assertEquals(currentDate.getTime(), expectedMsg.get("ts"));
        JSONObject expectedCustomParams = (JSONObject) expectedMsg.get("custom_params");
        assertEquals(455, expectedCustomParams.get("level_index"));
    }
    
    @Test
    public void testMultipleEvents() {
        UnityAnalyticsMock.onIapTransaction("myCoolProduct", 34.5F, "USD", true, "test receipt");
        UnityAnalyticsMock.onIapTransaction("myCoolProduct", 34.5F, "USD", true, "test receipt");
        
        assertEquals(2, UnityAnalyticsMock.getEventQueue().length());
    }

    @Test
    public void testMaximumQueuedEvents() {
        for (int i = 0; i < 200; i++) {
            UnityAnalyticsMock.onIapTransaction("myCoolProduct", 34.5F, "USD", true, "test receipt");
            assertEquals(i + 1, UnityAnalyticsMock.getEventQueue().length());
        }
        UnityAnalyticsMock.onIapTransaction("myCoolProduct", 34.5F, "USD",true, "test receipt");
        assertEquals(200, UnityAnalyticsMock.getEventQueue().length());
    }
}
