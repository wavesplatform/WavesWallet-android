/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.profile.address_book.add

import com.wavesplatform.wallet.v2.data.model.db.userdb.AddressBookUserDb
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface AddAddressView : BaseMvpView {
    fun successSaveAddress(addressBookUser: AddressBookUserDb)
}
