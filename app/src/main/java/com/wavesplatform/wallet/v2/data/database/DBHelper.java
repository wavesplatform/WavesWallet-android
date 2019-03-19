package com.wavesplatform.wallet.v2.data.database;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class DBHelper {
    public static DBHelper dbHelper;
    private RealmConfiguration realmConfiguration;
    private RealmConfiguration realmUserDataConfiguration;

    public static DBHelper getInstance() {
        if (dbHelper == null) {
            dbHelper = new DBHelper();
        }
        return dbHelper;
    }

    public void setRealmConfig(RealmConfiguration realmConfiguration) {
        this.realmConfiguration = realmConfiguration;
    }

    public void setRealmUserDataConfig(RealmConfiguration realmConfiguration) {
        this.realmUserDataConfiguration = realmConfiguration;
    }

    public RealmConfiguration getRealmConfig() {
        return realmConfiguration;
    }

    public Realm getRealm() {
        return Realm.getInstance(realmConfiguration);
    }

    public RealmConfiguration getRealmUserDataConfig() {
        return realmUserDataConfiguration;
    }

    public Realm getRealmUserData() {
        return Realm.getInstance(realmUserDataConfiguration);
    }

}