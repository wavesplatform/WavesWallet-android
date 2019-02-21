package com.wavesplatform.wallet.v1.ui.auth;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.wavesplatform.wallet.App;
import com.wavesplatform.wallet.v1.util.PrefsUtil;
import com.wavesplatform.wallet.v2.data.manager.MatcherDataManager;
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance;
import com.wavesplatform.wallet.v2.data.model.remote.response.GlobalConfiguration;
import com.wavesplatform.wallet.v2.data.model.remote.response.IssueTransaction;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
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

    private Environment current;
    private static Handler handler = new Handler();
    private Application application;

    public static void init(Application application) {
        instance = new EnvironmentManager();
        instance.application = application;
        instance.current = Environment.find(getEnvironment());
    }

    public static void updateConfiguration(MatcherDataManager matcherDataManager) {
        matcherDataManager.apiService.loadGlobalConfiguration(instance.current.url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(globalConfiguration -> {
                    SharedPreferences preferenceManager = PreferenceManager
                            .getDefaultSharedPreferences(App.getAppContext());
                    SharedPreferences.Editor editor = preferenceManager.edit();
                    editor.putString(PrefsUtil.GLOBAL_CURRENT_ENVIRONMENT_DATA,
                            new Gson().toJson(globalConfiguration))
                            .apply();
                    instance.current.setConfiguration(globalConfiguration);
                }, error -> {
                    Timber.e(error, "EnvironmentManager: Can't download GlobalConfiguration");
                    error.printStackTrace();
                });
    }


    public static EnvironmentManager get() {
        return instance;
    }

    public Environment current() {
        return current;
    }

    public static void setCurrentEnvironment(Environment current) {
        SharedPreferences preferenceManager = PreferenceManager
                .getDefaultSharedPreferences(App.getAppContext());
        preferenceManager
                .edit()
                .putString(PrefsUtil.GLOBAL_CURRENT_ENVIRONMENT, current.getName())
                .remove(PrefsUtil.GLOBAL_CURRENT_ENVIRONMENT_DATA)
                .apply();
        handler.postDelayed(EnvironmentManager::restartApp, 500);
    }

    public static String getEnvironment() {
        SharedPreferences preferenceManager = PreferenceManager
                .getDefaultSharedPreferences(instance.application);
        return preferenceManager.getString(
                PrefsUtil.GLOBAL_CURRENT_ENVIRONMENT, Environment.MAIN_NET.name);
    }

    public static GlobalConfiguration.GeneralAssetId findAssetIdByAssetId(@NotNull String assetId) {
        return get().current().findAssetByAssetId(assetId);
    }

    public static byte getNetCode() {
        return get().current().getNetCode();
    }

    public static GlobalConfiguration getGlobalConfiguration() {
        return get().current().getGlobalConfiguration();
    }

    private static void restartApp() {
        PackageManager packageManager = App.getAppContext().getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(App.getAppContext().getPackageName());
        assert intent != null;
        ComponentName componentName = intent.getComponent();
        Intent mainIntent = Intent.makeRestartActivityTask(componentName);
        App.getAppContext().startActivity(mainIntent);
        System.exit(0);
    }

    public static class Environment {

        private String name;
        private String url;
        private GlobalConfiguration configuration;
        public static Environment TEST_NET = new Environment(
                KEY_ENV_TEST_NET, URL_CONFIG_TEST_NET, EnvironmentConstants.TEST_NET_JSON);
        public static Environment MAIN_NET = new Environment(
                KEY_ENV_MAIN_NET, URL_CONFIG_MAIN_NET, EnvironmentConstants.MAIN_NET_JSON);
        static List<Environment> environments = new ArrayList<>();

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

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }

        public GlobalConfiguration getGlobalConfiguration() {
            return configuration;
        }

        public byte getNetCode() {
            return (byte) configuration.getScheme().charAt(0);
        }

        GlobalConfiguration.GeneralAssetId findAssetId(String gatewayId) {
            for (GlobalConfiguration.GeneralAssetId assetId : configuration.getGeneralAssetIds()) {
                if (assetId.getGatewayId().equals(gatewayId)) {
                    return assetId;
                }
            }
            return null;
        }

        GlobalConfiguration.GeneralAssetId findAssetByAssetId(String assetId) {
            for (GlobalConfiguration.GeneralAssetId asset : configuration.getGeneralAssetIds()) {
                if (asset.getAssetId().equals(assetId)) {
                    return asset;
                }
            }
            return null;
        }

        static Environment find(String name) {
            if (name != null) {
                for (Environment environment : environments) {
                    if (name.equalsIgnoreCase(environment.getName())) {
                        SharedPreferences preferenceManager = PreferenceManager
                                .getDefaultSharedPreferences(instance.application);
                        String json = preferenceManager.getString(
                                PrefsUtil.GLOBAL_CURRENT_ENVIRONMENT_DATA,
                                EnvironmentConstants.MAIN_NET_JSON);
                        environment.setConfiguration(new Gson().fromJson(json, GlobalConfiguration.class));
                        return environment;
                    }
                }
            }
            return null;
        }
    }
}
