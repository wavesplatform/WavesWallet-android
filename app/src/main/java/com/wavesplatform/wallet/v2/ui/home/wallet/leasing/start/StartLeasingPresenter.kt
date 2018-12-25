package com.wavesplatform.wallet.v2.ui.home.wallet.leasing.start

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import javax.inject.Inject

@InjectViewState
class StartLeasingPresenter @Inject constructor() : BasePresenter<StartLeasingView>() {
    var nodeAddressValidation = false
    var amountValidation = false

    var recipientIsAlias = false

    var wavesAssetBalance: Long = 0

    fun isAllFieldsValid(): Boolean {
        return nodeAddressValidation && amountValidation
    }

}
