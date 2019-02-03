package com.wavesplatform.wallet.v2.injection.scope

import java.lang.annotation.Documented
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

import javax.inject.Scope

/**
 * A scoping annotation to permit objects whose lifetime should
 * conform to the life of the Activity to be memorised in the
 * correct component.
 */
@Scope
@Documented
@Retention(RetentionPolicy.RUNTIME)
annotation class PerActivity
