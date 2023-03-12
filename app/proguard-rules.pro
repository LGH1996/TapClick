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
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class com.lgh.advertising.going.mybean.LatestMessage {*;}
-keep class com.lgh.advertising.going.mybean.LatestMessage$Asset {*;}
-keep class com.lgh.advertising.going.mybean.LatestMessage$Author {*;}
-keep class com.lgh.advertising.going.mybean.LatestMessage$Uploader {*;}
-keep class com.lgh.advertising.going.mybean.AppDescribe {*;}
-keep class com.lgh.advertising.going.mybean.AutoFinder {*;}
-keep class com.lgh.advertising.going.mybean.Widget {*;}
-keep class com.lgh.advertising.going.mybean.Coordinate {*;}
-keep class com.lgh.advertising.going.mybean.BasicContent {*;}
-keep class com.lgh.advertising.going.mybean.CoordinateShare {*;}
-keep class com.lgh.advertising.going.mybean.WidgetShare {*;}
