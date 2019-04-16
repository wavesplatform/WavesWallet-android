/*
 * Created by Eduard Zaydel on 3/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.database.realm.migration

import android.util.Log
import io.realm.DynamicRealm
import io.realm.RealmMigration

class UserDataMigration : RealmMigration {

    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        Log.d("", "")
        if (oldVersion < 2) {
            val schema = realm.schema
            schema.rename("AddressBookUser", "AddressBookUserDb")
            schema.rename("AssetBalanceStore", "AssetBalanceStoreDb")
            schema.rename("MarketResponse", "MarketResponseDb")
        }
    }
}