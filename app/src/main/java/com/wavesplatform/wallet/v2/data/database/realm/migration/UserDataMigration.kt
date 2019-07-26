/*
 * Created by Eduard Zaydel on 5/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.database.realm.migration

import io.realm.DynamicRealm
import io.realm.RealmMigration

class UserDataMigration : RealmMigration {

    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        if (oldVersion < 2) {
            val schema = realm.schema
            schema.rename("AddressBookUser", "AddressBookUserDb")
            schema.rename("AssetBalanceStore", "AssetBalanceStoreDb")
            schema.rename("MarketResponse", "MarketResponseDb")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}