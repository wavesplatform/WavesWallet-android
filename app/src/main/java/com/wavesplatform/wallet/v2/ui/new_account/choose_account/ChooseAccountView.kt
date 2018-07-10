package com.wavesplatform.wallet.v2.ui.new_account.choose_account

import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressTestObject

interface ChooseAccountView :BaseMvpView{
    fun afterSuccessGetAddress(list: ArrayList<AddressTestObject>)
}
