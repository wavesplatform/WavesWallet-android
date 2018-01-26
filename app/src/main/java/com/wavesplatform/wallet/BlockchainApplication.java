package com.wavesplatform.wallet;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import com.wavesplatform.wallet.data.access.AccessState;
import com.wavesplatform.wallet.data.access.DexAccessState;
import com.wavesplatform.wallet.data.connectivity.ConnectivityManager;
import com.wavesplatform.wallet.data.services.PinStoreService;
import com.wavesplatform.wallet.db.DBHelper;
import com.wavesplatform.wallet.injection.Injector;
import com.wavesplatform.wallet.ui.auth.EnvironmentManager;
import com.wavesplatform.wallet.util.AppUtil;
import com.wavesplatform.wallet.util.ApplicationLifeCycle;
import com.wavesplatform.wallet.util.PrefsUtil;
import com.wavesplatform.wallet.util.annotations.Thunk;
import com.wavesplatform.wallet.util.exceptions.LoggingExceptionHandler;

import javax.inject.Inject;

import io.reactivex.plugins.RxJavaPlugins;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class BlockchainApplication extends Application {

    @Thunk static final String TAG = BlockchainApplication.class.getSimpleName();
    private static final String RX_ERROR_TAG = "RxJava Error";
    @Inject PrefsUtil mPrefsUtil;

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
        // Init objects first
        Injector.getInstance().init(this);
        // Inject into Application
        Injector.getInstance().getAppComponent().inject(this);

        Realm.init(this);

        new LoggingExceptionHandler();

        RealmConfiguration config = new RealmConfiguration.Builder()
                .name(String.format("%s.realm", mPrefsUtil.getValue(PrefsUtil.KEY_PUB_KEY, "")))
                .deleteRealmIfMigrationNeeded()
                .build();
        DBHelper.getInstance().setRealmConfig(config);

        RxJavaPlugins.setErrorHandler(throwable -> Log.e(RX_ERROR_TAG, throwable.getMessage(), throwable));

        AppUtil appUtil = new AppUtil(this);

        AccessState.getInstance().initAccessState(this,
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

    /**
     * This is reached if the provider cannot be updated for some reason. App should consider all
     * HTTP communication to be vulnerable, and take appropriate action.
     */
    @Thunk
    void onProviderInstallerNotAvailable() {
        // TODO: 05/08/2016 Decide if we should take action here or not
        Log.wtf(TAG, "Security Provider Installer not available");
    }
}
