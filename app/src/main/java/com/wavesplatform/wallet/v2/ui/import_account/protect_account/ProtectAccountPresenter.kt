package com.wavesplatform.wallet.v2.ui.import_account.protect_account

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
