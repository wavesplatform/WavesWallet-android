/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.auth.choose_account

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.sdk.crypto.WavesCrypto
import com.wavesplatform.wallet.v2.util.EnvironmentManager
import com.wavesplatform.wallet.v2.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.model.local.WalletItem
import com.wavesplatform.wallet.v2.data.model.db.userdb.AddressBookUserDb
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import java.util.*
import javax.inject.Inject

@InjectViewState
class ChooseAccountPresenter @Inject constructor() : BasePresenter<ChooseAccountView>() {
    fun getAddresses() {
        val list = arrayListOf<AddressBookUserDb>()
        val guids = prefsUtil.getGlobalValueList(
                EnvironmentManager.name + PrefsUtil.LIST_WALLET_GUIDS)
        val wallets = ArrayList<WalletItem>()
        for (i in guids.indices) {
            val publicKey = prefsUtil.getGlobalValue(guids[i] + PrefsUtil.KEY_PUB_KEY, "")
            val name = prefsUtil.getGlobalValue(guids[i] + PrefsUtil.KEY_WALLET_NAME, "")
            val address = WavesCrypto.addressFromPublicKey(
                    WavesCrypto.base58decode(publicKey), EnvironmentManager.netCode)
            wallets.add(WalletItem(guids[i], name, address, publicKey))
            list.add(AddressBookUserDb(address, name))
        }
        viewState.afterSuccessGetAddress(list)
    }
}
