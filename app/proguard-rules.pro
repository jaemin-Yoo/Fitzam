# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Google Drive API client (JSON reflection)
-keepclassmembers class * {
    @com.google.api.client.util.Key <fields>;
}
-keepclassmembers class * extends com.google.api.client.json.GenericJson {
    <fields>;
    <methods>;
}
-keep class com.google.api.client.** { *; }
-keep class com.google.api.services.drive.** { *; }
-keep class com.google.api.client.util.** { *; }

# Apache HttpClient (not used; suppress missing class warnings from Google API client)
-dontwarn com.google.api.client.http.apache.v2.ApacheHttpTransport
-dontwarn org.apache.http.config.Registry
-dontwarn org.apache.http.config.RegistryBuilder
-dontwarn org.apache.http.conn.DnsResolver
-dontwarn org.apache.http.conn.HttpClientConnectionManager
-dontwarn org.apache.http.conn.HttpConnectionFactory
-dontwarn org.apache.http.conn.SchemePortResolver
-dontwarn org.apache.http.conn.socket.PlainConnectionSocketFactory
-dontwarn org.apache.http.conn.ssl.SSLConnectionSocketFactory
-dontwarn org.apache.http.impl.client.CloseableHttpClient
-dontwarn org.apache.http.impl.client.HttpClientBuilder
-dontwarn org.apache.http.impl.conn.PoolingHttpClientConnectionManager
-dontwarn org.apache.http.impl.conn.SystemDefaultRoutePlanner

# Gson (if used via reflection)
-keep class com.google.gson.** { *; }
