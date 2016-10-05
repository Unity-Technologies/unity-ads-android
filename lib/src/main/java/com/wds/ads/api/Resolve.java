package com.wds.ads.api;

import com.wds.ads.request.IResolveHostListener;
import com.wds.ads.request.ResolveHostError;
import com.wds.ads.request.ResolveHostEvent;
import com.wds.ads.request.WebRequestThread;
import com.wds.ads.webview.WebViewApp;
import com.wds.ads.webview.WebViewEventCategory;
import com.wds.ads.webview.bridge.WebViewCallback;
import com.wds.ads.webview.bridge.WebViewExposed;

/**
 * Created by rikshot on 24/03/16.
 */
public class Resolve {

  @WebViewExposed
  public static void resolve (final String id, String host, WebViewCallback callback) {
    if (WebRequestThread.resolve(host, new IResolveHostListener() {
      @Override
      public void onResolve(String host, String address) {
        if (WebViewApp.getCurrentApp() != null) {
          WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.RESOLVE, ResolveHostEvent.COMPLETE, id, host, address);
        }
      }

      @Override
      public void onFailed(String host, ResolveHostError error, String errorMessage) {
        if (WebViewApp.getCurrentApp() != null) {
          WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.RESOLVE, ResolveHostEvent.FAILED, id, host, error.name(), errorMessage);
        }
      }
    })) {
      callback.invoke(id);
    }
    else {
      callback.error(ResolveHostError.INVALID_HOST, id);
    }
  }

}
