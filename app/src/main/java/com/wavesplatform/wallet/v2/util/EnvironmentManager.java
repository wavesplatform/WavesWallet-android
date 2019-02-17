package com.wavesplatform.wallet.v2.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;

import com.google.gson.Gson;
import com.wavesplatform.wallet.App;
import com.wavesplatform.sdk.model.response.GlobalConfiguration;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

public class EnvironmentManager {

    public static final String KEY_ENV_MAIN_NET = "env_prod";
    public static final String KEY_ENV_TEST_NET = "env_testnet";
    public static final String URL_COMMISSION_MAIN_NET = "https://github-proxy.wvservices.com/" +
            "wavesplatform/waves-client-config/master/fee.json";
    private static final String URL_CONFIG_MAIN_NET = "https://github-proxy.wvservices.com/" +
            "wavesplatform/waves-client-config/master/environment_mainnet.json";
    private static final String URL_CONFIG_TEST_NET = "https://github-proxy.wvservices.com/" +
            "wavesplatform/waves-client-config/master/environment_testnet.json";
    private static final String JSON_FILENAME_MAIN_NET = "environment_mainnet.json";
    private static final String JSON_FILENAME_TEST_NET = "environment_testnet.json";

    private static EnvironmentManager instance;

    private Environment current;
    private PrefsUtil prefsUtil;
    private Handler handler = new Handler();

    private EnvironmentManager(Context context) {
        this.prefsUtil = new PrefsUtil(context);
        this.current = Environment.find(prefsUtil.getEnvironment());
    }

    public static void init(Context context) {
        instance = new EnvironmentManager(context);
    }

    public static EnvironmentManager get() {
        return instance;
    }

    public Environment current() {
        return current;
    }

    public void setCurrent(Environment current) {
        prefsUtil.setGlobalValue(PrefsUtil.GLOBAL_CURRENT_ENVIRONMENT, current.getName());
        handler.postDelayed(EnvironmentManager::restartApp, 500);
    }

    public static GlobalConfiguration.GeneralAssetId findAssetId(@NotNull String gatewayId) {
        return get().current().findAssetId(gatewayId);
    }

    public static byte getNetCode() {
        return get().current().getNetCode();
    }

    public static GlobalConfiguration getGlobalConfiguration() {
        return get().current().getGlobalConfiguration();
    }

    private static GlobalConfiguration getConfiguration(String fileName) {
        String json;
        try {
            InputStream is = App.getAppContext().getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return new Gson().fromJson(json, GlobalConfiguration.class);
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

    public enum Environment {

        MAIN_NET(KEY_ENV_MAIN_NET, URL_CONFIG_MAIN_NET, JSON_FILENAME_MAIN_NET),
        TEST_NET(KEY_ENV_TEST_NET, URL_CONFIG_TEST_NET, JSON_FILENAME_TEST_NET);

        private String name;
        private String url;
        private GlobalConfiguration configuration;

        Environment(String name, String url, String json) {
            this.name = name;
            this.url = url;
            this.configuration = getConfiguration(json);
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

        public GlobalConfiguration.GeneralAssetId findAssetId(String gatewayId) {
            for (GlobalConfiguration.GeneralAssetId assetId : configuration.getGeneralAssetIds()) {
                if (assetId.getGatewayId().equals(gatewayId)) {
                    return assetId;
                }
            }
            return null;
        }

        public static Environment find(String name) {
            if (name != null) {
                for (Environment anAll : values()) {
                    if (name.equalsIgnoreCase(anAll.getName())) {
                        return anAll;
                    }
                }
            }
            return null;
        }
    }
}
