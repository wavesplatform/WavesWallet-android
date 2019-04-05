/*
 * Created by Eduard Zaydel on 5/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

/*
 * Created by Eduard Zaydel on 3/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.database.realm.migration

import io.realm.DynamicRealm
import io.realm.RealmMigration

class UserDataMigration : RealmMigration {

    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        var oldVersion = oldVersion
        val schema = realm.schema

        if (oldVersion <= 1L) {
            // first migration here

            oldVersion++
        }
    }
}