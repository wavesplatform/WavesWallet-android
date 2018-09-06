package com.wavesplatform.wallet.v2.ui.auth.passcode.enter

import android.content.Context
import android.util.Log
import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v1.data.access.AccessState
import com.wavesplatform.wallet.v1.data.auth.IncorrectPinException
import com.wavesplatform.wallet.v2.data.manager.AccessManager
import com.wavesplatform.wallet.v2.ui.auth.passcode.create.CreatePassCodeActivity
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import javax.inject.Inject

@InjectViewState
class EnterPassCodePresenter @Inject constructor() : BasePresenter<EnterPasscodeView>() {

    val passCode: String = ""
    lateinit var step: CreatePassCodeActivity.CreatePassCodeStep

    fun validate(context: Context, guid: String, passCode: String) {
        AccessManager(context).validatePassCodeObservable(guid, passCode).subscribe({ password ->
            AccessState.getInstance().removePinFails()
            viewState.onSuccessValidatePassCode(password, passCode)
        }, { error ->
            if (error !is IncorrectPinException) {
                Log.e(javaClass.simpleName, "Failed to validate pin", error)
            } else {
                AccessState.getInstance().incrementPinFails()
            }
            viewState.onFailValidatePassCode(overMaxWrongPassCodes(), error.message)
        })
    }

    private fun overMaxWrongPassCodes(): Boolean {
        return AccessState.getInstance().pinFails >= MAX_AVAILABLE_TIMES
    }

    companion object {
        const val MAX_AVAILABLE_TIMES = 5
    }
}
