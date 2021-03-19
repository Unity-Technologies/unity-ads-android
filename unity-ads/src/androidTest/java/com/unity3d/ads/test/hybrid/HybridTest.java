package com.unity3d.ads.test.hybrid;

import android.os.Build;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.unity3d.ads.UnityAds;
import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.configuration.InitializeThread;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.services.core.properties.SdkProperties;
import com.unity3d.services.core.webview.WebView;
import com.unity3d.services.core.webview.bridge.WebViewCallback;
import com.unity3d.services.core.webview.bridge.WebViewExposed;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
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

    HybridTestConfiguration configuration = new HybridTestConfiguration();
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

  private class HybridTestConfiguration extends Configuration {
    @Override
    public String[] getModuleConfigurationList () {
      ArrayList<String> moduleConfigurationList = new ArrayList<>(Arrays.asList(super.getModuleConfigurationList()));
      moduleConfigurationList.add("com.unity3d.ads.test.hybrid.HybridTestModuleConfiguration");
      return moduleConfigurationList.toArray(new String[moduleConfigurationList.size()]);
    }
  }

}