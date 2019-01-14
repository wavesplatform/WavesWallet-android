package com.wavesplatform.wallet.v1.ui.auth;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.wavesplatform.wallet.v1.util.AppUtil;
import com.wavesplatform.wallet.v1.util.PrefsUtil;

public class EnvironmentManager {

    public static final String KEY_ENV_PROD = "env_prod";
    public static final String KEY_ENV_TEST_NET = "env_testnet";

    private static EnvironmentManager instance;

    private Environment current;

    private PrefsUtil prefsUtil;
    private AppUtil appUtil;

    private EnvironmentManager(PrefsUtil prefsUtil, AppUtil appUtil) {
        this.prefsUtil = prefsUtil;
        this.appUtil = appUtil;
        this.current = Environment.fromString(prefsUtil.getEnvironment());
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

    public void setCurrent(Context context, Environment current) {
        prefsUtil.setGlobalValue(PrefsUtil.GLOBAL_CURRENT_ENVIRONMENT, current.getName());
        restartApp(context);
        // appUtil.restartApp();
    }

    private static void restartApp(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(context.getPackageName());
        assert intent != null;
        ComponentName componentName = intent.getComponent();
        Intent mainIntent = Intent.makeRestartActivityTask(componentName);
        context.startActivity(mainIntent);
        System.exit(0);
    }

    public enum Environment {

        PRODUCTION(KEY_ENV_PROD,
                "https://nodes.wavesplatform.com/",
                "https://api.wavesplatform.com/",
                "https://matcher.wavesplatform.com/",
                'W',
                "5WvPKSJXzVE2orvbkJ8wsQmmQKqTv9sGBPksV4adViw3",
                "8LQW8f7P5d5PZM7GtZEBgaqRPGSzS3DfPuiXrURJ4AJS",
                "474jTeYx2r2Va35794tCScAXWJG9hU2HcgxzMowaZUnu",
                "zMFqXuoyrn5w17PFurTqxB7GsS71fp9dfk6XFwxbPCy",
                "HZk1mbfuJpmxU1Fs4AX5MWLVYtctsNcg6e2C6VKqK8zk",
                "BrjUWjndUanm5VsJkbUip8VRYy6LWJePtxya3FNv4TQa",
                "B3uGHFRpSUuGEDWjqB9LWWxafQj8VTvpMucEyoxzws5H",
                "Ft8X1v1LTa1ABafufpaCWyVj8KkaxUWE6xBhW6sNFJck",
                "Gtb1WRznfchDnTh37ezoDTJ4wcoKaRsKqKjJjy7nm2zU",
                "2mX5DzVKWrAJw8iwdJnV2qtoeVG9h5nTDpTqC1wb1WEN"),

        TEST(KEY_ENV_TEST_NET,
                "https://pool.testnet.wavesnodes.com/",
                "https://api.testnet.wavesplatform.com/",
                "https://matcher.testnet.wavesnodes.com/",
                'T',
                "8oPbSCKFHkXBy1hCGSg9pJkSARE7zhTQTLpc8KZwdtr7",
                "DWgwcZTMhSvnyYCoWLRUXXSH1RSkzThXLJhww9gwkqdn",
                "BrmjyAWT5jjr3Wpsiyivyvg5vDuzoX2s93WgiexXetB3",
                "8HT8tXwrXAYqwm8XrZ2hywWWTUAXxobHB5DakVC1y6jn",
                "BNdAstuFogzSyN2rY3beJbnBYwYcu7RzTHFjW88g8roK",
                "CFg2KQfkUgUVM2jFCMC5Xh8T8zrebvPc4HjHPfAugU1S",
                "DGgBtwVoXKAKKvV2ayUpSoPzTJxt7jo9KiXMJRzTH2ET",
                "D6N2rAqWN6ZCWnCeNFWLGqqjS6nJLeK4m19XiuhdDenr",
                "AsuWyM9MUUsMmWkK7jS48L3ky6gA1pxx7QtEYPbfLjAJ",
                "7itsmgdmomeTXvZzaaxqF3346h4FhciRoWceEw9asNV3");

        private String name;
        private String nodes;
        private String api;
        private String matcher;
        private char netCode;
        private String moneroAssetId;
        private String bitcoinAssetId;
        private String ethereumAssetId;
        private String bitcoincashAssetId;
        private String lightcoinAssetId;
        private String zecAssetId;
        private String dashAssetId;
        private String wusdAssetId;
        private String weurAssetId;
        private String wtryAssetId;

        Environment(String name, String nodes, String api, String matcher, char netCode,
                    String moneroAssetId, String bitcoinAssetId, String ethereumAssetId,
                    String bitcoincashAssetId, String lightcoinAssetId, String zecAssetId,
                    String dashAssetId, String wusdAssetId, String weurAssetId, String wtryAssetId) {
            this.name = name;
            this.nodes = nodes;
            this.api = api;
            this.matcher = matcher;
            this.netCode = netCode;
            this.moneroAssetId = moneroAssetId;
            this.bitcoinAssetId = bitcoinAssetId;
            this.ethereumAssetId = ethereumAssetId;
            this.bitcoincashAssetId = bitcoincashAssetId;
            this.lightcoinAssetId = lightcoinAssetId;
            this.zecAssetId = zecAssetId;
            this.dashAssetId = dashAssetId;
            this.wusdAssetId = wusdAssetId;
            this.weurAssetId = weurAssetId;
            this.wtryAssetId = wtryAssetId;
        }

        public String getName() {
            return name;
        }

        public String getNodes() {
            return nodes;
        }

        public String getApi() {
            return api;
        }

        public String getMatcher() {
            return matcher;
        }

        public char getNetCode() {
            return netCode;
        }

        public String getMoneroAssetId() {
            return moneroAssetId;
        }

        public String getBitcoinAssetId() {
            return bitcoinAssetId;
        }

        public String getEthereumAssetId() {
            return ethereumAssetId;
        }

        public String getBitcoincashAssetId() {
            return bitcoincashAssetId;
        }

        public String getLightcoinAssetId() {
            return lightcoinAssetId;
        }

        public String getZecAssetId() {
            return zecAssetId;
        }

        public String getDashAssetId() {
            return dashAssetId;
        }

        public String getWusdAssetId() {
            return wusdAssetId;
        }

        public String getWeurAssetId() {
            return weurAssetId;
        }

        public String getWtryAssetId() {
            return wtryAssetId;
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
