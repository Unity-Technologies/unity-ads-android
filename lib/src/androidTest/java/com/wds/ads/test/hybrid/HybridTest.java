package com.wds.ads.test.hybrid;

import android.os.Build;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.wds.ads.UnityAds;
import com.wds.ads.configuration.Configuration;
import com.wds.ads.configuration.InitializeThread;
import com.wds.ads.misc.Utilities;
import com.wds.ads.properties.ClientProperties;
import com.wds.ads.properties.SdkProperties;
import com.wds.ads.webview.WebView;
import com.wds.ads.webview.bridge.WebViewCallback;
import com.wds.ads.webview.bridge.WebViewExposed;

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

  @WebViewExposed
  public static void onTestResult(Integer failures, @SuppressWarnings("unused") WebViewCallback callback) {
    _failures = failures;
    _resultSemaphore.release();
  }

  @Test
  public void hybridTest() throws MalformedURLException, URISyntaxException, InterruptedException {
    Utilities.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (Build.VERSION.SDK_INT >= 19) {
          WebView.setWebContentsDebuggingEnabled(true);
        }
      }
    });

    UnityAds.setDebugMode(_debugMode);

    ClientProperties.setGameId(_gameId);
    ClientProperties.setApplicationContext(_activityRule.getActivity()
      .getApplicationContext());
    SdkProperties.setTestMode(_testMode);
    SdkProperties.setConfigUrl(SdkProperties.getDefaultConfigUrl("test"));

    Configuration configuration = new Configuration();
    final Class[] apiClassList = {
      com.wds.ads.api.AdUnit.class,
      com.wds.ads.api.Broadcast.class,
      com.wds.ads.api.Cache.class,
      com.wds.ads.api.Connectivity.class,
      com.wds.ads.api.DeviceInfo.class,
      com.wds.ads.api.Listener.class,
      com.wds.ads.api.Storage.class,
      com.wds.ads.api.Sdk.class,
      com.wds.ads.api.Request.class,
      com.wds.ads.api.Resolve.class,
      com.wds.ads.api.VideoPlayer.class,
      com.wds.ads.api.Placement.class,
      com.wds.ads.api.Intent.class,
      com.wds.ads.test.hybrid.HybridTest.class,
    };

    configuration.setWebAppApiClassList(apiClassList);
    InitializeThread.initialize(configuration);

    if (!_resultSemaphore.tryAcquire(5, TimeUnit.MINUTES)) {
      fail("onHybridTestResult did not arrive");
    }

    assertEquals("Hybrid test had failures, check device log for details", Integer.valueOf(0), _failures);
  }

}
