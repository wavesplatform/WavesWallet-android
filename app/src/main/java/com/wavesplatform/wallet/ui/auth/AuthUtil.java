package com.wavesplatform.wallet.ui.auth;

import android.app.Activity;
import android.content.Intent;

import com.wavesplatform.wallet.api.NodeManager;
import com.wavesplatform.wallet.api.datafeed.DataFeedManager;
import com.wavesplatform.wallet.api.matcher.MatcherManager;
import com.wavesplatform.wallet.db.DBHelper;
import com.wavesplatform.wallet.ui.home.MainActivity;
import com.wavesplatform.wallet.util.PrefsUtil;

import io.realm.RealmConfiguration;

public class AuthUtil {
    public static boolean startMainActivity(Activity parent, String publicKey) {
        if (NodeManager.createInstance(publicKey) != null) {
            DataFeedManager.createInstance();
            MatcherManager.createInstance(publicKey);

            NodeManager.get().setPrefsUtil(new PrefsUtil(parent));

            RealmConfiguration config = new RealmConfiguration.Builder()
                    .name(String.format("%s.realm", publicKey))
                    .deleteRealmIfMigrationNeeded()
                    .build();
            DBHelper.getInstance().setRealmConfig(config);

            Intent intent = new Intent(parent, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            parent.startActivity(intent);
            return true;
        } else {
            return false;
        }
    }
}
