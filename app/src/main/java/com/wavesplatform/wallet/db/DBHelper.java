package com.wavesplatform.wallet.db;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class DBHelper {
    public static DBHelper dbHelper;
    private RealmConfiguration realmConfiguration;

    public static DBHelper getInstance() {
        if (dbHelper == null) {
            dbHelper = new DBHelper();
        }
        return dbHelper;
    }

    public void setRealmConfig(RealmConfiguration realmConfiguration) {
        this.realmConfiguration = realmConfiguration;
    }

    public Realm getRealm() {
        return Realm.getInstance(realmConfiguration);
    }

}