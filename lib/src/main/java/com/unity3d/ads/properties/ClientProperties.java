package com.unity3d.ads.properties;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

import com.unity3d.ads.IUnityAdsListener;
import com.unity3d.ads.log.DeviceLog;

import java.io.ByteArrayInputStream;
import java.lang.ref.WeakReference;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.security.auth.x500.X500Principal;

public class ClientProperties {
	private static final X500Principal DEBUG_CERT = new X500Principal("CN=Android Debug,O=Android,C=US");
	private static WeakReference<Activity> _activity;
	private static Context _applicationContext;
	private static Application _application;
	private static IUnityAdsListener _listener;
	private static String _gameId;

	public static Activity getActivity () {
		return _activity.get();
	}

	public static void setActivity (Activity activity) {
		_activity = new WeakReference<>(activity);
	}

	public static Context getApplicationContext () {
		return _applicationContext;
	}

	public static void setApplicationContext (Context context) {
		_applicationContext = context;
	}

	public static Application getApplication () {
		return _application;
	}

	public static void setApplication (Application application) {
		_application = application;
	}

	public static IUnityAdsListener getListener () {
		return _listener;
	}

	public static void setListener (IUnityAdsListener listener) {
		_listener = listener;
	}

	public static String getGameId () {
		return _gameId;
	}

	public static void setGameId (String gameId) {
		_gameId = gameId;
	}

	public static String getAppName() {
		return _applicationContext.getPackageName();
	}

	public static String getAppVersion() {
		String pkgName = ClientProperties.getApplicationContext().getPackageName();
		PackageManager pm = ClientProperties.getApplicationContext().getPackageManager();

		try {
			return pm.getPackageInfo(pkgName, 0).versionName;
		} catch(PackageManager.NameNotFoundException e) {
			DeviceLog.exception("Error getting package info", e);
			return null;
		}
	}

	public static boolean isAppDebuggable() {
		boolean debuggable = false;
		boolean couldNotGetApplicationInfo = false;
		PackageManager pm;
		String pkgName;

		if (ClientProperties.getApplicationContext() != null) {
			pm = ClientProperties.getApplicationContext().getPackageManager();
			pkgName = ClientProperties.getApplicationContext().getPackageName();
		}
		else {
			return false;
		}

		try {
			ApplicationInfo appinfo = pm.getApplicationInfo(pkgName, 0);
			if (0 != (appinfo.flags &= ApplicationInfo.FLAG_DEBUGGABLE)) {
				debuggable = true;
			}
		}
		catch (PackageManager.NameNotFoundException e) {
			DeviceLog.exception("Could not find name", e);
			couldNotGetApplicationInfo = true;
		}

		if (couldNotGetApplicationInfo) {
			try {
				PackageInfo pinfo = pm.getPackageInfo(pkgName, PackageManager.GET_SIGNATURES);
				Signature[] signatures = pinfo.signatures;

				for (Signature signature : signatures) {
					CertificateFactory cf = CertificateFactory.getInstance("X.509");
					ByteArrayInputStream stream = new ByteArrayInputStream(signature.toByteArray());
					X509Certificate cert = (X509Certificate) cf.generateCertificate(stream);
					debuggable = cert.getSubjectX500Principal().equals(DEBUG_CERT);
					if (debuggable)
						break;
				}
			}
			catch (PackageManager.NameNotFoundException e) {
				DeviceLog.exception("Could not find name", e);
			}
			catch (CertificateException e) {
				DeviceLog.exception("Certificate exception", e);
			}
		}

		return debuggable;
	}
}
