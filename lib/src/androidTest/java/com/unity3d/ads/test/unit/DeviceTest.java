package com.unity3d.ads.test.unit;

import android.media.AudioManager;
import android.opengl.GLES11;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.ConditionVariable;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.SurfaceView;
import android.view.ViewGroup;

import com.unity3d.ads.adunit.AdUnitActivity;
import com.unity3d.ads.device.Device;
import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.misc.Utilities;
import com.unity3d.ads.properties.ClientProperties;
import com.unity3d.ads.properties.SdkProperties;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class DeviceTest {
	@BeforeClass
	public static void prepareTests () {
		ClientProperties.setApplicationContext(InstrumentationRegistry.getContext());
	}

	@Test
	public void testApiLevel () {
		assertTrue("Expected SDK_INT should be > 9", Device.getApiLevel() >= 9);
	}

	@Test
	public void testScreenLayout () {
		assertTrue("Expected screenLayout property be something else than undefined (0)", Device.getScreenLayout() > 0);
	}

	@Test
	public void testManufacturer () {
		assertNotNull("Hardware version should not ever be null", Device.getManufacturer());
		assertFalse("Hardware version should not ever be empty", Device.getManufacturer().isEmpty());
	}

	@Test
	public void testModel () {
		assertNotNull("Hardware version should not ever be null", Device.getModel());
		assertFalse("Hardware version should not ever be empty", Device.getModel().isEmpty());
	}

	@Test
	public void testAndroidId () {
		assertNotNull("AndroidID should never be null", Device.getAndroidId());
		assertTrue("AndroidID length should be 8 chars or more (64bit)", Device.getAndroidId().length() >= 8);
	}

	@Test
	public void testIsAppInstalled () {
		List<Map<String, Object>> installedPackages = Device.getInstalledPackages(false);
		assertNotNull("Installed packages should not be null", installedPackages);
		assertTrue("Installed packages contained: " + installedPackages.get(0).get("name") + " so it should also be installed", Device.isAppInstalled((String)installedPackages.get(0).get("name")));
	}

	@Test
	public void testInstalledPackages () {
		List<Map<String, Object>> installedPackages = Device.getInstalledPackages(false);
		assertNotNull("Installed packages should not be null", installedPackages);
		assertTrue("Installed packages should contain at least 1 entry", installedPackages.size() > 0);
	}

	@Test
	public void testIsActiveNetworkConnected () {
		assertTrue("Active network should be connected", Device.isActiveNetworkConnected());
	}

	@Test
	public void testScreenDensity () {
		assertTrue("DPI Density of the screen should be > 100 (minimum by Android should be 120)", Device.getScreenDensity() > 100);
	}

	@Test
	public void testGetNetworkType () {
		assertTrue("Network type must not be negative", Device.getNetworkType() > -1);
	}

	@Test
	public void testIsNetworkMetered () {
		assertNotNull("Metered network should not be null", Device.getNetworkMetered());
	}

	@Test
	public void testNetworkOperatorName () {
		assertNotNull("Expected network operator name to be something else than null", Device.getNetworkOperatorName());
	}

	@Test
	public void testGetFreeSpace () {
		assertTrue("There should be space left in the SDK cache directory", Device.getFreeSpace(SdkProperties.getCacheDirectory()) > 0);
		assertEquals("Invalid file should return -1 as result", -1, Device.getFreeSpace(new File("/fkjdhsfdsfd/fdsfdsfds/")));
		assertEquals("Null file should return -1 as result", -1, Device.getFreeSpace(null));
	}

	@Test
	public void testGetTotalSpace () {
		assertTrue("The total space on the cache directory should be more than 0", Device.getTotalSpace(SdkProperties.getCacheDirectory()) > 0);
		assertEquals("Invalid file should return -1 as result", -1, Device.getTotalSpace(new File("/fkjdhsfdsfd/fdsfdsfds/")));
		assertEquals("Null file should return -1 as result", -1, Device.getTotalSpace(null));
	}

	@Test
	@Ignore
	public void testIsWiredHeadSetOn () {
		assertFalse("Wired headset should not be connected in test device", Device.isWiredHeadsetOn());
	}

	@Test
	public void tetGetSystemProperty () {
		assertNotNull("System property 'java.class.path' should always be defined", Device.getSystemProperty("java.class.path", null));
		assertNull("System property 'test.non.existing' should not exist and be null", Device.getSystemProperty("test.non.existing", null));
		assertEquals("Fetching invalid system property should return the defaultValue", "Test", Device.getSystemProperty("test.non.existing", "Test"));
	}

	@Test
	public void testGetRingerMode () {
		int[] expected = {AudioManager.RINGER_MODE_NORMAL, AudioManager.RINGER_MODE_SILENT, AudioManager.RINGER_MODE_VIBRATE};
		int ringerMode = Device.getRingerMode();
		boolean found = false;

		for (int value : expected) {
			if (value == ringerMode) {
				found = true;
				break;
			}
		}

		assertTrue("Should have found the received ringer mode in the expected ringerMode values", found);
	}

	@Test
	public void testScreenBrightness () throws Exception {
		assertTrue("Screen brightness should be equal or more than 0", Device.getScreenBrightness() >= 0);
	}

	@Test
	public void testGetAdbStatus () throws Exception {
		assertTrue("Adb status should be true", Device.isAdbEnabled());
	}

	@Test
	public void testGetGLVersion () throws Exception {
		assertNotNull("GLES version should not be null", Device.getGLVersion());
	}

	@Test
	public void testGetBoard () throws Exception {
		assertNotNull("Board should not be null", Device.getBoard());
	}

	@Test
	public void testGetBootloader () throws Exception {
		assertNotNull("Bootloader should not be null", Device.getBootloader());
	}

	@Test
	public void testGetDevice () throws Exception {
		assertNotNull("Device should not be null", Device.getDevice());
	}

	@Test
	public void testGetHardware () throws Exception {
		assertNotNull("Hardware should not be null", Device.getHardware());
	}

	@Test
	public void testGetHost () throws Exception {
		assertNotNull("Host should not be null", Device.getHost());
	}

	@Test
	public void testGetProduct () throws Exception {
		assertNotNull("Product should not be null", Device.getProduct());
	}

	@Test
	public void testGetFingerprint () throws Exception {
		assertNotNull("Fingerprint should not be null", Device.getFingerprint());
	}

	@Test
	public void testGetApkDigest () throws Exception {
		String digest = Device.getApkDigest();

		assertNotNull("Apk Digest should not be null", digest);
		assertEquals("Wrong size", 64, digest.length());
		assertTrue("Contains illegal characters", digest.matches("^[0-9a-fA-F]+$"));
	}

	@Test
	public void testGetCertificateFingerprint () throws Exception {
		assertNotNull("Certificate fingerprint should not be null", Device.getCertificateFingerprint());
	}

	@Test
	public void testGetSupportedAbis () throws Exception {
		ArrayList<String> supportedAbis = Device.getSupportedAbis();

		assertNotNull("List of supported abis should not be null", supportedAbis);
		assertTrue("Number of supported abis should be larger than 0", supportedAbis.size() > 0);
	}

	@Test
	public void testGetProcessInfo () throws Exception {
		Map<String, String> data = Device.getProcessInfo();

		assertNotNull("Stats should not be null", data.get("stat"));
		assertNotEquals("Stats should not be empty", data.get("stat"), "");
	}

	@Test
	public void testGetCPUCount() {
		assertTrue("Number of CPUs should be greater than 0", Device.getCPUCount() > 0);
	}

	@Test
	public void testGetUptime() {
		assertTrue("Uptime should be greater than 0", Device.getUptime() > 0);
	}

	@Test
	public void testGetElapsedRealtime() {
		assertTrue("Elapsed realtime should be greater than 0", Device.getElapsedRealtime() > 0);
	}
}