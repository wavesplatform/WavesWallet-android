package com.wavesplatform.wallet.v2.ui.auth.choose_account

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.sdk.utils.EnvironmentManager
import com.wavesplatform.sdk.utils.addressFromPublicKey
import com.wavesplatform.wallet.v1.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.model.local.WalletItem
import com.wavesplatform.wallet.v2.data.model.userdb.AddressBookUser
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import java.util.*
import javax.inject.Inject

@InjectViewState
class ChooseAccountPresenter @Inject constructor() : BasePresenter<ChooseAccountView>() {
    fun getAddresses() {
        val list = arrayListOf<AddressBookUser>()
        val guids = prefsUtil.getGlobalValueList(
                EnvironmentManager.name + PrefsUtil.LIST_WALLET_GUIDS)
        val wallets = ArrayList<WalletItem>()
        for (i in guids.indices) {
            val pubKey = prefsUtil.getGlobalValue(guids[i] + PrefsUtil.KEY_PUB_KEY, "")
            val name = prefsUtil.getGlobalValue(guids[i] + PrefsUtil.KEY_WALLET_NAME, "")
            val address = addressFromPublicKey(pubKey)
            wallets.add(WalletItem(guids[i], name, address, pubKey))
            list.add(AddressBookUser(address, name))
        }
        viewState.afterSuccessGetAddress(list)
    }
}
