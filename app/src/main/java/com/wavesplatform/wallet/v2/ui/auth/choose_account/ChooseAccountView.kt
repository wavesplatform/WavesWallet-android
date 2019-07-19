/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.auth.choose_account

import com.wavesplatform.wallet.v2.data.model.db.userdb.AddressBookUserDb
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface ChooseAccountView : BaseMvpView {
    fun afterSuccessGetAddress(list: ArrayList<AddressBookUserDb>)
}
