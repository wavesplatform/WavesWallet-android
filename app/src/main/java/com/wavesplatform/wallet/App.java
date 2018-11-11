package com.wavesplatform.wallet;

import android.arch.lifecycle.ProcessLifecycleOwner;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Build;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.franmontiel.localechanger.LocaleChanger;
import com.github.moduth.blockcanary.BlockCanary;
import com.novoda.simplechromecustomtabs.SimpleChromeCustomTabs;
import com.wavesplatform.wallet.v1.data.connectivity.ConnectivityManager;
import com.wavesplatform.wallet.v1.ui.auth.EnvironmentManager;
import com.wavesplatform.wallet.v1.util.AppUtil;
import com.wavesplatform.wallet.v1.util.ApplicationLifeCycle;
import com.wavesplatform.wallet.v1.util.PrefsUtil;
import com.wavesplatform.wallet.v2.data.helpers.AuthHelper;
import com.wavesplatform.wallet.v2.data.manager.AccessManager;
import com.wavesplatform.wallet.v2.data.receiver.ScreenReceiver;
import com.wavesplatform.wallet.v2.injection.component.DaggerApplicationV2Component;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DaggerApplication;
import io.fabric.sdk.android.Fabric;
import io.github.kbiakov.codeview.classifier.CodeProcessor;
import io.reactivex.plugins.RxJavaPlugins;
import io.realm.Realm;
import pers.victor.ext.Ext;

public class App extends DaggerApplication {

    private static final String RX_ERROR_TAG = "RxJava Error";
    @Inject
    PrefsUtil mPrefsUtil;
    @Inject
    AuthHelper authHelper;
    private static Context sContext;
    private static AccessManager accessManager;


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            MultiDex.install(base);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        sContext = this;
        List<Locale> SUPPORTED_LOCALES =
                Arrays.asList(
                        new Locale(getString(R.string.choose_language_russia_code).toLowerCase()),
                        new Locale(getString(R.string.choose_language_china_code).toLowerCase()),
                        new Locale(getString(R.string.choose_language_korea_code).toLowerCase()),
                        new Locale(getString(R.string.choose_language_turkey_code).toLowerCase()),
                        new Locale(getString(R.string.choose_language_english_code).toLowerCase()),
                        new Locale(getString(R.string.choose_language_hindi_code).toLowerCase()),
                        new Locale(getString(R.string.choose_language_dansk_code).toLowerCase()),
                        new Locale(getString(R.string.choose_language_nederlands_code).toLowerCase())
                );

        BlockCanary.install(this, new AppBlockCanaryContext()).start();
        LocaleChanger.initialize(getApplicationContext(), SUPPORTED_LOCALES);

        CodeProcessor.init(this);

        Realm.init(this);
        Ext.INSTANCE.setCtx(this);

        RxJavaPlugins.setErrorHandler(throwable -> Log.e(RX_ERROR_TAG, throwable.getMessage(), throwable));

        AppUtil appUtil = new AppUtil(this);
        accessManager = new AccessManager(mPrefsUtil, appUtil, authHelper);

        // sessions handlers
        ProcessLifecycleOwner.get().getLifecycle().addObserver(new AppLifecycleObserver());
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        BroadcastReceiver mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);

        EnvironmentManager.init(new PrefsUtil(this), appUtil);

        // Apply PRNG fixes on app start if needed
        appUtil.applyPRNGFixes();

        ConnectivityManager.getInstance().registerNetworkListener(this);


        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        SimpleChromeCustomTabs.initialize(this);

        // todo сомнительная штука
        ApplicationLifeCycle.getInstance().addListener(new ApplicationLifeCycle.LifeCycleListener() {
            @Override
            public void onBecameForeground() {
                // Ensure that PRNG fixes are always current for the session
                appUtil.applyPRNGFixes();
            }

            @Override
            public void onBecameBackground() {
                // No-op
            }
        });
    }

    public static Context getAppContext() {
        return sContext;
    }

    public static AccessManager getAccessManager() {
        return accessManager;
    }

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        return DaggerApplicationV2Component.builder().create(this);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LocaleChanger.onConfigurationChanged();
    }
}
