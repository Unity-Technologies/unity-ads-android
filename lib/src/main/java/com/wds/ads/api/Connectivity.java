package com.wds.ads.api;

import com.wds.ads.connectivity.ConnectivityMonitor;
import com.wds.ads.webview.bridge.WebViewCallback;
import com.wds.ads.webview.bridge.WebViewExposed;

public class Connectivity {

  @WebViewExposed
  public static void setConnectionMonitoring(Boolean monitoring, WebViewCallback callback) {
    ConnectivityMonitor.setConnectionMonitoring(monitoring);
    callback.invoke();
  }

}
