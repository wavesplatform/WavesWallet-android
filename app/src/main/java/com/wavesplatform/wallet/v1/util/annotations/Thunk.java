package com.wavesplatform.wallet.v1.util.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the given field or method has package visibility solely to prevent the creation of
 * a synthetic method. In practice, you should treat this field/method as if it were private.
 *
 * When a private method is called from an inner class, the Java compiler generates a simple package
 * private shim method that the class generated from the inner class can call. This results in
 * unnecessary bloat and runtime method call overhead. It also gets us closer to the dex method
 * count limit.
 *
 * If you'd like to see warnings for these synthetic methods in Android Studio: Preferences > Editor
 * > Inspections > J2ME Issues > "Private member access between outer and inner classes". This is
 * disabled by default.
 *
 * @see <a href="https://realm.io/news/360andev-jake-wharton-java-hidden-costs-android/">Realm.io
 * talk by Jake Wharton</a>
 * @see <a href="https://android.googlesource.com/platform/packages/apps/Launcher3/+/master/src/com/android/launcher3/util/Thunk.java">Launcher
 * 3 source by Google</a>
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.TYPE})
public @interface Thunk {
}