# Keep filenames and line numbers for stack traces
-keepattributes SourceFile,LineNumberTable

# Keep JavascriptInterface for WebView bridge
-keepattributes JavascriptInterface

# Sometimes keepattributes is not enough to keep annotations
-keep class android.webkit.JavascriptInterface {
   *;
}

# Keep all classes in Unity Ads package
-keep class com.unity3d.ads.** {
   *;
}

# Keep all classes in Unity Services package
-keep class com.unity3d.services.** {
   *;
}

-keep class com.google.android.gms.ads.initialization.** {
        *;
}

-keep class com.google.android.gms.ads.MobileAds {
        *;
}

-dontwarn com.google.ads.mediation.admob.*
-dontwarn com.google.android.gms.ads.**