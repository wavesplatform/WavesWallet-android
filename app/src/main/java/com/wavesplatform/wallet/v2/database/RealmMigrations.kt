package com.wavesplatform.wallet.v2.database

import io.realm.DynamicRealm
import io.realm.RealmMigration


class RealmMigrations : RealmMigration {

    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        if (oldVersion < 1L) {
            val userSchema = realm.schema.get("Transaction")
            userSchema!!.addField("script", String::class.java)
            userSchema!!.addField("minSponsoredAssetFee", String::class.java)
        }
    }
}