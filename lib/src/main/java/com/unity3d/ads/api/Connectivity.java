package com.unity3d.ads.api;

import com.unity3d.ads.connectivity.ConnectivityMonitor;
import com.unity3d.ads.webview.bridge.WebViewCallback;
import com.unity3d.ads.webview.bridge.WebViewExposed;

public class Connectivity {

  @WebViewExposed
  public static void setConnectionMonitoring(Boolean monitoring, WebViewCallback callback) {
    ConnectivityMonitor.setConnectionMonitoring(monitoring);
    callback.invoke();
  }

}
