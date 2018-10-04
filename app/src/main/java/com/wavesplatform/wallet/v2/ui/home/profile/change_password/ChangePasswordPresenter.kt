package com.wavesplatform.wallet.v2.ui.home.profile.change_password

import android.util.Log
import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.R.id.edit_confirm_password
import com.wavesplatform.wallet.R.id.edit_old_password
import com.wavesplatform.wallet.v1.data.auth.WavesWallet
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.showError
import kotlinx.android.synthetic.main.activity_change_password.*
import pers.victor.ext.ActivityMgr.finish
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

    fun writePassword(oldPassword: String, newPassword: String) {
        val guid = App.getAccessManager().getLoggedInGuid()
        val oldWallet = WavesWallet(
                App.getAccessManager().getCurrentWavesWalletEncryptedData(),
                oldPassword
        )
        val newWallet = WavesWallet(oldWallet.seed)

        App.getAccessManager().storePassword(
                guid, newWallet.publicKeyStr,
                newWallet.getEncryptedData(newPassword))

        addSubscription(App.getAccessManager()
                .writePassCodeObservable(guid, newPassword, passCode)
                .subscribe({
                    viewState.afterSuccessChangePassword()
                }, { throwable ->
                    Log.e("CreatePassCodeActivity", throwable.message)
                }))
    }
}
