/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.helpers

import com.vicpin.krealmextensions.RealmConfigStore
import com.vicpin.krealmextensions.queryFirst
import com.vicpin.krealmextensions.save
import com.wavesplatform.sdk.utils.EnvironmentManager
import com.wavesplatform.wallet.v2.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.database.DBHelper
import com.wavesplatform.wallet.v2.data.database.realm.migration.UserDataMigration
import com.wavesplatform.wallet.v2.data.database.realm.module.DataModule
import com.wavesplatform.wallet.v2.data.database.realm.module.UserDataModule
import com.wavesplatform.wallet.v2.data.model.remote.response.*
import com.wavesplatform.wallet.v2.data.model.userdb.AssetBalanceStore
import com.wavesplatform.wallet.v2.data.model.userdb.MarketResponseDb
import com.wavesplatform.wallet.v2.util.MigrationUtil
import io.realm.Realm
import io.realm.RealmConfiguration
import javax.inject.Inject

class AuthHelper @Inject constructor(private var prefsUtil: PrefsUtil) {

    fun configureDB(address: String?, guid: String) {

        val configUserData = RealmConfiguration.Builder()
                .modules(UserDataModule())
                .name(String.format("%s_userdata.realm", guid))
                .schemaVersion(1)
                .build()
        Realm.compactRealm(configUserData)
        RealmConfigStore.initModule(UserDataModule::class.java, configUserData)
        DBHelper.getInstance().realmUserDataConfig = configUserData
        Realm.getInstance(configUserData).isAutoRefresh = false

        migration(guid, address)

        val config = RealmConfiguration.Builder()
                .modules(DataModule())
                .name(String.format("%s.realm", guid))
                .schemaVersion(MigrationUtil.VER_DB_WITHOUT_USER_DATA)
                .deleteRealmIfMigrationNeeded()
                .build()
        Realm.compactRealm(config)
        RealmConfigStore.initModule(DataModule::class.java, config)
        DBHelper.getInstance().realmConfig = config
        Realm.getInstance(config).isAutoRefresh = false

        saveDefaultAssets()
    }

    private fun migration(guid: String, address: String?) {
        MigrationUtil.copyPrefDataFromDb(guid)
        MigrationUtil.checkPrevDbAndRename(address, guid)
        MigrationUtil.checkOldAddressBook(prefsUtil, guid)
    }

    private fun saveDefaultAssets() {
        EnvironmentManager.defaultAssets.forEach {
            val asset = queryFirst<AssetBalanceDb> { equalTo("assetId", it.assetId) }
            if (asset == null) {
                AssetBalanceDb(it).save()
            }
        }
        prefsUtil.setValue(PrefsUtil.KEY_DEFAULT_ASSETS, true)
    }
}
