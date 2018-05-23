package com.wavesplatform.wallet.v2.ui.new_account

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.ui.new_account.NewAccountView
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import javax.inject.Inject

@InjectViewState
class NewAccountPresenter @Inject constructor() : BasePresenter<NewAccountView>() {
    var accountNameFieldValid = false
    var createPasswrodFieldValid = false
    var confirmPasswordFieldValid = false

    fun isAllFieldsValid(): Boolean {
        return accountNameFieldValid && createPasswrodFieldValid && confirmPasswordFieldValid
    }
}
