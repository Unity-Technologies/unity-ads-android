package com.unity3d.ads.test.hybrid;

import android.os.Build;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.unity3d.ads.UnityAds;
import com.unity3d.ads.configuration.Configuration;
import com.unity3d.ads.configuration.InitializeThread;
import com.unity3d.ads.misc.Utilities;
import com.unity3d.ads.properties.ClientProperties;
import com.unity3d.ads.properties.SdkProperties;
import com.unity3d.ads.webview.WebView;
import com.unity3d.ads.webview.bridge.WebViewCallback;
import com.unity3d.ads.webview.bridge.WebViewExposed;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class HybridTest {

  private static final String _gameId = "14851";
  private static final boolean _testMode = true;
  private static final boolean _debugMode = true;

  private static final Semaphore _resultSemaphore = new Semaphore(0);

  private static Integer _failures = Integer.MAX_VALUE;

  @Rule
  public final ActivityTestRule<HybridTestActivity> _activityRule = new ActivityTestRule<>(HybridTestActivity.class);

  @Test
  public void hybridTest() throws MalformedURLException, URISyntaxException, InterruptedException {
    Utilities.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if(Build.VERSION.SDK_INT >= 19) {
          WebView.setWebContentsDebuggingEnabled(true);
        }
      }
    });

    UnityAds.setDebugMode(_debugMode);

    ClientProperties.setGameId(_gameId);
    ClientProperties.setApplicationContext(_activityRule.getActivity().getApplicationContext());
    SdkProperties.setTestMode(_testMode);
    SdkProperties.setConfigUrl(SdkProperties.getDefaultConfigUrl("test"));

    Configuration configuration = new Configuration();
    final Class[] apiClassList = {
      com.unity3d.ads.api.AdUnit.class,
      com.unity3d.ads.api.Broadcast.class,
      com.unity3d.ads.api.Cache.class,
      com.unity3d.ads.api.Connectivity.class,
      com.unity3d.ads.api.DeviceInfo.class,
      com.unity3d.ads.api.Listener.class,
      com.unity3d.ads.api.Storage.class,
      com.unity3d.ads.api.Sdk.class,
      com.unity3d.ads.api.Request.class,
      com.unity3d.ads.api.Resolve.class,
      com.unity3d.ads.api.VideoPlayer.class,
      com.unity3d.ads.api.Placement.class,
      com.unity3d.ads.api.Intent.class,
      com.unity3d.ads.api.WebPlayer.class,
      com.unity3d.ads.test.hybrid.HybridTest.class,
      com.unity3d.ads.api.Lifecycle.class,
      com.unity3d.ads.api.Preferences.class,
      com.unity3d.ads.api.Purchasing.class,
      com.unity3d.ads.api.SensorInfo.class,
      com.unity3d.ads.test.hybrid.HybridTest.class
    };

    configuration.setWebAppApiClassList(apiClassList);
    InitializeThread.initialize(configuration);

    if(!_resultSemaphore.tryAcquire(10, TimeUnit.MINUTES)) {
      fail("onHybridTestResult did not arrive");
    }

    assertEquals("Hybrid test had failures, check device log for details", Integer.valueOf(0), _failures);
  }

  @WebViewExposed
  public static void onTestResult(Integer failures, @SuppressWarnings("unused") WebViewCallback callback) {
    _failures = failures;
    _resultSemaphore.release();
  }

}
