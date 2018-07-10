package com.wavesplatform.wallet.v2.ui.passcode.enter

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.ui.passcode.create.CreatePasscodeActivity
import com.wavesplatform.wallet.v2.ui.passcode.create.CreatePasscodeView
import javax.inject.Inject

@InjectViewState
class EnterPasscodePresenter @Inject constructor() : BasePresenter<EnterPasscodeView>() {
    var passCode: String = ""
    lateinit var step: CreatePasscodeActivity.CreatePassCodeStep
}
