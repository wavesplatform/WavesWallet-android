package com.wavesplatform.wallet.v1.ui.auth;

import com.wavesplatform.wallet.v1.util.AppUtil;
import com.wavesplatform.wallet.v1.util.PrefsUtil;

public class EnvironmentManager {
    private static final String TAG = EnvironmentManager.class.getSimpleName();

    public static final String KEY_ENV_PROD = "env_prod";
    private static final String KEY_ENV_TESTNET = "env_testnet";

    private static EnvironmentManager instance;

    private Environment current;

    private PrefsUtil prefsUtil;
    private AppUtil appUtil;

    private EnvironmentManager(PrefsUtil prefsUtil, AppUtil appUtil) {
        this.prefsUtil = prefsUtil;
        this.appUtil = appUtil;
        String storedEnv = prefsUtil.getEnvironment();
        this.current = Environment.fromString(storedEnv);
    }

    public static void init(PrefsUtil prefsUtil, AppUtil appUtil) {
        instance = new EnvironmentManager(prefsUtil, appUtil);
    }

    public static EnvironmentManager get() {
        return instance;
    }

    public Environment current() {
        return current;
    }

    public void setCurrent(Environment current) {
        prefsUtil.setGlobalValue(PrefsUtil.GLOBAL_CURRENT_ENVIRONMENT, current.getName());
        this.current = current;
        appUtil.restartApp();
    }

    public enum Environment {
        PRODUCTION(KEY_ENV_PROD, "https://nodes.wavesplatform.com", "https://matcher.wavesplatform.com/", "https://marketdata.wavesplatform.com/api/", 'W'),
        TESTNET(KEY_ENV_TESTNET, "http://52.30.47.67:6869", "http://52.30.47.67:6886/", "https://marketdata.wavesplatform.com/api/", 'T');

        private String name;
        private String nodeUrl;

        Environment(String name, String nodeUrl, String matherUrl, String dataFeedUrl, char addressScheme) {
            this.name = name;
            this.nodeUrl = nodeUrl;
        }

        public String getName() {
            return name;
        }

        public String getNodeUrl() {
            return nodeUrl;
        }

        public static Environment fromString(String text) {
            if (text != null) {
                Environment[] all = values();

                for (Environment anAll : all) {
                    if (text.equalsIgnoreCase(anAll.getName())) {
                        return anAll;
                    }
                }
            }
            return null;
        }
    }
}
