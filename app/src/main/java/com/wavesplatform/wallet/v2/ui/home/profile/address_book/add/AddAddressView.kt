package com.wavesplatform.wallet.v2.ui.home.profile.address_book.add

import com.wavesplatform.wallet.v2.data.model.userdb.AddressBookUser
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface AddAddressView : BaseMvpView {
    fun successSaveAddress(addressBookUser: AddressBookUser)
}
