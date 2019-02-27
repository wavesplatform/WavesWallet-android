package com.wavesplatform.wallet.v2.ui.home.wallet.leasing.cancel.confirmation

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v2.data.model.remote.request.CancelLeasingRequest
import com.wavesplatform.wallet.v2.data.model.remote.response.GlobalTransactionCommission
import com.wavesplatform.wallet.v2.data.model.remote.response.ScriptInfo
import com.wavesplatform.wallet.v2.data.model.remote.response.Transaction
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import com.wavesplatform.wallet.v2.util.TransactionUtil
import com.wavesplatform.wallet.v2.util.errorBody
import com.wavesplatform.wallet.v2.util.isSmartError
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import javax.inject.Inject

@InjectViewState
class ConfirmationCancelLeasingPresenter @Inject constructor() : BasePresenter<ConfirmationCancelLeasingView>() {

    var address: String = ""
    var amount: String = ""
    var cancelLeasingRequest: CancelLeasingRequest = CancelLeasingRequest()
    var transactionId: String = ""
    var fee = 0L

    fun cancelLeasing() {
        cancelLeasingRequest.leaseId = transactionId
        cancelLeasingRequest.fee = fee
        addSubscription(nodeDataManager.cancelLeasing(cancelLeasingRequest)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({
                    viewState.successCancelLeasing()
                    viewState.showProgressBar(false)
                }, {
                    viewState.showProgressBar(false)
                    it.printStackTrace()

                    if (it.errorBody()?.isSmartError() == true) {
                        viewState.failedCancelLeasingCauseSmart()
                    } else {
                        viewState.failedCancelLeasing(it.errorBody()?.message)
                    }
                }))
    }

    fun loadCommission() {
        viewState.showCommissionLoading()
        fee = 0L
        addSubscription(Observable.zip(
                githubDataManager.getGlobalCommission(),
                nodeDataManager.scriptAddressInfo(App.getAccessManager().getWallet()?.address!!),
                BiFunction { t1: GlobalTransactionCommission,
                             t2: ScriptInfo ->
                    return@BiFunction Pair(t1, t2)
                })
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({ triple ->
                    val commission = triple.first
                    val scriptInfo = triple.second
                    val params = GlobalTransactionCommission.Params()
                    params.transactionType = Transaction.LEASE_CANCEL
                    params.smartAccount = scriptInfo.extraFee != 0L
                    fee = TransactionUtil.countCommission(commission, params)
                    viewState.showCommissionSuccess(fee)
                }, {
                    it.printStackTrace()
                    fee = 0L
                    viewState.showCommissionError()
                }))
    }
}
