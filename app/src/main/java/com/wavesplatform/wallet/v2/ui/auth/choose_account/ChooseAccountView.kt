package com.wavesplatform.wallet.v2.ui.auth.choose_account

import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView
import com.wavesplatform.wallet.v2.data.model.db.AddressBookUserDb

interface ChooseAccountView : BaseMvpView {
    fun afterSuccessGetAddress(list: ArrayList<AddressBookUserDb>)
}
