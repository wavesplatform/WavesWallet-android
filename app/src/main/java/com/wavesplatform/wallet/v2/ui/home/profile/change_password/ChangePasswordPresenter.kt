package com.wavesplatform.wallet.v2.ui.home.profile.change_password

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import javax.inject.Inject

@InjectViewState
class ChangePasswordPresenter @Inject constructor() : BasePresenter<ChangePasswordView>() {
    var oldPasswordFieldValid = false
    var newPasswordFieldValid = false
    var confirmPasswordFieldValid = false

    fun isAllFieldsValid(): Boolean {
        return oldPasswordFieldValid && newPasswordFieldValid && confirmPasswordFieldValid
    }
}
