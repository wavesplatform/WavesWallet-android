package com.wavesplatform.wallet.v2.ui.auth.passcode.enter

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.ui.auth.passcode.create.CreatePasscodeActivity
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import javax.inject.Inject

@InjectViewState
class EnterPasscodePresenter @Inject constructor() : BasePresenter<EnterPasscodeView>() {
    var passCode: String = ""
    lateinit var step: CreatePasscodeActivity.CreatePassCodeStep
}
