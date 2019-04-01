/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.auth.import_account.protect_account

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import javax.inject.Inject

@InjectViewState
class ProtectAccountPresenter @Inject constructor() : BasePresenter<ProtectAccountView>() {
    var accountNameFieldValid = false
    var createPasswrodFieldValid = false
    var confirmPasswordFieldValid = false

    fun isAllFieldsValid(): Boolean {
        return accountNameFieldValid && createPasswrodFieldValid && confirmPasswordFieldValid
    }
}
