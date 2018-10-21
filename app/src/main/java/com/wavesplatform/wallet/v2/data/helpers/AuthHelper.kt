package com.wavesplatform.wallet.v2.data.helpers

import com.vicpin.krealmextensions.RealmConfigStore
import com.vicpin.krealmextensions.saveAll
import com.wavesplatform.wallet.v1.db.DBHelper
import com.wavesplatform.wallet.v1.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.remote.response.*
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookUser
import io.realm.Realm
import io.realm.RealmConfiguration
import pyxis.uzuki.live.richutilskt.utils.runAsync
import javax.inject.Inject

class AuthHelper @Inject constructor(private var prefsUtil: PrefsUtil) {

    fun configureDB(address: String?) {
        val config = RealmConfiguration.Builder()
                .name(String.format("%s.realm", address))
                .deleteRealmIfMigrationNeeded()
                .build()

        Realm.compactRealm(config)

        RealmConfigStore.init(AssetBalance::class.java, config)
        RealmConfigStore.init(IssueTransaction::class.java, config)
        RealmConfigStore.init(Transaction::class.java, config)
        RealmConfigStore.init(Transfer::class.java, config)
        RealmConfigStore.init(AssetPair::class.java, config)
        RealmConfigStore.init(Order::class.java, config)
        RealmConfigStore.init(Lease::class.java, config)
        RealmConfigStore.init(Alias::class.java, config)
        RealmConfigStore.init(SpamAsset::class.java, config)
        RealmConfigStore.init(AddressBookUser::class.java, config)
        RealmConfigStore.init(AssetInfo::class.java, config)

        DBHelper.getInstance().realmConfig = config
        Realm.getInstance(config).isAutoRefresh = false

        if (!prefsUtil.getValue(PrefsUtil.KEY_DEFAULT_ASSETS, false)) {
            runAsync {
                Constants.defaultAssets.saveAll()
                prefsUtil.setValue(PrefsUtil.KEY_DEFAULT_ASSETS, true)
            }
        }

    }
}
