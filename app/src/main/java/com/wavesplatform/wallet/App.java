package com.wavesplatform.wallet;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import com.franmontiel.localechanger.LocaleChanger;
import com.github.moduth.blockcanary.BlockCanary;
import com.novoda.simplechromecustomtabs.SimpleChromeCustomTabs;
import com.vicpin.krealmextensions.RealmConfigStore;
import com.wavesplatform.wallet.v1.data.access.AccessState;
import com.wavesplatform.wallet.v1.data.access.DexAccessState;
import com.wavesplatform.wallet.v1.data.connectivity.ConnectivityManager;
import com.wavesplatform.wallet.v1.data.services.PinStoreService;
import com.wavesplatform.wallet.v1.db.DBHelper;
import com.wavesplatform.wallet.v1.injection.Injector;
import com.wavesplatform.wallet.v1.ui.auth.EnvironmentManager;
import com.wavesplatform.wallet.v1.util.AppUtil;
import com.wavesplatform.wallet.v1.util.ApplicationLifeCycle;
import com.wavesplatform.wallet.v1.util.PrefsUtil;
import com.wavesplatform.wallet.v1.util.annotations.Thunk;
import com.wavesplatform.wallet.v1.util.exceptions.LoggingExceptionHandler;
import com.wavesplatform.wallet.v2.data.helpers.AuthHelper;
import com.wavesplatform.wallet.v2.data.manager.AccessManager;
import com.wavesplatform.wallet.v2.data.model.remote.response.Alias;
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance;
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetPair;
import com.wavesplatform.wallet.v2.data.model.remote.response.IssueTransaction;
import com.wavesplatform.wallet.v2.data.model.remote.response.Lease;
import com.wavesplatform.wallet.v2.data.model.remote.response.Order;
import com.wavesplatform.wallet.v2.data.model.remote.response.SpamAsset;
import com.wavesplatform.wallet.v2.data.model.remote.response.Transaction;
import com.wavesplatform.wallet.v2.data.model.remote.response.Transfer;
import com.wavesplatform.wallet.v2.injection.component.DaggerApplicationV2Component;
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookUser;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DaggerApplication;
import io.github.kbiakov.codeview.classifier.CodeProcessor;
import io.reactivex.plugins.RxJavaPlugins;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import pers.victor.ext.Ext;

public class App extends DaggerApplication {

    @Thunk
    static final String TAG = App.class.getSimpleName();
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
        // Init objects first
        Injector.getInstance().init(this);
        CodeProcessor.init(this);
        // Inject into Application
//        Injector.getInstance().getAppComponent().inject(this);

        Realm.init(this);
        Ext.INSTANCE.setCtx(this);

        new LoggingExceptionHandler();

        RxJavaPlugins.setErrorHandler(throwable -> Log.e(RX_ERROR_TAG, throwable.getMessage(), throwable));

        AppUtil appUtil = new AppUtil(this);
        accessManager = new AccessManager(mPrefsUtil, appUtil, authHelper);

        AccessState.getInstance().initAccessState(
                new PrefsUtil(this),
                new PinStoreService(),
                appUtil);

        DexAccessState.getInstance().initAccessState(this,
                new PrefsUtil(this),
                new PinStoreService(),
                appUtil);

        EnvironmentManager.init(new PrefsUtil(this), appUtil);

        // Apply PRNG fixes on app start if needed
        appUtil.applyPRNGFixes();

        ConnectivityManager.getInstance().registerNetworkListener(this);


        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        SimpleChromeCustomTabs.initialize(this);

        ApplicationLifeCycle.getInstance().addListener(new ApplicationLifeCycle.LifeCycleListener() {
            @Override
            public void onBecameForeground() {
                // Ensure that PRNG fixes are always current for the session
                appUtil.applyPRNGFixes();
            }

            @Override
            public void onBecameBackground() {
                // No-op
                AccessState.getInstance().removeWavesWallet();
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

    /**
     * This is reached if the provider cannot be updated for some reason. App should consider all
     * HTTP communication to be vulnerable, and take appropriate action.
     */
    @Thunk
    void onProviderInstallerNotAvailable() {
        // TODO: 05/08/2016 Decide if we should take action here or not
        Log.wtf(TAG, "Security Provider Installer not available");
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LocaleChanger.onConfigurationChanged();
    }
}
