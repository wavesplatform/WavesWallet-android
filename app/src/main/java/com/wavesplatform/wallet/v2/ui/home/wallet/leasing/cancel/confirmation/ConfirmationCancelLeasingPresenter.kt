package com.wavesplatform.wallet.v2.ui.home.wallet.leasing.cancel.confirmation

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.data.model.remote.request.CancelLeasingRequest
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import com.wavesplatform.wallet.v2.util.errorBody
import com.wavesplatform.wallet.v2.util.isSmartError
import javax.inject.Inject

@InjectViewState
class ConfirmationCancelLeasingPresenter @Inject constructor() : BasePresenter<ConfirmationCancelLeasingView>() {

    var address: String = ""
    var amount: String = ""
    var cancelLeasingRequest: CancelLeasingRequest = CancelLeasingRequest()
    var transactionId: String = ""

    fun cancelLeasing() {
        cancelLeasingRequest.txId = transactionId
        addSubscription(nodeDataManager.cancelLeasing(cancelLeasingRequest)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({
                    viewState.successCancelLeasing()
                    viewState.showProgressBar(false)
                }, {
                    viewState.failedCancelLeasing()
                    viewState.showProgressBar(false)
                    it.printStackTrace()

                    if (it.errorBody()?.isSmartError() == true) {
                        viewState.failedCancelLeasingCauseSmart()
                    }
                }))
    }
}
