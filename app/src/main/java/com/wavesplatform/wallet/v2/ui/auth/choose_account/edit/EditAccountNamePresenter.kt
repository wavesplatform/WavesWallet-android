/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.auth.choose_account.edit

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.data.model.userdb.AddressBookUser
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import javax.inject.Inject

@InjectViewState
class EditAccountNamePresenter @Inject constructor() : BasePresenter<EditAccountNameView>() {

    var account: AddressBookUser? = null
    var accountNameFieldValid = false
}
