package com.wavesplatform.wallet.v2.ui.home.profile.address_book.edit

import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView
import com.wavesplatform.wallet.v2.data.model.db.AddressBookUserDb

interface EditAddressView : BaseMvpView {
    fun successEditAddress(addressBookUser: AddressBookUserDb?)
    fun successDeleteAddress()
}
