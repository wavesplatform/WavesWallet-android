package com.wavesplatform.wallet.v2.data.helpers

import com.vicpin.krealmextensions.RealmConfigStore
import com.vicpin.krealmextensions.queryFirst
import com.vicpin.krealmextensions.save
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v1.ui.auth.EnvironmentManager
import com.wavesplatform.wallet.v1.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.database.DBHelper
import com.wavesplatform.wallet.v2.data.model.remote.response.*
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookUser
import com.wavesplatform.wallet.v2.util.MigrationUtil
import io.realm.DynamicRealm
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

        RealmConfigStore.init(AssetBalance::class.java, config)
        RealmConfigStore.init(IssueTransaction::class.java, config)
        RealmConfigStore.init(Transaction::class.java, config)
        RealmConfigStore.init(Transfer::class.java, config)
        RealmConfigStore.init(Data::class.java, config)
        RealmConfigStore.init(AssetPair::class.java, config)
        RealmConfigStore.init(Order::class.java, config)
        RealmConfigStore.init(Lease::class.java, config)
        RealmConfigStore.init(Alias::class.java, config)
        RealmConfigStore.init(SpamAsset::class.java, config)
        RealmConfigStore.init(AssetInfo::class.java, config)
        RealmConfigStore.init(MarketResponse::class.java, config)

        DBHelper.getInstance().realmConfig = config
        Realm.getInstance(config).isAutoRefresh = false

        saveDefaultAssets()
    }

    private fun migration(guid: String, address: String?) {
        val initConfig = RealmConfiguration.Builder()
                .name(String.format("%s.realm", guid))
                .build()
        val tempRealm = DynamicRealm.getInstance(initConfig)
        if (tempRealm!!.version < 3) {
            val addressBookUsersDb = tempRealm.where("AddressBookUser").findAll()
            val addressBookUsers = prefsUtil.allAddressBookUsers
            for (item in addressBookUsersDb) {
                val addressBookUser = AddressBookUser(
                        item.getString("address"),
                        item.getString("name"))
                addressBookUsers.add(addressBookUser)
            }
            prefsUtil.setAddressBookUsers(addressBookUsers)

            val assetBalances = hashMapOf<String, AssetBalanceStore>()
            val assetBalancesDb = tempRealm.where("AssetBalance").findAll()
            for (item in assetBalancesDb) {
                assetBalances[item.getString("assetId")] =
                        AssetBalanceStore(
                                assetId = item.getString("assetId"),
                                isHidden = item.getBoolean("isHidden"),
                                isFavorite = item.getBoolean("isFavorite"),
                                position = item.getInt("position"))
            }
            prefsUtil.saveAssetBalances(assetBalances)


            tempRealm.close()
            App.getAccessManager().deleteRealm(guid)
        } else {
            tempRealm.close()
        }
        MigrationUtil.checkPrevDbAndRename(address, guid)
        MigrationUtil.checkOldAddressBook(prefsUtil, guid)
    }

    private fun saveDefaultAssets() {
        EnvironmentManager.defaultAssets.forEach {
            val asset = queryFirst<AssetBalance> { equalTo("assetId", it.assetId) }
            if (asset == null) {
                it.save()
            }
        }
        prefsUtil.setValue(PrefsUtil.KEY_DEFAULT_ASSETS, true)
    }
}
