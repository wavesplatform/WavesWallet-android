/*
 * Created by Eduard Zaydel on 30/9/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.auth.migration.migrate_account

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.sdk.crypto.WavesCrypto
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.db.userdb.AddressBookUserDb
import com.wavesplatform.wallet.v2.data.model.local.MigrateAccountItem
import com.wavesplatform.wallet.v2.data.model.local.WalletItem
import com.wavesplatform.wallet.v2.ui.auth.new_account.secret_phrase.SecretPhraseView
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.EnvironmentManager
import com.wavesplatform.wallet.v2.util.PrefsUtil
import java.util.ArrayList
import javax.inject.Inject

@InjectViewState
class MigrateAccountPresenter @Inject constructor() : BasePresenter<MigrateAccountView>() {
    fun getAddresses() {
        // TODO: Multi account logic here
        val list = arrayListOf<MigrateAccountItem>()

        list.add(MigrateAccountItem(R.string.migrate_account_successfully_unlocked_header))

        val guids = prefsUtil.getGlobalValueList(
                EnvironmentManager.name + PrefsUtil.LIST_WALLET_GUIDS)
        for (i in guids.indices) {
            val publicKey = prefsUtil.getGlobalValue(guids[i] + PrefsUtil.KEY_PUB_KEY, "")
            val name = prefsUtil.getGlobalValue(guids[i] + PrefsUtil.KEY_WALLET_NAME, "")
            val address = WavesCrypto.addressFromPublicKey(WavesCrypto.base58decode(publicKey), EnvironmentManager.netCode)
            list.add(MigrateAccountItem(AddressBookUserDb(address, name), false))
        }

        //TODO: Remove test
        list.add(MigrateAccountItem(R.string.migrate_account_pending_unlock_header))
        list.add(MigrateAccountItem(AddressBookUserDb("test", "test"), true))

        viewState.afterSuccessGetAddress(list)
    }
}