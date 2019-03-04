-verbose
-dontobfuscate
-ignorewarnings

# These lines allow optimisation whilst preserving stack traces
-optimizations !code/allocation/variable
-optimizations !class/unboxing/enum
-keepattributes SourceFile, LineNumberTable
-keep,allowshrinking,allowoptimization class * { <methods>; }

# Support V7
-dontwarn android.support.v7.**
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }

 #Strip all logging for security and performance
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# Google Play Services
-keep public class com.google.android.gms.* { public *; }
-dontwarn com.google.android.gms.**

# Don't mess with classes with native methods
-keepclasseswithmembers class * {
    native <methods>;
}

-keepclasseswithmembernames class * {
    native <methods>;
}

-keep class android.support.v7.widget.SearchView { *; }

# Enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep all serializable objects
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# To prevent cases of reflection causing issues
-keepattributes InnerClasses
# Keep custom components in XML
-keep public class custom.components.**

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# To maintain custom components names that are used on layouts XML:
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

# Specific to Blockchain
-keep class android.support.design.widget.NavigationView { *; }

# Retrolambda
-dontwarn java.lang.invoke.*

-dontnote com.subgraph.orchid.crypto.PRNGFixes

# zxing
-dontwarn com.google.zxing.common.BitMatrix

# Guava
-dontwarn sun.misc.Unsafe
-dontnote com.google.common.reflect.**
-dontnote com.google.common.util.concurrent.MoreExecutors
-dontnote com.google.common.cache.Striped64,com.google.common.cache.Striped64$Cell


-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontnote com.squareup.okhttp.internal.Platform

-dontwarn com.octo.android.robospice.retrofit.RetrofitJackson**
-dontwarn retrofit.appengine.UrlFetchClient
-keepclasseswithmembers class * {
    @retrofit.http.* <methods>;
}
-keep class com.google.gson.** { *; }
-keep class com.google.inject.** { *; }
-keep class org.apache.http.** { *; }
-keep class org.apache.james.mime4j.** { *; }
-keep class javax.inject.** { *; }
-dontwarn org.apache.http.**
-dontwarn android.net.http.AndroidHttpClient

-dontwarn sun.misc.**


# ALSO REMEMBER KEEPING YOUR MODEL CLASSES
-keep class com.wavesplatform.wallet.v1.payload.** { *; }
-keep class com.wavesplatform.wallet.v1.api.** { *; }

# slf4j
-dontwarn org.slf4j.**

# Apache Commons
-dontwarn org.apache.**

-keep class com.firebase.** { *; }
-keep class org.apache.** { *; }
-keepnames class com.fasterxml.jackson.** { *; }
-keepnames class javax.servlet.** { *; }
-keepnames class org.ietf.jgss.** { *; }
-dontwarn org.w3c.dom.**
-dontwarn org.joda.time.**
-dontwarn org.shaded.apache.**
-dontwarn org.ietf.jgss.**

# Only necessary if you downloaded the SDK jar directly instead of from maven.
-keep class com.shaded.fasterxml.jackson.** { *; }

-keepattributes InnerClasses

-keep class com.wavesplatform.wallet.v1.data.services.** {*;}
-keepclassmembers class com.wavesplatform.wallet.v1.data.services.** {*;}
-keep class com.wavesplatform.wallet.v1.data.access.** {*;}
-keepclassmembers class com.wavesplatform.wallet.v1.data.access.** {*;}


# fingerprintidentify
-ignorewarnings
# fingerprintidentify MeiZuFingerprint
-dontwarn com.fingerprints.service.**
-keep class com.fingerprints.service.** { *; }
# fingerprintidentify SmsungFingerprint
-dontwarn com.samsung.android.sdk.**
-keep class com.samsung.android.sdk.** { *; }

-keep class com.chad.library.adapter.** {
*;
}
-keep public class * extends com.chad.library.adapter.base.BaseQuickAdapter
-keep public class * extends com.chad.library.adapter.base.BaseViewHolder
-keepclassmembers  class **$** extends com.chad.library.adapter.base.BaseViewHolder {
     <init>(...);
}


## Android architecture components: Lifecycle
# LifecycleObserver's empty constructor is considered to be unused by proguard
-keepclassmembers class * implements android.arch.lifecycle.LifecycleObserver {
    <init>(...);
}
# ViewModel's empty constructor is considered to be unused by proguard
-keepclassmembers class * extends android.arch.lifecycle.ViewModel {
    <init>(...);
}
# keep Lifecycle State and Event enums values
-keepclassmembers class android.arch.lifecycle.Lifecycle$State { *; }
-keepclassmembers class android.arch.lifecycle.Lifecycle$Event { *; }
# keep methods annotated with @OnLifecycleEvent even if they seem to be unused
# (Mostly for LiveData.LifecycleBoundObserver.onStateChange(), but who knows)
-keepclassmembers class * {
    @android.arch.lifecycle.OnLifecycleEvent *;
}

-keep class android.support.v7.widget.SearchView { *; }

-keepclassmembers class * implements android.arch.lifecycle.LifecycleObserver {
    <init>(...);
}

-keep class * implements android.arch.lifecycle.LifecycleObserver {
    <init>(...);
}
-keepclassmembers class android.arch.** { *; }
-keep class android.arch.** { *; }
-dontwarn android.arch.**


-keepattributes *Annotation*,InnerClasses

-dontwarn ren.yale.android.retrofitcachelib.**
-keep class ren.yale.android.retrofitcachelib.** { *; }

# Retrofit
-dontwarn retrofit2.**
-dontwarn org.codehaus.mojo.**
-keep class retrofit2.** { *; }
-keepattributes Exceptions

-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleParameterAnnotations


-keepclasseswithmembers class * {
    @retrofit2.* <methods>;
}

-keepclasseswithmembers interface * {
    @retrofit2.* <methods>;
}

-dontwarn org.robovm.**
-keep class org.robovm.** { *; }

#okhttp3
-dontwarn com.squareup.okhttp3.**
-keep class com.squareup.okhttp3.** { *;}
-keep class okhttp3.** { *;}
-keep class okio.** { *;}
-dontwarn sun.security.**
-keep class sun.security.** { *;}
-dontwarn okhttp3.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-dontwarn org.codehaus.mojo.animal_sniffer.*
-dontwarn okhttp3.internal.platform.ConscryptPlatform

# Rxjava rules
-dontwarn rx.internal.util.**

-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    long producerNode;
    long consumerNode;
}

#rxjava
-dontwarn rx.**
-keep class rx.** { *; }

-dontwarn sun.misc.**

-dontwarn ren.yale.android.retrofitcachelibrx2.**
-keep class ren.yale.android.retrofitcachelibrx2.** { *; }
-keepclasseswithmembernames class  retrofit2.adapter.rxjava2.BodyObservable { *; }
-keepclasseswithmembernames class  retrofit2.adapter.rxjava2.ResultObservable { *; }
-keepclasseswithmembernames class  retrofit2.adapter.rxjava2.CallEnqueueObservable { *; }
-keepclasseswithmembernames class  retrofit2.adapter.rxjava2.CallExecuteObservable { *; }
-keepclasseswithmembernames class retrofit2.Retrofit { *; }
-keepclasseswithmembernames class retrofit2.ServiceMethod { *; }
-keepclasseswithmembernames class retrofit2.OkHttpCall { *; }

-dontwarn com.google.errorprone.annotations.*
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
   long producerIndex;
   long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}
-dontnote rx.internal.util.PlatformDependent

# Retrofit rules
-dontwarn retrofit.**
-keep class retrofit.** { *; }
-keepattributes Exceptions
-dontwarn retrofit2.Platform$Java8

# OkHttp rules
-dontwarn okio.**
-dontwarn com.squareup.okhttp.**

# RxJava rules
# RxAndroid will soon ship with rules so this may not be needed in the future
# https://github.com/ReactiveX/RxAndroid/issues/219
-dontwarn sun.misc.Unsafe
-keep class rx.internal.util.unsafe.** { *; }

# Gson rules
-keep class sun.misc.Unsafe { *; }
-keepattributes RetrofitException
# Keep non static or private fields of models so Gson can find their names
-keepclassmembers class com.filmsinfo.android.data.model.** {
    !static !private <fields>;
}

-keep class com.wavesplatform.wallet.v2.data.remote.GithubService { *; }
-keep class com.wavesplatform.wallet.v2.data.remote.NodeService { *; }
-keep class com.wavesplatform.wallet.v2.data.remote.ApiService { *; }
-keep class com.wavesplatform.wallet.v2.data.remote.MatcherService { *; }
-keep class com.wavesplatform.wallet.v2.data.remote.CoinomatService { *; }
-keep class com.wavesplatform.wallet.v2.data.remote.DataFeedService { *; }

-keep class com.wavesplatform.wallet.v2.data.model.** { *; }
-keepclassmembernames interface * {
    @retrofit.http.* <methods>;
}

# Produces useful obfuscated stack traces
# http://proguard.sourceforge.net/manual/examples.html#stacktrace
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable
-dontwarn sun.misc.**


-dontnote rx.internal.util.PlatformDependent


-keepnames public class * extends io.realm.RealmObject
-keepnames public class * extends io.realm.RealmModel
-keep class io.realm.annotations.RealmModule
-keep class io.realm.annotations.RealmClass
-keep @io.realm.annotations.RealmModule class *
-keep @io.realm.annotations.RealmClass class *
-keep class io.realm.internal.Keep
-keep @io.realm.internal.Keep class *
-dontwarn io.realm.**

# com.github.vicpinm:krealmextensions
-keep class com.vicpin.krealmextensions.**


# Gson specific classes
-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }

-keepattributes *Annotation*,EnclosingMethod,Signature
-keepnames class com.fasterxml.jackson.** {
*;
}
-keepnames interface com.fasterxml.jackson.** {
    *;
}
-dontwarn com.fasterxml.jackson.databind.**
-keep class org.codehaus.** { *; }


-keep class com.google.common.io.Resources {
    public static <methods>;
}
-keep class com.google.common.collect.Lists {
    public static ** reverse(**);
}
-keep class com.google.common.base.Charsets {
    public static <fields>;
}

-keep class com.google.common.base.Joiner {
    public static com.google.common.base.Joiner on(java.lang.String);
    public ** join(...);
}

-keep class com.google.common.collect.MapMakerInternalMap$ReferenceEntry
-keep class com.google.common.cache.LocalCache$ReferenceEntry

# http://stackoverflow.com/questions/9120338/proguard-configuration-for-guava-with-obfuscation-and-optimization
-dontwarn javax.annotation.**
-dontwarn javax.inject.**
-dontwarn sun.misc.Unsafe

# Guava
-dontwarn java.lang.ClassValue
-dontwarn com.google.j2objc.annotations.Weak
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontoptimize
-dontobfuscate
-dontwarn sun.misc.Unsafe
-dontwarn com.google.common.collect.MinMaxPriorityQueue

-keepclasseswithmembers public class * {
    public static void main(java.lang.String[]);
}

# AppsFlyer
-dontwarn com.android.installreferrer
-keep class com.appsflyer.** { *; }

-dontwarn com.samsung.android.sdk.**
-dontwarn afu.org.checkerframework.checker.**
-dontnote com.samsung.android.sdk.**

# Crashlytics
-keep public class * extends java.lang.Exception
-printmapping mapping.txt
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**

# ReactiveNetwork
-dontwarn com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
-dontwarn io.reactivex.functions.Function
-dontwarn rx.internal.util.**
-dontwarn sun.misc.Unsafe

# BaseRecyclerViewAdapterHelper
-keep class com.chad.library.adapter.** { *; }
-keep public class * extends com.chad.library.adapter.base.BaseQuickAdapter
-keep public class * extends com.chad.library.adapter.base.BaseViewHolder
-keepclassmembers  class **$** extends com.chad.library.adapter.base.BaseViewHolder { <init>(...); }

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}