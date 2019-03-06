package com.wavesplatform.wallet;

import android.arch.lifecycle.ProcessLifecycleOwner;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatDelegate;

import com.akexorcist.localizationactivity.core.LocalizationApplicationDelegate;
import com.crashlytics.android.Crashlytics;
import com.github.moduth.blockcanary.BlockCanary;
import com.google.firebase.FirebaseApp;
import com.novoda.simplechromecustomtabs.SimpleChromeCustomTabs;
import com.squareup.leakcanary.LeakCanary;
import com.wavesplatform.wallet.v1.data.connectivity.ConnectivityManager;
import com.wavesplatform.wallet.v1.ui.auth.EnvironmentManager;
import com.wavesplatform.wallet.v1.util.AppUtil;
import com.wavesplatform.wallet.v1.util.ApplicationLifeCycle;
import com.wavesplatform.wallet.v1.util.PrefsUtil;
import com.wavesplatform.wallet.v2.data.helpers.AuthHelper;
import com.wavesplatform.wallet.v2.data.manager.AccessManager;
import com.wavesplatform.wallet.v2.data.manager.GithubDataManager;
import com.wavesplatform.wallet.v2.data.receiver.ScreenReceiver;
import com.wavesplatform.wallet.v2.injection.component.DaggerApplicationV2Component;
import com.wavesplatform.wallet.v2.util.Analytics;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DaggerApplication;
import io.fabric.sdk.android.Fabric;
import io.reactivex.plugins.RxJavaPlugins;
import io.realm.Realm;
import io.sentry.Sentry;
import io.sentry.android.AndroidSentryClientFactory;
import pers.victor.ext.Ext;
import timber.log.Timber;

public class App extends DaggerApplication {

    @Inject
    PrefsUtil mPrefsUtil;
    @Inject
    AuthHelper authHelper;
    @Inject
    GithubDataManager githubDataManager;
    private static App application;
    private static AccessManager accessManager;
    private LocalizationApplicationDelegate localizationDelegate
            = new LocalizationApplicationDelegate(this);

    @Override
    public void onCreate() {
        EnvironmentManager.init(this);
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);

        Analytics.appsFlyerInit(this);
        FirebaseApp.initializeApp(this);
        Fabric.with(this, new Crashlytics());
        application = this;
        BlockCanary.install(this, new AppBlockCanaryContext()).start();

        Realm.init(this);
        Ext.INSTANCE.setCtx(this);

        Sentry.init(new AndroidSentryClientFactory(this.getApplicationContext()));

        RxJavaPlugins.setErrorHandler(Timber::e);

        AppUtil appUtil = new AppUtil(this);
        accessManager = new AccessManager(mPrefsUtil, appUtil, authHelper);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        // sessions handlers
        ProcessLifecycleOwner.get().getLifecycle().addObserver(new AppLifecycleObserver());
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        BroadcastReceiver mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);

        ConnectivityManager.getInstance().registerNetworkListener(this);


        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        SimpleChromeCustomTabs.initialize(this);

        EnvironmentManager.updateConfiguration(githubDataManager);
    }

    public static Context getAppContext() {
        return application;
    }

    public static AccessManager getAccessManager() {
        return accessManager;
    }

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        return DaggerApplicationV2Component.builder().create(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(localizationDelegate.attachBaseContext(base));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        localizationDelegate.onConfigurationChanged(this);
    }

    @Override
    public Context getApplicationContext() {
        return localizationDelegate.getApplicationContext(super.getApplicationContext());
    }
}
