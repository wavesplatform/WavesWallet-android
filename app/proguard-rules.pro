-verbose
-dontobfuscate
-ignorewarnings

# Glide ------------------------------------------------------#
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
# End Glide ---------------------------------------------------

# Crashlytics ------------------------------------------------#
-keep public class * extends java.lang.Exception
-printmapping mapping.txt
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**
# End Crashlytics ---------------------------------------------

# Gson -------------------------------------------------------#
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
# -keep class com.google.gson.examples.android.model.** { *; }

# Prevent proguard from stripping interface information from TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
# End Gson ----------------------------------------------------

# Guava ------------------------------------------------------#
-dontwarn javax.lang.model.element.Modifier

# Note: We intentionally don't add the flags we'd need to make Enums work.
# That's because the Proguard configuration required to make it work on
# optimized code would preclude lots of optimization, like converting enums
# into ints.

# Throwables uses internal APIs for lazy stack trace resolution
-dontnote sun.misc.SharedSecrets
-keep class sun.misc.SharedSecrets {
  *** getJavaLangAccess(...);
}
-dontnote sun.misc.JavaLangAccess
-keep class sun.misc.JavaLangAccess {
  *** getStackTraceElement(...);
  *** getStackTraceDepth(...);
}

# FinalizableReferenceQueue calls this reflectively
# Proguard is intelligent enough to spot the use of reflection onto this, so we
# only need to keep the names, and allow it to be stripped out if
# FinalizableReferenceQueue is unused.
-keepnames class com.google.common.base.internal.Finalizer {
  *** startFinalizer(...);
}
# However, it cannot "spot" that this method needs to be kept IF the class is.
-keepclassmembers class com.google.common.base.internal.Finalizer {
  *** startFinalizer(...);
}
-keepnames class com.google.common.base.FinalizableReference {
  void finalizeReferent();
}
-keepclassmembers class com.google.common.base.FinalizableReference {
  void finalizeReferent();
}

# Striped64, LittleEndianByteArray, UnsignedBytes, AbstractFuture
-dontwarn sun.misc.Unsafe

# Striped64 appears to make some assumptions about object layout that
# really might not be safe. This should be investigated.
-keepclassmembers class com.google.common.cache.Striped64 {
  *** base;
  *** busy;
}
-keepclassmembers class com.google.common.cache.Striped64$Cell {
  <fields>;
}

-dontwarn java.lang.SafeVarargs

-keep class java.lang.Throwable {
  *** addSuppressed(...);
}

# Futures.getChecked, in both of its variants, is incompatible with proguard.

# Used by AtomicReferenceFieldUpdater and sun.misc.Unsafe
-keepclassmembers class com.google.common.util.concurrent.AbstractFuture** {
  *** waiters;
  *** value;
  *** listeners;
  *** thread;
  *** next;
}
-keepclassmembers class com.google.common.util.concurrent.AtomicDouble {
  *** value;
}
-keepclassmembers class com.google.common.util.concurrent.AggregateFutureState {
  *** remaining;
  *** seenExceptions;
}

# Since Unsafe is using the field offsets of these inner classes, we don't want
# to have class merging or similar tricks applied to these classes and their
# fields. It's safe to allow obfuscation, since the by-name references are
# already preserved in the -keep statement above.
-keep,allowshrinking,allowobfuscation class com.google.common.util.concurrent.AbstractFuture** {
  <fields>;
}

# Futures.getChecked (which often won't work with Proguard anyway) uses this. It
# has a fallback, but again, don't use Futures.getChecked on Android regardless.
-dontwarn java.lang.ClassValue

# MoreExecutors references AppEngine
-dontnote com.google.appengine.api.ThreadManager
-keep class com.google.appengine.api.ThreadManager {
  static *** currentRequestThreadFactory(...);
}
-dontnote com.google.apphosting.api.ApiProxy
-keep class com.google.apphosting.api.ApiProxy {
  static *** getCurrentEnvironment (...);
}
# End Guava ---------------------------------------------------

# AppsFlyer --------------------------------------------------#
-dontwarn com.android.installreferrer
-keep class com.appsflyer.** { *; }
# End AppsFlyer -----------------------------------------------

# com.github.vicpinm:krealmextensions ------------------------#
-keep class com.vicpin.krealmextensions.**
# com.github.vicpinm:krealmextensions -------------------------

# RxJava 2 & RxAndroid ---------------------------------------#
# Nothing requires ProGuard
# End RxJava 2 & RxAndroid ------------------------------------

# Retrofit ---------------------------------------------------#
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
# End Retrofit ------------------------------------------------

# LeakCanary -------------------------------------------------#
-keep class org.eclipse.mat.** { *; }
-keep class com.squareup.leakcanary.** { *; }
# End LeakCanary ----------------------------------------------

# Reactive Network -------------------------------------------#
-dontwarn com.github.pwittchen.reactivenetwork.library.ReactiveNetwork
-dontwarn io.reactivex.functions.Function
-dontwarn rx.internal.util.**
-dontwarn sun.misc.Unsafe
# End Reactive Network ----------------------------------------

# Fingerprintidentify ----------------------------------------#
-ignorewarnings
# fingerprintidentify MeiZuFingerprint
-dontwarn com.fingerprints.service.**
-keep class com.fingerprints.service.** { *; }
# fingerprintidentify SmsungFingerprint
-dontwarn com.samsung.android.sdk.**
-keep class com.samsung.android.sdk.** { *; }
# End Fingerprintidentify -------------------------------------

# Zxing ------------------------------------------------------#
-dontwarn com.google.zxing.common.BitMatrix
# End xing ----------------------------------------------------

# Common recommendations -------------------------------------#
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
-keepattributes *Annotation*, InnerClasses

# Produces useful obfuscated stack traces
# http://proguard.sourceforge.net/manual/examples.html#stacktrace
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable
-dontwarn sun.misc.**
# End Common recommendations -----------------------------------

# BaseRecyclerViewAdapterHelper -------------------------------#
-keep class com.chad.library.adapter.** {
*;
}
-keep public class * extends com.chad.library.adapter.base.BaseQuickAdapter
-keep public class * extends com.chad.library.adapter.base.BaseViewHolder
-keepclassmembers  class **$** extends com.chad.library.adapter.base.BaseViewHolder {
     <init>(...);
}
# End BaseRecyclerViewAdapterHelper ---------------------------

# Realm ------------------------------------------------------#
-keepnames public class * extends io.realm.RealmObject
-keepnames public class * extends io.realm.RealmModel
-keep class io.realm.annotations.RealmModule
-keep class io.realm.annotations.RealmClass
-keep @io.realm.annotations.RealmModule class *
-keep @io.realm.annotations.RealmClass class *
-keep class io.realm.internal.Keep
-keep @io.realm.internal.Keep class *
-dontwarn io.realm.**
# End Realm ---------------------------------------------------

# Apache commons.io ------------------------------------------#
-dontwarn java.beans.*
-dontnote org.apache.commons.compress.**
-dontwarn org.apache.commons.compress.**
-keep class org.apache.commons.lang3.StringUtils { *; }
-keep class org.apache.commons.io.IOUtils { *; }
-keep class org.apache.commons.io.FileUtils { *; }
-dontnote org.apache.commons.io.IOUtils
-dontnote org.apache.commons.lang3.StringUtils
-dontnote org.apache.commons.io.FileUtils
-dontwarn java.nio.channels.SeekableByteChannel
-dontwarn java.nio.file.attribute.FileAttribute
-dontwarn java.io.File
# End Apache commons.io ---------------------------------------

# Android architecture components: Lifecycle -----------------#
# todo не уверен что это нужно (legacy)
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
# End Android architecture components: Lifecycle --------------

# Keep custom components in XML ------------------------------#
# todo не уверен что это нужно (legacy)
-keep public class custom.components.**
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
# End Keep custom components in XML ---------------------------

# Strip all logging for security and performance -------------#
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}
# End Strip all logging for security and performance ----------

# Support ----------------------------------------------------#
-dontwarn android.support.v7.**
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }
-keep class android.support.v7.widget.SearchView { *; }
# Specific to Blockchain
-keep class android.support.design.widget.NavigationView { *; }
# End Support -------------------------------------------------

# Allow optimisation whilst preserving stack traces ----------#
-optimizations !code/allocation/variable
-optimizations !class/unboxing/enum
-keepattributes SourceFile, LineNumberTable
-keep,allowshrinking,allowoptimization class * { <methods>; }
# End Allow optimisation whilst preserving stack traces -------