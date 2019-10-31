/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.auth.passcode.enter

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v2.data.manager.PinStoreService
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import timber.log.Timber
import javax.inject.Inject

@InjectViewState
class EnterPassCodePresenter @Inject constructor() : BasePresenter<EnterPasscodeView>() {

    fun validate(guid: String, passCode: String) {
        addSubscription(App.accessManager
                .validatePassCodeObservable(guid, passCode)
                .subscribe({ password ->
                    App.accessManager.resetPassCodeInputFails(guid)
                    viewState.onSuccessValidatePassCode(password, passCode)
                }, { error ->
                    if (error !is PinStoreService.IncorrectPinException) {
                        Timber.e(error, "Failed to validate pin")
                    } else {
                        App.accessManager.incrementPassCodeInputFails(guid)
                    }
                    viewState.onFailValidatePassCode(overMaxWrongPassCodes(guid), error.message)
                }))
    }

    companion object {
        private const val MAX_AVAILABLE_TIMES = 5

        fun overMaxWrongPassCodes(guid: String): Boolean {
            return App.accessManager.getPassCodeInputFails(guid) >= MAX_AVAILABLE_TIMES
        }
    }
}
