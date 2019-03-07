package com.wavesplatform.wallet.v2.ui.home.profile.address_book

import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface AddressBookView : BaseMvpView {
    fun afterSuccessGetAddress(list: MutableList<AddressBookUser>)
    fun afterFailedGetAddress()
}
