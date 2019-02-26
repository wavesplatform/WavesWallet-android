package com.wavesplatform.wallet.v1.ui.auth;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.wavesplatform.wallet.App;
import com.wavesplatform.wallet.v1.util.PrefsUtil;
import com.wavesplatform.wallet.v2.data.manager.MatcherDataManager;
import com.wavesplatform.wallet.v2.data.model.remote.response.GlobalConfiguration;
import com.wavesplatform.wallet.v2.injection.module.HostSelectionInterceptor;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class EnvironmentManager {

    public static final String KEY_ENV_TEST_NET = "env_testnet";
    private static final String URL_CONFIG_MAIN_NET = "https://github-proxy.wvservices.com/" +
            "wavesplatform/waves-client-config/master/environment_mainnet.json";

    public static final String KEY_ENV_MAIN_NET = "env_prod";
    private static final String URL_CONFIG_TEST_NET = "https://github-proxy.wvservices.com/" +
            "wavesplatform/waves-client-config/master/environment_testnet.json";

    public static final String URL_COMMISSION_MAIN_NET = "https://github-proxy.wvservices.com/" +
            "wavesplatform/waves-client-config/master/fee.json";

    private static EnvironmentManager instance;
    private static Handler handler = new Handler();

    private Environment current;
    private Application application;
    private Disposable disposable;
    private HostSelectionInterceptor interceptor;

    public static void init(Application application) {
        instance = new EnvironmentManager();
        instance.application = application;

        String envName = getEnvironmentName();
        if (!TextUtils.isEmpty(envName)) {
            for (Environment environment : Environment.environments) {
                if (envName.equalsIgnoreCase(environment.name)) {
                    SharedPreferences preferenceManager = PreferenceManager
                            .getDefaultSharedPreferences(instance.application);
                    String json = preferenceManager.getString(
                            PrefsUtil.GLOBAL_CURRENT_ENVIRONMENT_DATA,
                            EnvironmentConstants.MAIN_NET_JSON);
                    environment.setConfiguration(new Gson().fromJson(json, GlobalConfiguration.class));
                    instance.current =  environment;
                }
            }
        }
    }

    public static HostSelectionInterceptor createHostInterceptor() {
        instance.interceptor = new HostSelectionInterceptor(getEnvironment().configuration.getServers());
        return instance.interceptor;
    }

    public static void updateConfiguration(MatcherDataManager matcherDataManager) {
        instance.disposable = matcherDataManager.apiService.loadGlobalConfiguration(getEnvironment().url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(globalConfiguration -> {
                    instance.interceptor.setHosts(globalConfiguration.getServers());
                    SharedPreferences preferenceManager = PreferenceManager
                            .getDefaultSharedPreferences(App.getAppContext());
                    SharedPreferences.Editor editor = preferenceManager.edit();
                    editor.putString(PrefsUtil.GLOBAL_CURRENT_ENVIRONMENT_DATA,
                            new Gson().toJson(globalConfiguration))
                            .apply();
                    instance.current.setConfiguration(globalConfiguration);
                    instance.disposable.dispose();
                }, error -> {
                    Timber.e(error, "EnvironmentManager: Can't download GlobalConfiguration");
                    error.printStackTrace();
                    instance.disposable.dispose();
                });
    }

    public static void setCurrentEnvironment(Environment current) {
        PreferenceManager.getDefaultSharedPreferences(App.getAppContext())
                .edit()
                .putString(PrefsUtil.GLOBAL_CURRENT_ENVIRONMENT, current.name)
                .remove(PrefsUtil.GLOBAL_CURRENT_ENVIRONMENT_DATA)
                .apply();
        restartApp();
    }

    public static String getEnvironmentName() {
        SharedPreferences preferenceManager = PreferenceManager
                .getDefaultSharedPreferences(instance.application);
        return preferenceManager.getString(
                PrefsUtil.GLOBAL_CURRENT_ENVIRONMENT, Environment.MAIN_NET.name);
    }

    public static GlobalConfiguration.GeneralAssetId findAssetIdByAssetId(@NotNull String assetId) {
        for (GlobalConfiguration.GeneralAssetId asset : instance.current.configuration.getGeneralAssetIds()) {
            if (asset.getAssetId().equals(assetId)) {
                return asset;
            }
        }
        return null;
    }

    public static byte getNetCode() {
        return (byte) getEnvironment().configuration.getScheme().charAt(0);
    }

    public static GlobalConfiguration getGlobalConfiguration() {
        return getEnvironment().configuration;
    }

    public static String getName() {
        return getEnvironment().name;
    }

    public static GlobalConfiguration.Servers getServers() {
        return getEnvironment().configuration.getServers();
    }

    public static Environment getEnvironment() {
        return instance.current;
    }

    private static void restartApp() {
        handler.postDelayed(() -> {
            PackageManager packageManager = App.getAppContext().getPackageManager();
            Intent intent = packageManager.getLaunchIntentForPackage(App.getAppContext().getPackageName());
            if (intent != null) {
                ComponentName componentName = intent.getComponent();
                Intent mainIntent = Intent.makeRestartActivityTask(componentName);
                App.getAppContext().startActivity(mainIntent);
                System.exit(0);
            }
        }, 300);
    }

    public static class Environment {

        private String name;
        private String url;
        private GlobalConfiguration configuration;

        static List<Environment> environments = new ArrayList<>();
        public static Environment TEST_NET = new Environment(
                KEY_ENV_TEST_NET, URL_CONFIG_TEST_NET, EnvironmentConstants.TEST_NET_JSON);
        public static Environment MAIN_NET = new Environment(
                KEY_ENV_MAIN_NET, URL_CONFIG_MAIN_NET, EnvironmentConstants.MAIN_NET_JSON);

        static {
            environments.add(TEST_NET);
            environments.add(MAIN_NET);
        }

        Environment(String name, String url, String json) {
            this.name = name;
            this.url = url;
            this.configuration = new Gson().fromJson(json, GlobalConfiguration.class);
        }

        void setConfiguration(GlobalConfiguration configuration) {
            this.configuration = configuration;
        }

        public String getUrl() {
            return url;
        }
    }
}