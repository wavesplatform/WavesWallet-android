package com.wavesplatform.wallet.v2.ui.home.profile.address_book.add

import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView
import com.wavesplatform.wallet.v2.data.model.db.AddressBookUserDb

interface AddAddressView : BaseMvpView {
    fun successSaveAddress(addressBookUser: AddressBookUserDb)

}
