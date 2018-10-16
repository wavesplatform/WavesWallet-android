package com.wavesplatform.wallet.v2.ui.home.history.details

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.data.model.remote.request.AliasRequest
import com.wavesplatform.wallet.v2.data.model.remote.request.CancelLeasingRequest
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.data.model.remote.response.Transaction
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import javax.inject.Inject

@InjectViewState
class HistoryDetailsPresenter @Inject constructor() : BasePresenter<HistoryDetailsView>() {
    var cancelLeasingRequest: CancelLeasingRequest = CancelLeasingRequest()


    fun cancelLeasing(txId: String, callback: (Transaction) -> Unit) {
        cancelLeasingRequest.txId = txId

        addSubscription(nodeDataManager.cancelLeasing(cancelLeasingRequest)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({
                    callback.invoke(it)
                }, {
                    viewState.showProgressBar(false)
                    it.printStackTrace()
                }))
    }

}
