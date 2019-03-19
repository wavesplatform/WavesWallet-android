package com.wavesplatform.wallet.v2.ui.home.profile.address_book.edit

import com.wavesplatform.wallet.v2.data.model.userdb.AddressBookUser
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface EditAddressView : BaseMvpView {
    fun successEditAddress(addressBookUser: AddressBookUser?)
    fun successDeleteAddress()
}
