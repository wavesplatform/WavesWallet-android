package com.wavesplatform.wallet.v2.ui.home.profile.address_book.edit

import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookUser

interface EditAddressView : BaseMvpView {
    fun successEditAddress(addressBookUser: AddressBookUser?)
    fun successDeleteAddress()
}
