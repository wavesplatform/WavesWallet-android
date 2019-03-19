package com.wavesplatform.wallet.v2.ui.auth.choose_account

import com.wavesplatform.wallet.v2.data.model.userdb.AddressBookUser
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface ChooseAccountView : BaseMvpView {
    fun afterSuccessGetAddress(list: ArrayList<AddressBookUser>)
}
