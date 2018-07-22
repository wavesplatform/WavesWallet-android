package com.wavesplatform.wallet.v2.ui.auth.passcode.create

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import javax.inject.Inject

@InjectViewState
class CreatePasscodePresenter @Inject constructor() : BasePresenter<CreatePasscodeView>() {
    var passCode: String = ""
    lateinit var step: CreatePasscodeActivity.CreatePassCodeStep
}
