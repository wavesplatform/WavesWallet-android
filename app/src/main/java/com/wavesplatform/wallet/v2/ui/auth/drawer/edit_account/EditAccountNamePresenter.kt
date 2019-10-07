/*
 * Created by Eduard Zaydel on 2/10/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.auth.drawer.edit_account

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.data.model.db.userdb.AddressBookUserDb
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import javax.inject.Inject

@InjectViewState
class EditAccountNamePresenter @Inject constructor() : BasePresenter<EditAccountNameView>() {

    var account: AddressBookUserDb? = null
    var accountNameFieldValid = false
}
