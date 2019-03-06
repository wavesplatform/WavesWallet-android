package com.wavesplatform.wallet.v2.ui.auth.passcode.enter

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v1.data.auth.IncorrectPinException
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import timber.log.Timber
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
                    if (error !is IncorrectPinException) {
                        Timber.e(error, "Failed to validate pin")
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
