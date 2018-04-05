package com.wavesplatform.wallet.v1.ui.auth;

import android.app.Activity;
import android.content.Intent;

import com.wavesplatform.wallet.v1.api.NodeManager;
import com.wavesplatform.wallet.v1.api.datafeed.DataFeedManager;
import com.wavesplatform.wallet.v1.api.mather.MatherManager;
import com.wavesplatform.wallet.v1.db.DBHelper;
import com.wavesplatform.wallet.v1.ui.home.MainActivity;

import io.realm.RealmConfiguration;

public class AuthUtil {
    public static boolean startMainActivity(Activity parent, String publicKey) {
        if (NodeManager.createInstance(publicKey) != null) {
            DataFeedManager.createInstance();
            MatherManager.createInstance(publicKey);

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
