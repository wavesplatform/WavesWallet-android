package com.wavesplatform.wallet.v2.ui.auth.choose_account

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v1.ui.auth.EnvironmentManager
import com.wavesplatform.wallet.v1.ui.auth.WalletItem
import com.wavesplatform.wallet.v1.util.AddressUtil
import com.wavesplatform.wallet.v1.util.PrefsUtil
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookUser
import java.util.ArrayList
import javax.inject.Inject

@InjectViewState
class ChooseAccountPresenter @Inject constructor() : BasePresenter<ChooseAccountView>() {
    fun getAddresses() {
        val list = arrayListOf<AddressBookUser>()
        val env = EnvironmentManager.get().current().getName()
        val guids = prefsUtil.getGlobalValueList(env + PrefsUtil.LIST_WALLET_GUIDS)
        val wallets = ArrayList<WalletItem>()
        for (i in guids.indices) {
            val pubKey = prefsUtil.getGlobalValue(guids[i] + PrefsUtil.KEY_PUB_KEY, "")
            val name = prefsUtil.getGlobalValue(guids[i] + PrefsUtil.KEY_WALLET_NAME, "")
            val address = AddressUtil.addressFromPublicKey(pubKey)
            wallets.add(WalletItem(guids[i], name, address, pubKey))
            list.add(AddressBookUser(address, name))
        }
        viewState.afterSuccessGetAddress(list)
    }
}
