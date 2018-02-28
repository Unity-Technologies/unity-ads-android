package com.unity3d.ads.test.unit;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.unity3d.ads.preferences.AndroidPreferences;
import com.unity3d.ads.properties.ClientProperties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class PreferencesTest {
	String testSettings = "unity.test.preferences";
	String nonExistingSettings = "unity.test.does.not.exist";
	String stringKey = "test.string";
	String intKey = "test.int";
	String longKey = "test.long";
	String boolKey = "test.boolean";
	String floatKey = "test.float";
	String nonExistingKey = "non.existing.key";

	@Before
	public void setup() {
		ClientProperties.setApplicationContext(InstrumentationRegistry.getTargetContext());

		deleteKey(stringKey);
		deleteKey(intKey);
		deleteKey(longKey);
		deleteKey(boolKey);
		deleteKey(floatKey);
	}

	@Test
	public void testPreferencesStringGetter() {
		String testValue = "testString";

		assertFalse("Preferences contained a key before key was written", AndroidPreferences.hasKey(testSettings, stringKey));

		SharedPreferences.Editor editor = getEditor();
		editor.putString(stringKey, testValue);
		editor.commit();

		assertTrue("Preferences did not contain previously committed value", AndroidPreferences.hasKey(testSettings, stringKey));
		assertEquals("Proper string value was not read from SharedPreferences", AndroidPreferences.getString(testSettings, stringKey), testValue);
		assertNull("Non-null value returned for a non-existing key", AndroidPreferences.getString(testSettings, nonExistingKey));
		assertNull("Non-null value returned for a non-existing settings", AndroidPreferences.getString(nonExistingSettings, stringKey));
	}

	@Test
	public void testPreferencesIntGetter() {
		int testValue = 12345;

		assertFalse("Preferences contained a key before key was written", AndroidPreferences.hasKey(testSettings, intKey));

		SharedPreferences.Editor editor = getEditor();
		editor.putInt(intKey, testValue);
		editor.commit();

		assertTrue("Preferences did not contain previously committed value", AndroidPreferences.hasKey(testSettings, intKey));
		assertEquals("Proper int value was not read from SharedPreferences", AndroidPreferences.getInteger(testSettings, intKey), Integer.valueOf(testValue));
		assertNull("Non-null value returned for a non-existing key", AndroidPreferences.getInteger(testSettings, nonExistingKey));
		assertNull("Non-null value returned for a non-existing settings", AndroidPreferences.getInteger(nonExistingSettings, intKey));
	}

	@Test
	public void testPreferencesLongGetter() {
		long testValue = 12345678;

		assertFalse("Preferences contained a key before key was written", AndroidPreferences.hasKey(testSettings, longKey));

		SharedPreferences.Editor editor = getEditor();
		editor.putLong(longKey, testValue);
		editor.commit();

		assertTrue("Preferences did not contain previously committed value", AndroidPreferences.hasKey(testSettings, longKey));
		assertEquals("Proper long value was not read from SharedPreferences", AndroidPreferences.getLong(testSettings, longKey), Long.valueOf(testValue));
		assertNull("Non-null value returned for a non-existing key", AndroidPreferences.getLong(testSettings, nonExistingKey));
		assertNull("Non-null value returned for a non-existing settings", AndroidPreferences.getLong(nonExistingSettings, longKey));
	}

	@Test
	public void testPreferencesBooleanGetter() {
		boolean testValue = true;

		assertFalse("Preferences contained a key before key was written", AndroidPreferences.hasKey(testSettings, boolKey));

		SharedPreferences.Editor editor = getEditor();
		editor.putBoolean(boolKey, testValue);
		editor.commit();

		assertTrue("Preferences did not contain previously committed value", AndroidPreferences.hasKey(testSettings, boolKey));
		assertEquals("Proper boolean value was not read from SharedPreferences", AndroidPreferences.getBoolean(testSettings, boolKey), Boolean.valueOf(testValue));
		assertNull("Non-null value returned for a non-existing key", AndroidPreferences.getBoolean(testSettings, nonExistingKey));
		assertNull("Non-null value returned for a non-existing settings", AndroidPreferences.getBoolean(nonExistingSettings, boolKey));
	}

	@Test
	public void testPreferencesFloatGetter() {
		float testValue = 1.2345f;

		assertFalse("Preferences contained a key before key was written", AndroidPreferences.hasKey(testSettings, floatKey));

		SharedPreferences.Editor editor = getEditor();
		editor.putFloat(floatKey, testValue);
		editor.commit();

		assertTrue("Preferences did not contain previously committed value", AndroidPreferences.hasKey(testSettings, floatKey));
		assertEquals("Proper float value was not read from SharedPreferences", AndroidPreferences.getFloat(testSettings, floatKey), Float.valueOf(testValue));
		assertNull("Non-null value returned for a non-existing key", AndroidPreferences.getFloat(testSettings, nonExistingKey));
		assertNull("Non-null value returned for a non-existing settings", AndroidPreferences.getFloat(nonExistingSettings, floatKey));
	}

	@Test
	public void testPreferencesStringSetter() {
		String testValue = "testString";

		assertFalse("Preferences contained a key before key was written", AndroidPreferences.hasKey(testSettings, stringKey));

		AndroidPreferences.setString(testSettings, stringKey, testValue);

		assertTrue("Preferences did not contain previously committed value", AndroidPreferences.hasKey(testSettings, stringKey));
		assertEquals("Proper string value was not read from SharedPreferences", getPrefs().getString(stringKey, ""), testValue);
	}

	@Test
	public void testPreferencesIntSetter() {
		int testValue = 12345;

		assertFalse("Preferences contained a key before key was written", AndroidPreferences.hasKey(testSettings, intKey));

		AndroidPreferences.setInteger(testSettings, intKey, testValue);

		assertTrue("Preferences did not contain previously committed value", AndroidPreferences.hasKey(testSettings, intKey));
		assertEquals("Proper int value was not read from SharedPreferences", (Integer)getPrefs().getInt(intKey, -1), Integer.valueOf(testValue));
	}

	@Test
	public void testPreferencesLongSetter() {
		long testValue = 12345678;

		assertFalse("Preferences contained a key before key was written", AndroidPreferences.hasKey(testSettings, longKey));

		AndroidPreferences.setLong(testSettings, longKey, testValue);

		assertTrue("Preferences did not contain previously committed value", AndroidPreferences.hasKey(testSettings, longKey));
		assertEquals("Proper long value was not read from SharedPreferences", (Long)getPrefs().getLong(longKey, -1), Long.valueOf(testValue));
	}

	@Test
	public void testPreferencesBooleanSetter() {
		boolean testValue = true;

		assertFalse("Preferences contained a key before key was written", AndroidPreferences.hasKey(testSettings, boolKey));

		AndroidPreferences.setBoolean(testSettings, boolKey, testValue);

		assertTrue("Preferences did not contain previously committed value", AndroidPreferences.hasKey(testSettings, boolKey));
		assertEquals("Proper boolean value was not read from SharedPreferences", getPrefs().getBoolean(boolKey, false), Boolean.valueOf(testValue));
	}

	@Test
	public void testPreferencesFloatSetter() {
		float testValue = 1.2345f;

		assertFalse("Preferences contained a key before key was written", AndroidPreferences.hasKey(testSettings, floatKey));

		AndroidPreferences.setFloat(testSettings, floatKey, new Double(testValue));

		assertTrue("Preferences did not contain previously committed value", AndroidPreferences.hasKey(testSettings, floatKey));
		assertEquals("Proper float value was not read from SharedPreferences", (Float)getPrefs().getFloat(floatKey, Float.NaN), Float.valueOf(testValue));
	}

	@Test
	public void testPreferencesTypeErrors() {
		SharedPreferences.Editor editor = getEditor();
		editor.putString(stringKey, "testString");
		editor.putInt(intKey, 12345);
		editor.commit();

		assertNull("Type mismatch did not return null", AndroidPreferences.getString(testSettings, intKey));
		assertNull("Type mismatch did not return null", AndroidPreferences.getInteger(testSettings, stringKey));
		assertNull("Type mismatch did not return null", AndroidPreferences.getLong(testSettings, stringKey));
		assertNull("Type mismatch did not return null", AndroidPreferences.getBoolean(testSettings, stringKey));
		assertNull("Type mismatch did not return null", AndroidPreferences.getFloat(testSettings, stringKey));
	}

	@Test
	public void testPreferencesRemoveKey() {
		SharedPreferences.Editor editor = getEditor();
		editor.putString(stringKey, "testString");
		editor.commit();

		assertTrue("Preferences did not contain test string value", AndroidPreferences.hasKey(testSettings, stringKey));

		AndroidPreferences.removeKey(testSettings, stringKey);

		assertFalse("Preferences has a key after is was removed", AndroidPreferences.hasKey(testSettings, stringKey));
	}

	private SharedPreferences getPrefs() {
		return InstrumentationRegistry.getTargetContext().getSharedPreferences(testSettings, Context.MODE_PRIVATE);
	}

	private SharedPreferences.Editor getEditor() {
		return InstrumentationRegistry.getTargetContext().getSharedPreferences(testSettings, Context.MODE_PRIVATE).edit();
	}

	private void deleteKey(String key) {
		SharedPreferences prefs = getPrefs();
		if(prefs != null) {
			if(prefs.contains(key)) {
				SharedPreferences.Editor editor = getEditor();
				editor.remove(key);
				editor.commit();
			}
		}
	}
}
