package com.wavesplatform.wallet.v2.data.helpers

import android.util.Log
import com.vicpin.krealmextensions.RealmConfigStore
import com.vicpin.krealmextensions.queryFirst
import com.vicpin.krealmextensions.saveAll
import com.wavesplatform.wallet.v1.ui.auth.EnvironmentManager
import com.wavesplatform.wallet.v1.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.model.remote.response.*
import com.wavesplatform.wallet.v2.data.database.DBHelper
import com.wavesplatform.wallet.v2.data.database.RealmMigrations
import com.wavesplatform.wallet.v2.data.manager.NodeDataManager
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookUser
import com.wavesplatform.wallet.v2.util.MigrationUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import io.realm.RealmConfiguration
import javax.inject.Inject

class AuthHelper @Inject constructor(private var prefsUtil: PrefsUtil, var nodeDataManager: NodeDataManager) {

    fun configureDB(address: String?, guid: String) {

        // check db with old name
        MigrationUtil.checkPrevDbAndRename(address, guid)

        val config = RealmConfiguration.Builder()
                .name(String.format("%s.realm", guid))
                .schemaVersion(2)
                .migration(RealmMigrations())
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
        RealmConfigStore.init(AddressBookUser::class.java, config)
        RealmConfigStore.init(AssetInfo::class.java, config)
        RealmConfigStore.init(MarketResponse::class.java, config)

        DBHelper.getInstance().realmConfig = config
        Realm.getInstance(config).isAutoRefresh = false

        saveDefaultAssets()
    }

    private fun saveDefaultAssets() {
        val list = mutableListOf<String>()
        for (asset in EnvironmentManager.getGlobalConfiguration().generalAssetIds) {
            list.add(asset.assetId)
        }
        nodeDataManager.apiDataManager.assetsInfoByIds(list)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ list ->
                    for (assetInfo in list) {
                        val assetBalance = AssetBalance(assetId = assetInfo.id,
                                quantity = assetInfo.quantity,
                                isFavorite = assetInfo.id == "WAVES",
                                issueTransaction = IssueTransaction(
                                        name = assetInfo.name,
                                        decimals = assetInfo.precision,
                                        quantity = assetInfo.quantity,
                                        timestamp = assetInfo.timestamp.time),
                                isGateway = EnvironmentManager.findAssetIdByAssetId(assetInfo.id).isGateway)

                        val listToSave = arrayListOf<AssetBalance>()
                        val asset = queryFirst<AssetBalance> { equalTo("assetId", assetInfo.id) }
                        if (asset == null) {
                            listToSave.add(assetBalance)
                        }
                        if (listToSave.isNotEmpty()) {
                            listToSave.saveAll()
                        }
                    }
                    prefsUtil.setValue(PrefsUtil.KEY_DEFAULT_ASSETS, true)
                }, { error ->
                    Log.d("AuthHelper", "saveDefaultAssets: error")
                })
    }
}
