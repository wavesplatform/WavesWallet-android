package com.wavesplatform.wallet.v2.data.database

import com.wavesplatform.wallet.v2.util.notNull
import io.realm.DynamicRealm
import io.realm.RealmMigration

class RealmMigrations : RealmMigration {

    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        var oldVersion = oldVersion

        if (oldVersion < 1L) {
            realm.where("Transaction").findAll().deleteAllFromRealm()
            val schema = realm.schema.get("Transaction")
            schema.notNull {
                it.addField("script", String::class.java)
                it.addField("minSponsoredAssetFee", String::class.java)
            }
            oldVersion++
        }
        if (oldVersion == 1L) {
            realm.where("Transaction").findAll().deleteAllFromRealm()
            oldVersion++
        }
    }

    override fun hashCode(): Int {
        return RealmMigrations::class.java.hashCode()
    }

    override fun equals(any: Any?): Boolean {
        return if (any == null) {
            false
        } else any is RealmMigrations
    }
}