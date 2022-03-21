package com.unity3d.ads.test.instrumentation.services.core.misc;

import com.unity3d.services.core.misc.JsonFlattener;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class JsonFlattenerTest {

	@Test
	public void testJsonFlattenerSimple() throws JSONException {
		JSONObject jsonObject = new JSONObject("{\"mediation\":{\"ordinal\":{\"value\":1,\"ts\":1642792098742}}}");
		JsonFlattener jsonFlattener = new JsonFlattener(jsonObject);
		JSONObject result = jsonFlattener.flattenJson(".", Collections.singletonList("mediation"), Collections.singletonList("value"), Collections.singletonList("ts"));
		Assert.assertEquals(1, result.get("mediation.ordinal"));
		Assert.assertEquals("{\"mediation.ordinal\":1}", result.toString());
	}

	@Test
	public void testJsonFlattenerComplex() throws JSONException {
		JSONObject jsonObject = new JSONObject("{\"configuration\":{\"hasInitialized\":true},\"analytics\":{\"appstartinfo\":{\"latestUTCTransactionDate\":{\"year\":2022,\"month\":0,\"day\":21},\"todayCount\":5,\"totalCount\":5\"},\"installhour\":1642780800000,\"userid\":\"247bd7e7fe1849a6b92eeeb211679fdc\",\"sessionid\":199406426711996,\"appversion\":\"4.0.0\",\"osversion\":\"12\"},\"user\":{\"requestCount\":14,\"requestToReadyTime\":1035,\"hasSentAvailabilityFeatures\":true}}");
		JsonFlattener jsonFlattener = new JsonFlattener(jsonObject);
		JSONObject result = jsonFlattener.flattenJson(".", Collections.singletonList("configuration"), Collections.singletonList(""), Collections.singletonList(""));
		Assert.assertEquals("{\"configuration.hasInitialized\":true}", result.toString());
	}

	@Test
	public void testJsonFlattenerMultipleKeys() throws JSONException {
		JSONObject jsonObject = new JSONObject("{\"mediation\":{\"ordinal\":{\"value\":1,\"ts\":1642792098742}},\"configuration\":{\"hasInitialized\":true},\"analytics\":{\"appstartinfo\":{\"latestUTCTransactionDate\":{\"year\":2022,\"month\":0,\"day\":21},\"todayCount\":5,\"totalCount\":5\"},\"installhour\":1642780800000,\"userid\":\"247bd7e7fe1849a6b92eeeb211679fdc\",\"sessionid\":199406426711996,\"appversion\":\"4.0.0\",\"osversion\":\"12\"},\"user\":{\"requestCount\":14,\"requestToReadyTime\":1035,\"hasSentAvailabilityFeatures\":true}}");
		JsonFlattener jsonFlattener = new JsonFlattener(jsonObject);
		JSONObject result = jsonFlattener.flattenJson(".", Arrays.asList("mediation", "configuration"), Collections.singletonList("value"), Collections.singletonList("ts"));
		Assert.assertEquals(1, result.get("mediation.ordinal"));
		Assert.assertEquals(true, result.get("configuration.hasInitialized"));
		Assert.assertEquals("{\"mediation.ordinal\":1,\"configuration.hasInitialized\":true}", result.toString());
	}

	@Test
	public void testJsonFlattenerWithCompleteSample() throws JSONException {
		JSONObject jsonObject = new JSONObject("{\"configuration\":{\"hasInitialized\":true},\"analytics\":{\"appstartinfo\":\"{\\\"latestUTCTransactionDate\\\":{\\\"year\\\":2022,\\\"month\\\":0,\\\"day\\\":21},\\\"todayCount\\\":4,\\\"totalCount\\\":4}\",\"installhour\":1642780800000,\"userid\":\"cdae1fdb677748efbf8d000f149618a2\",\"sessionid\":4562428049642,\"appversion\":\"4.0.0\",\"osversion\":\"11\"},\"user\":{\"requestCount\":8,\"requestToReadyTime\":946,\"hasSentAvailabilityFeatures\":true},\"cache\":{\"files\":{\"8daf1c6ed98f3a5e611405014f4ffbc74b34ba6cc9f88a0c32eab5b534aaa940\":{\"fullyDownloaded\":true,\"size\":1246574,\"totalSize\":1246574,\"extension\":\"webm\"},\"be8344e28eda78a17a7c72b0ce828d5da57e5ab8871efc3c0284fed05a69e340\":{\"fullyDownloaded\":true,\"size\":1927,\"totalSize\":-1,\"extension\":\"png\"},\"8439a0eb930900d782d6cec1d4cec7d76f320f3038bf8140c1829f947aa745fe\":{\"fullyDownloaded\":true,\"size\":75544,\"totalSize\":75544,\"extension\":\"jpg\"},\"75d9d06ab6399c8970077567e5919ac64ddab28ae4cc713b88c7925427c56dad\":{\"fullyDownloaded\":true,\"size\":763494,\"totalSize\":763494,\"extension\":\"webm\"},\"72ba0b2698f65fe52e41863bc17a6fe237c4e1b93eb1bc28fa1c51ef917cccf4\":{\"fullyDownloaded\":true,\"size\":8397,\"totalSize\":-1,\"extension\":\"jpg\"},\"0f7755520aa8d6f4cfea157c16cd9253901ce2ce3498b68052c24cdae9da9fa7\":{\"fullyDownloaded\":true,\"size\":181464,\"totalSize\":181464,\"extension\":\"png\"},\"3e911d142fb6cae824047ab650191c912984e0d28e745f70632b3842b0ff3107\":{\"fullyDownloaded\":true,\"size\":3378505,\"totalSize\":3378505,\"extension\":\"webm\"},\"908775f950b9c73d28897f89ee975d44c965b03b301d30e4bd90aab923fde0e5\":{\"fullyDownloaded\":true,\"size\":63630,\"totalSize\":-1,\"extension\":\"png\"},\"b7b1e91e658151c123f09a97cffa8c6d0993f1aa995a105254ca6484f0b8b9b9\":{\"fullyDownloaded\":true,\"size\":140280,\"totalSize\":140280,\"extension\":\"jpg\"},\"4b388ee9c22af10a6e431212294b582b0446cd75dbc4de90797202a054bcb362\":{\"fullyDownloaded\":true,\"size\":114113,\"totalSize\":114113,\"extension\":\"jpg\"},\"8453b617f78aafbd5e2b5d4645a802025a5a5d77ef5c7a733f5bf271f0debe00\":{\"fullyDownloaded\":true,\"size\":134924,\"totalSize\":134924,\"extension\":\"mp4\"},\"54b8286986daadccca34640c2dfe2f3cd3caaed40d475b3976552ea40e5be59d\":{\"fullyDownloaded\":true,\"size\":7216,\"totalSize\":7216,\"extension\":\"png\"},\"053fda185f385334924fec869bf7d58823feae55aa90090eb5cf80ad7e3d6cb1\":{\"fullyDownloaded\":true,\"size\":134174,\"totalSize\":134174,\"extension\":\"png\"},\"c903f40f6a87c083a9deb3302d3020e5fa4e88e8bb67c48b0577c3e4933c454d\":{\"fullyDownloaded\":true,\"size\":105591,\"totalSize\":105591,\"extension\":\"png\"}},\"campaigns\":{\"61deb306ff039da35a7a6a0d-6194d17998127d145cc1e8e4\":{\"8daf1c6ed98f3a5e611405014f4ffbc74b34ba6cc9f88a0c32eab5b534aaa940\":{\"extension\":\"webm\"}},\"61e3c375f1950b7abd849c81-61dfa77c532dfc930cdd56be\":{\"75d9d06ab6399c8970077567e5919ac64ddab28ae4cc713b88c7925427c56dad\":{\"extension\":\"webm\"}},\"5c6adee1a4f58800185757b0-61bb5b4e255c8e7ae26e4a24\":{\"3e911d142fb6cae824047ab650191c912984e0d28e745f70632b3842b0ff3107\":{\"extension\":\"webm\"}},\"000000000000000000000000\":{\"8453b617f78aafbd5e2b5d4645a802025a5a5d77ef5c7a733f5bf271f0debe00\":{\"extension\":\"mp4\"}}}},\"session\":{\"46e78c9a-278f-49a2-9509-1ab8a9814ab3\":{\"ts\":1642784021215},\"f54b72c4-6f2c-422d-ae0d-b474e019be88\":{\"ts\":1642784038564},\"1ea54b76-8867-4fd9-9534-937f00d8fa9b\":{\"ts\":1642784072042},\"282c4e09-8192-47e6-b638-dc1754bbd71e\":{\"ts\":1642784155396},\"f87b0dec-cd32-40b2-b671-c03ff4a2f435\":{\"ts\":1642784519094},\"ba8b86f4-897f-4c4d-b6b2-6d3b89f51b11\":{\"ts\":1642784758483},\"4121f536-ecc5-474d-9c6d-3fe8258d3538\":{\"ts\":1642784774885},\"783a16e4-c95f-47e4-98fb-b5f58630c1df\":{\"ts\":1642784866453}},\"unity\":{\"privacy\":{\"permissions\":{\"ads\":true,\"external\":true,\"gameExp\":true}}}}");
		JsonFlattener jsonFlattener = new JsonFlattener(jsonObject);
		JSONObject result = jsonFlattener.flattenJson(".", Arrays.asList("configuration", "unity"), Collections.singletonList("value"), Collections.singletonList("ts"));
		Assert.assertEquals(true, result.get("configuration.hasInitialized"));
	}
}
