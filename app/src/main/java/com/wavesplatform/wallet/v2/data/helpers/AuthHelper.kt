package com.wavesplatform.wallet.v2.data.helpers

import com.vicpin.krealmextensions.RealmConfigStore
import com.vicpin.krealmextensions.queryFirst
import com.vicpin.krealmextensions.save
import com.wavesplatform.sdk.utils.EnvironmentManager
import com.wavesplatform.wallet.v2.data.database.DBHelper
import com.wavesplatform.wallet.v2.data.model.db.*
import com.wavesplatform.wallet.v2.util.MigrationUtil
import com.wavesplatform.wallet.v2.util.PrefsUtil
import io.realm.Realm
import io.realm.RealmConfiguration
import javax.inject.Inject

class AuthHelper @Inject constructor(private var prefsUtil: PrefsUtil) {

    fun configureDB(address: String?, guid: String) {

        migration(guid, address)

        val config = RealmConfiguration.Builder()
                .name(String.format("%s.realm", guid))
                .schemaVersion(3)
                .deleteRealmIfMigrationNeeded()
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
        RealmConfigStore.init(AssetInfoDb::class.java, config)
        RealmConfigStore.init(MarketResponseDb::class.java, config)

        DBHelper.getInstance().realmConfig = config
        Realm.getInstance(config).isAutoRefresh = false

        saveDefaultAssets()
    }

    private fun migration(guid: String, address: String?) {
        MigrationUtil.copyPrefDataFromDb(prefsUtil, guid)
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
