/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet

import android.arch.lifecycle.ProcessLifecycleOwner
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.support.v7.app.AppCompatDelegate
import com.akexorcist.localizationactivity.core.LocalizationApplicationDelegate
import com.crashlytics.android.Crashlytics
import com.github.moduth.blockcanary.BlockCanary
import com.google.firebase.FirebaseApp
import com.novoda.simplechromecustomtabs.SimpleChromeCustomTabs
import com.squareup.leakcanary.LeakCanary
import com.wavesplatform.sdk.WavesSdk
import com.wavesplatform.sdk.utils.Environment
import com.wavesplatform.wallet.v2.data.analytics.Analytics
import com.wavesplatform.wallet.v2.data.helpers.AuthHelper
import com.wavesplatform.wallet.v2.data.manager.AccessManager
import com.wavesplatform.wallet.v2.data.receiver.ScreenReceiver
import com.wavesplatform.wallet.v2.injection.component.DaggerApplicationV2Component
import com.wavesplatform.wallet.v2.util.EnvironmentManager
import com.wavesplatform.wallet.v2.util.PrefsUtil
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import io.fabric.sdk.android.Fabric
import io.reactivex.plugins.RxJavaPlugins
import io.realm.Realm
import io.sentry.Sentry
import io.sentry.android.AndroidSentryClientFactory
import pers.victor.ext.Ext
import timber.log.Timber
import javax.inject.Inject

class App : DaggerApplication() {

    @Inject
    lateinit var mPrefsUtil: PrefsUtil
    @Inject
    lateinit var authHelper: AuthHelper
    private val localizationDelegate by lazy { LocalizationApplicationDelegate(this) }

    override fun onCreate() {
        super.onCreate()

        if (initDebugTools()) return

        initExtension()
        initLocalProperties()
        intiAnalytics()
        initRealm()
        initFirebaseServices()
        initWavesSdk()
        initVectorSupport()
        initChromeTabs()
        registerSessionHandler()
    }

    private fun initVectorSupport() {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    private fun initChromeTabs() {
        SimpleChromeCustomTabs.initialize(this)
    }

    private fun initExtension() {
        Ext.with(this)
    }

    private fun initWavesSdk() {
        WavesSdk.init(this, Environment.DEFAULT)
        EnvironmentManager.update()
    }

    private fun initLocalProperties() {
        appContext = this
        accessManager = AccessManager(mPrefsUtil, authHelper)
    }

    private fun initFirebaseServices() {
        FirebaseApp.initializeApp(this)
        Fabric.with(this, Crashlytics())
    }

    private fun initRealm() {
        Realm.init(this)
    }

    private fun registerSessionHandler() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver())
        val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        val mReceiver = ScreenReceiver()
        registerReceiver(mReceiver, filter)
    }

    private fun intiAnalytics() {
        Analytics.init(this)
        Sentry.init(AndroidSentryClientFactory(this.applicationContext))
    }

    private fun initDebugTools(): Boolean {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return true
        }

        LeakCanary.install(this)
        BlockCanary.install(this, AppBlockCanaryContext()).start()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        RxJavaPlugins.setErrorHandler { Timber.e(it) }

        return false
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerApplicationV2Component.builder().create(this)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(localizationDelegate.attachBaseContext(base))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        localizationDelegate.onConfigurationChanged(this)
    }

    override fun getApplicationContext(): Context {
        return localizationDelegate.getApplicationContext(super.getApplicationContext())
    }

    companion object {
        lateinit var appContext: App
            private set
        lateinit var accessManager: AccessManager
            private set
    }
}
