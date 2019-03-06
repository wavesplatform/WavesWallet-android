package com.wavesplatform.wallet.v2.ui.home.wallet.leasing.start.confirmation

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.sdk.utils.MoneyUtil
import com.wavesplatform.sdk.model.request.CreateLeasingRequest
import com.wavesplatform.sdk.utils.isSmartError
import com.wavesplatform.sdk.utils.makeAsAlias
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import com.wavesplatform.wallet.v2.util.errorBody
import javax.inject.Inject

@InjectViewState
class ConfirmationStartLeasingPresenter @Inject constructor() : BasePresenter<ConfirmationStartLeasingView>() {

    var createLeasingRequest: CreateLeasingRequest = CreateLeasingRequest()
    var recipientIsAlias = false
    var address: String = ""
    var amount: String = ""
    var fee = 0L

    fun startLeasing() {
        if (recipientIsAlias) {
            createLeasingRequest.recipient = address.makeAsAlias()
        } else {
            createLeasingRequest.recipient = address
        }
        createLeasingRequest.amount = MoneyUtil.getUnscaledValue(amount, 8)

        addSubscription(nodeDataManager.startLeasing(createLeasingRequest, recipientIsAlias, fee)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({
                    viewState.successStartLeasing()
                    viewState.showProgressBar(false)
                }, {
                    it.printStackTrace()

                    viewState.showProgressBar(false)

                    if (it.errorBody()?.isSmartError() == true) {
                        viewState.failedStartLeasingCauseSmart()
                    } else {
                        viewState.failedStartLeasing(it.errorBody()?.message)
                    }
                }))
    }
}
