package com.wavesplatform.wallet.v2.ui.auth.import_account.manually

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import javax.inject.Inject

@InjectViewState
class EnterSeedManuallyPresenter @Inject constructor() : BasePresenter<EnterSeedManuallyView>() {
    var nextStepValidation = false
}
