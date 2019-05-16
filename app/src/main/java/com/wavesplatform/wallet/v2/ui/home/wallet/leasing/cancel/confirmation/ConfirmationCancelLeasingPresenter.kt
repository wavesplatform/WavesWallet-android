/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet.leasing.cancel.confirmation

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.sdk.net.model.request.CancelLeasingRequest
import com.wavesplatform.sdk.net.model.response.GlobalTransactionCommissionResponse
import com.wavesplatform.sdk.net.model.response.ScriptInfoResponse
import com.wavesplatform.sdk.net.model.response.TransactionResponse
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.sdk.utils.RxUtil
import com.wavesplatform.sdk.utils.TransactionUtil
import com.wavesplatform.sdk.utils.isSmartError
import com.wavesplatform.wallet.v2.util.errorBody
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

    var success = false

    fun cancelLeasing() {
        cancelLeasingRequest.leaseId = transactionId
        cancelLeasingRequest.fee = fee
        addSubscription(nodeDataManager.cancelLeasing(cancelLeasingRequest)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({
                    success = true
                    viewState.successCancelLeasing()
                    viewState.showProgressBar(false)
                }, {
                    viewState.showProgressBar(false)
                    it.printStackTrace()

                    it.errorBody()?.let { error ->
                        if (error.isSmartError()) {
                            viewState.failedCancelLeasingCauseSmart()
                        } else {
                            viewState.failedCancelLeasing(error.message)
                        }
                    }

                }))
    }

    fun loadCommission() {
        viewState.showCommissionLoading()
        fee = 0L
        addSubscription(Observable.zip(
                githubDataManager.getGlobalCommission(),
                nodeDataManager.scriptAddressInfo(),
                BiFunction { t1: GlobalTransactionCommissionResponse,
                             t2: ScriptInfoResponse ->
                    return@BiFunction Pair(t1, t2)
                })
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({ triple ->
                    val commission = triple.first
                    val scriptInfo = triple.second
                    val params = GlobalTransactionCommissionResponse.ParamsResponse()
                    params.transactionType = TransactionResponse.LEASE_CANCEL
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
