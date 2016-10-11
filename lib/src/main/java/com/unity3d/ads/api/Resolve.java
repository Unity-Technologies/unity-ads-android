package com.unity3d.ads.api;

import com.unity3d.ads.request.IResolveHostListener;
import com.unity3d.ads.request.ResolveHostError;
import com.unity3d.ads.request.ResolveHostEvent;
import com.unity3d.ads.request.WebRequestThread;
import com.unity3d.ads.webview.WebViewApp;
import com.unity3d.ads.webview.WebViewEventCategory;
import com.unity3d.ads.webview.bridge.WebViewCallback;
import com.unity3d.ads.webview.bridge.WebViewExposed;

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
