# Keep filenames and line numbers for stack traces
-keepattributes SourceFile,LineNumberTable

# Keep JavascriptInterface for WebView bridge
-keepattributes JavascriptInterface

-keepattributes Signature,Exceptions

# Keep the parameter names for our Public APIs
-keepparameternames

-keeppackagenames

# Keep Javascript annotation class name
-keep public class android.webkit.JavascriptInterface

# Keep publicly exposed SDK APIs.
-keep public class com.unity3d.ads.* { public *; }
-keep public class com.unity3d.ads.metadata.* { *; }
-keep public class com.unity3d.services.banners.* { public *; }

# Keep internal libraries from being removed in optimization.
-keep public class com.unity3d.scar.** { public *; }

# Keep webview accessed APIs
-keep public class com.unity3d.services.**.api.** { public *; }
-keep public class com.unity3d.services.**.configuration.** { public *; }
-keep public class com.unity3d.services.core.webview.bridge.WebViewExposed

-keep public class com.unity3d.services.core.webview.WebView

# Don't warn about compile time depencencies
-dontwarn com.google.ar.core.**
-dontwarn com.google.android.gms.ads.**