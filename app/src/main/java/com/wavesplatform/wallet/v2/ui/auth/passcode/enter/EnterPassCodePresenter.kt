package com.wavesplatform.wallet.v2.ui.auth.passcode.enter

import android.util.Log
import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v2.data.manager.PinStoreService
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import javax.inject.Inject

@InjectViewState
class EnterPassCodePresenter @Inject constructor() : BasePresenter<EnterPasscodeView>() {

    fun validate(guid: String, passCode: String) {
        addSubscription(App.getAccessManager()
                .validatePassCodeObservable(guid, passCode)
                .subscribe({ password ->
                    App.getAccessManager().resetPassCodeInputFails(guid)
                    viewState.onSuccessValidatePassCode(password, passCode)
                }, { error ->
                    if (error !is PinStoreService.IncorrectPinException) {
                        Log.e(javaClass.simpleName, "Failed to validate pin", error)
                    } else {
                        App.getAccessManager().incrementPassCodeInputFails(guid)
                    }
                    viewState.onFailValidatePassCode(overMaxWrongPassCodes(guid), error.message)
                }))
    }


    companion object {
        private const val MAX_AVAILABLE_TIMES = 5

        fun overMaxWrongPassCodes(guid: String): Boolean {
            return App.getAccessManager().getPassCodeInputFails(guid) >= MAX_AVAILABLE_TIMES
        }
    }
}
