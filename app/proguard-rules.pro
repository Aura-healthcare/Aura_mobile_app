# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/lecoucl/Downloads/android-sdk-linux_x86-1.6_r1/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

-keep interface android.support.v4.app.** { *; }
-keep class android.support.v4.app.** { *; }

-dontwarn com.google.common.**
-keepclassmembers class com.google.common.** {
   public *;
}
-dontwarn com.facebook.**
-keepclassmembers class com.facebook.** {
   public *;
}
-dontwarn android.test.**
-keepclassmembers class android.test.** {
   public *;
}
-dontwarn org.junit.**
-keepclassmembers class org.junit.** {
   public *;
}
-dontwarn okio.**
-keepclassmembers class okio.** {
   public *;
}
-dontwarn org.codehaus.mojo.**
-keepclassmembers class org.codehaus.mojo.** {
   public *;
}
-keep class com.wearablesensor.aura.user_session.UserModel  { *; }
-keep class com.wearablesensor.aura.user_session.UserPreferencesModel  { *; }
-keep class com.facebook.**  { *; }

# GreenRobot
-keepattributes *Annotation*
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

# AWS
-keep class org.apache.commons.logging.**               { *; }
-keep class com.amazonaws.org.apache.commons.logging.** { *; }
-keep class com.amazonaws.services.sqs.QueueUrlHandler  { *; }
-keep class com.amazonaws.services.**  { *; }
-keep class com.amazonaws.mobileconnectors.**  { *; }
-keep class com.amazonaws.javax.xml.transform.sax.*     { public *; }
-keep class com.amazonaws.javax.xml.stream.**           { *; }
-keep class com.amazonaws.services.**.model.*Exception* { *; }
-keep class com.amazonaws.internal.**                   { *; }
-keep class org.codehaus.**                             { *; }
-keep class org.joda.time.tz.Provider                   { *; }
-keep class org.joda.time.tz.NameProvider               { *; }
-keepattributes Signature,*Annotation*,EnclosingMethod
-keepnames class com.fasterxml.jackson.** { *; }
-keepnames class com.amazonaws.** { *; }
-keepclassmembers class com.amazon.util.** {
   public *;
}

-dontwarn com.amazonaws.util.**
-dontwarn com.fasterxml.jackson.databind.**
-dontwarn javax.xml.stream.events.**
-dontwarn org.codehaus.jackson.**
-dontwarn com.amazonaws.mobileconnectors.**
-dontwarn org.apache.commons.logging.impl.**
-dontwarn org.apache.http.conn.scheme.**
-dontwarn org.apache.http.annotation.**
-dontwarn org.ietf.jgss.**
-dontwarn org.joda.convert.**
-dontwarn com.amazonaws.org.joda.convert.**
-dontwarn org.w3c.dom.bootstrap.**

#SDK split into multiple jars so certain classes may be referenced but not used
-dontwarn com.amazonaws.services.s3.**
-dontwarn com.amazonaws.services.sqs.**

-dontnote com.amazonaws.services.sqs.QueueUrlHandler

# Log
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
    public static *** wtf(...);
    public static *** println(...);
}

