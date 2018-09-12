package com.wavesplatform.wallet.v2.ui.auth.passcode.enter

import android.util.Log
import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.BlockchainApplication
import com.wavesplatform.wallet.v1.data.auth.IncorrectPinException
import com.wavesplatform.wallet.v2.ui.auth.passcode.create.CreatePassCodeActivity
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import javax.inject.Inject

@InjectViewState
class EnterPassCodePresenter @Inject constructor() : BasePresenter<EnterPasscodeView>() {

    val passCode: String = ""
    lateinit var step: CreatePassCodeActivity.CreatePassCodeStep

    fun validate(guid: String, passCode: String) {
        BlockchainApplication
                .getAccessManager()
                .validatePassCodeObservable(guid, passCode)
                .subscribe({ password ->
                    BlockchainApplication.getAccessManager().resetPassCodeInputFails()
                    viewState.onSuccessValidatePassCode(password, passCode)
                }, { error ->
                    if (error !is IncorrectPinException) {
                        Log.e(javaClass.simpleName, "Failed to validate pin", error)
                    } else {
                        BlockchainApplication.getAccessManager().incrementPassCodeInputFails()
                    }
                    viewState.onFailValidatePassCode(overMaxWrongPassCodes(), error.message)
                })
    }

    private fun overMaxWrongPassCodes(): Boolean {
        return BlockchainApplication.getAccessManager()
                .getPassCodeInputFails() >= MAX_AVAILABLE_TIMES
    }

    companion object {
        const val MAX_AVAILABLE_TIMES = 5
    }
}
