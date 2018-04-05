package com.wavesplatform.wallet;

import android.content.Context;
import android.os.Build;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

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
import com.wavesplatform.wallet.v2.injection.component.DaggerApplicationV2Component;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DaggerApplication;
import io.reactivex.plugins.RxJavaPlugins;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import pers.victor.ext.Ext;

public class BlockchainApplication extends DaggerApplication {

    @Thunk
    static final String TAG = BlockchainApplication.class.getSimpleName();
    private static final String RX_ERROR_TAG = "RxJava Error";
    @Inject
    PrefsUtil mPrefsUtil;

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
//        Injector.getInstance().getAppComponent().inject(this);

        Realm.init(this);
        Ext.INSTANCE.setCtx(this);

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
}
