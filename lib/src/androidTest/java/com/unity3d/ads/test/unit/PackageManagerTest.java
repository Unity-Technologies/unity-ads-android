package com.unity3d.ads.test.unit;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.test.runner.AndroidJUnit4;
import android.test.mock.MockContext;
import android.test.mock.MockPackageManager;

import com.unity3d.ads.device.Device;
import com.unity3d.ads.properties.ClientProperties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class PackageManagerTest {
	private final String existingPkg = "com.example.package.exists";
	private final String nonExistingPkg = "com.example.package.does.not.exist";

	@Before
	public void setup() {
		ClientProperties.setApplicationContext(new MockContext() {
			@Override
			public PackageManager getPackageManager() {
				return new TestPackageManager();
			}
		});
	}

	@Test
	public void testIsAppInstalled() {
		assertTrue("Package manager test: existing app was not found in installed apps", Device.isAppInstalled(existingPkg));
		assertFalse("Package manager test: non-exisiting app was found in installed apps", Device.isAppInstalled(nonExistingPkg));
	}

	private class TestPackageManager extends MockPackageManager {
		@Override
		public PackageInfo getPackageInfo(String packageName, int flags) {
			if(existingPkg.equals(packageName)) {
				PackageInfo info = new PackageInfo();
				info.packageName = existingPkg;
				return info;
			} else {
				return null;
			}
		}
	}
}