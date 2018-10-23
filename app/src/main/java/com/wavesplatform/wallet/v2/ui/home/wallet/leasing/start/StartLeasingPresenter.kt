package com.wavesplatform.wallet.v2.ui.home.wallet.leasing.start

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import javax.inject.Inject

@InjectViewState
class StartLeasingPresenter @Inject constructor() : BasePresenter<StartLeasingView>() {
    var nodeAddressValidation = false
    var amountValidation = false

    var recipientIsAlias = false

    var wavesAsset: AssetBalance? = null


    fun isAllFieldsValid(): Boolean {
        return nodeAddressValidation && amountValidation
    }

}
