package com.unity3d.ads.test.unit;

import android.util.Log;

import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.misc.Utilities;

import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UtilitiesTest {
    @Test
    public void testSha256() throws Exception {
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", Utilities.Sha256(""));
    }

    @Test
	public void testMergeJson() throws Exception {
		JSONObject objectOne = new JSONObject();
		JSONObject objectOneSub = new JSONObject();
		JSONObject objectSubOneOne = new JSONObject();
		JSONObject objectSubOneOneOne = new JSONObject();
		objectSubOneOneOne.put("subsubone", 111);
		objectSubOneOne.put("subone", 11);
		objectSubOneOne.put("sub", objectSubOneOneOne);
		objectOneSub.put("one", 1);
		objectOneSub.put("sub", objectSubOneOne);
		objectOneSub.put("override", "primary");
		objectOne.put("test", objectOneSub);

		JSONObject objectTwo = new JSONObject();
		JSONObject objectTwoSub = new JSONObject();
		JSONObject objectSubTwoTwo = new JSONObject();
		JSONObject objectSubTwoTwoTwo = new JSONObject();
		objectSubTwoTwoTwo.put("subsubtwo", 222);
		objectSubTwoTwo.put("subtwo", 22);
		objectSubTwoTwo.put("sub", objectSubTwoTwoTwo);
		objectTwoSub.put("two", 2);
		objectTwoSub.put("sub", objectSubTwoTwo);
		objectTwoSub.put("override", "secondary");
		objectTwo.put("test", objectTwoSub);

		JSONObject resultObject = Utilities.mergeJsonObjects(objectOne, objectTwo);

		Log.d("UnityAds", resultObject.toString());

		JSONObject testObject = resultObject.getJSONObject("test");

		assertEquals("Incorrect 'one' value", 1, testObject.getInt("one"));
		assertEquals("Incorrect 'two' value", 2, testObject.getInt("two"));

		JSONObject subObject = testObject.getJSONObject("sub");

		assertEquals("Incorrect 'subone' value", 11, subObject.getInt("subone"));
		assertEquals("Incorrect 'subtwo' value", 22, subObject.getInt("subtwo"));

		JSONObject subSubObject = subObject.getJSONObject("sub");

		assertEquals("Incorrect 'subsubone' value", 111, subSubObject.getInt("subsubone"));
		assertEquals("Incorrect 'subsubtwo' value", 222, subSubObject.getInt("subsubtwo"));

		assertEquals("Incorrect 'override' value", "primary", testObject.getString("override"));
	}
}