package com.unity3d.ads.test.unit;

import android.support.test.InstrumentationRegistry;
import android.util.Log;

import com.unity3d.ads.misc.Utilities;
import com.unity3d.ads.properties.ClientProperties;
import com.unity3d.ads.properties.SdkProperties;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class UtilitiesTest {
	@Before
	public void setup() throws IOException {
		ClientProperties.setApplicationContext(InstrumentationRegistry.getTargetContext());
		assertNotEquals("Cache directory has not been properly initialized", null, SdkProperties.getCacheDirectory());

		File tesfile_large = new File(SdkProperties.getCacheDirectory() + "/" + SdkProperties.getCacheFilePrefix() + "testfile_large.dat");
		tesfile_large.delete();

		File tesfile_small = new File(SdkProperties.getCacheDirectory() + "/" + SdkProperties.getCacheFilePrefix() + "testfile_small.dat");
		tesfile_small.delete();

		// Write large test file to cache directory
		File largef = new File(SdkProperties.getCacheDirectory() + "/" + SdkProperties.getCacheFilePrefix() + "testfile_large.dat");
		FileOutputStream lfos = new FileOutputStream(largef, true);
		for (int i = 0; i < 56789; i++) {
			lfos.write((int)(Math.random() * 128) + 1);
		}
		lfos.flush();
		lfos.close();

		File smallf = new File(SdkProperties.getCacheDirectory() + "/" + SdkProperties.getCacheFilePrefix() + "testfile_small.dat");
		FileOutputStream sfos = new FileOutputStream(smallf, true);
		for (int i = 0; i < 1024; i++) {
			sfos.write((int)(Math.random() * 128) + 1);
		}
		sfos.flush();
		sfos.close();
	}


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

	@Test
	public void testSha256Stream() throws Exception {
		assertHash(10240, "88278aa898609ddff1418ac55011741c563d3e3cd0d70bc07c407a3a882d8004");
		assertHash(8192, "9b4cd9b4977133275a2fc9ab7ec25101cfe5273808837a2490437871614f56ab");
		assertHash(2048, "b07cbef157ea4e7bf4e233f3e766c668bf93578e9954c64fcd6ec3d9f4c6f4f4");
		assertHash(1, "5feceb66ffc86f38d952786c6d696c79c2dbc239dd4e91b46729d73a27fb57e9");
		assertHash(0, "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
	}

	@Test
	public void testReadFileBytes() throws Exception {
		byte[] large_data = Utilities.readFileBytes(new File(SdkProperties.getCacheDirectory() + "/" + SdkProperties.getCacheFilePrefix() + "testfile_large.dat"));
		byte[] small_data = Utilities.readFileBytes(new File(SdkProperties.getCacheDirectory() + "/" + SdkProperties.getCacheFilePrefix() + "testfile_small.dat"));

		assertEquals("Incorrect read large file data size", 56789, large_data.length);
		assertEquals("Incorrect read small file data size", 1024, small_data.length);
	}

	public void assertHash(int size, String sha256) throws Exception {
		byte[] buffer = new byte[size];

		for(int i = 0; i < buffer.length; i++ ) {
			buffer[i] = (byte)((byte)'0' + (byte)(i % 9));
		}

		ByteArrayInputStream inputStream = new ByteArrayInputStream(buffer);
		assertEquals(String.format("Hash for buffer size %d is wrong", size), sha256, Utilities.Sha256(inputStream));
	}
}