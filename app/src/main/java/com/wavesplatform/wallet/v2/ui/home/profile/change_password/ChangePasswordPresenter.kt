/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.profile.change_password

import android.util.Log
import moxy.InjectViewState
import com.wavesplatform.wallet.v2.util.WavesWallet
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import javax.inject.Inject

@InjectViewState
class ChangePasswordPresenter @Inject constructor() : BasePresenter<ChangePasswordView>() {
    var oldPasswordFieldValid = false
    var newPasswordFieldValid = false
    var confirmPasswordFieldValid = false
    var passCode: String = ""

    fun isAllFieldsValid(): Boolean {
        return oldPasswordFieldValid && newPasswordFieldValid && confirmPasswordFieldValid
    }

    fun writePassword(oldPassword: String?, newPassword: String?) {
        val guid = App.accessManager.getLoggedInGuid()
        val oldWallet = WavesWallet(
                App.accessManager.getCurrentWavesWalletEncryptedData(),
                oldPassword
        )
        val newWallet = WavesWallet(oldWallet.seed)

        App.accessManager.storePassword(
                guid, newWallet.publicKeyStr,
                newWallet.getEncryptedData(newPassword))

        addSubscription(App.accessManager
                .writePassCodeObservable(guid, newPassword, passCode)
                .subscribe({
                    viewState.afterSuccessChangePassword()
                }, { throwable ->
                    Log.e("CreatePassCodeActivity", throwable.message)
                }))
    }
}
