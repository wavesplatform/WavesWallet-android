package com.wavesplatform.wallet.v2.data.helpers

import com.vicpin.krealmextensions.RealmConfigStore
import com.vicpin.krealmextensions.queryFirst
import com.vicpin.krealmextensions.saveAll
import com.wavesplatform.wallet.v2.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.database.DBHelper
import com.wavesplatform.wallet.v2.data.database.RealmMigrations
import com.wavesplatform.wallet.v2.data.model.db.*
import com.wavesplatform.wallet.v2.util.MigrationUtil
import io.realm.Realm
import io.realm.RealmConfiguration
import pyxis.uzuki.live.richutilskt.utils.runAsync
import javax.inject.Inject

class AuthHelper @Inject constructor(private var prefsUtil: PrefsUtil) {

    fun configureDB(address: String?, guid: String) {

        // check db with old name
        MigrationUtil.checkPrevDbAndRename(address, guid)

        val config = RealmConfiguration.Builder()
                .name(String.format("%s.realm", guid))
                .schemaVersion(2)
                .migration(RealmMigrations())
                .build()

        Realm.compactRealm(config)

        RealmConfigStore.init(AssetBalanceDb::class.java, config)
        RealmConfigStore.init(IssueTransactionDb::class.java, config)
        RealmConfigStore.init(TransactionDb::class.java, config)
        RealmConfigStore.init(TransferDb::class.java, config)
        RealmConfigStore.init(DataDb::class.java, config)
        RealmConfigStore.init(AssetPairDb::class.java, config)
        RealmConfigStore.init(OrderDb::class.java, config)
        RealmConfigStore.init(LeaseDb::class.java, config)
        RealmConfigStore.init(AliasDb::class.java, config)
        RealmConfigStore.init(SpamAssetDb::class.java, config)
        RealmConfigStore.init(AddressBookUserDb::class.java, config)
        RealmConfigStore.init(AssetInfoDb::class.java, config)
        RealmConfigStore.init(MarketResponseDb::class.java, config)

        DBHelper.getInstance().realmConfig = config
        Realm.getInstance(config).isAutoRefresh = false

        saveDefaultAssets()
    }

    private fun saveDefaultAssets() {
        runAsync {
            AssetBalanceDb.convertToDb(Constants.defaultAssets).forEach {
                val listToSave = arrayListOf<AssetBalanceDb>()
                val asset = queryFirst<AssetBalanceDb> { equalTo("assetId", it.assetId) }
                if (asset == null) {
                    listToSave.add(it)
                }
                if (listToSave.isNotEmpty()) listToSave.saveAll()
            }
            prefsUtil.setValue(PrefsUtil.KEY_DEFAULT_ASSETS, true)
        }
    }
}
