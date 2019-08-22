/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet.leasing.start

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.sdk.model.request.node.BaseTransaction
import com.wavesplatform.wallet.v2.data.model.service.configs.GlobalTransactionCommissionResponse
import com.wavesplatform.sdk.model.response.node.ScriptInfoResponse
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.sdk.utils.RxUtil
import com.wavesplatform.wallet.v2.util.TransactionCommissionUtil
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import javax.inject.Inject

@InjectViewState
class StartLeasingPresenter @Inject constructor() : BasePresenter<StartLeasingView>() {

    var nodeAddressValidation = false
    var amountValidation = false
    var recipientIsAlias = false
    var wavesAssetBalance: Long = 0
    var fee = 0L

    fun isAllFieldsValid(): Boolean {
        return nodeAddressValidation && amountValidation && fee > 0L
    }

    fun loadCommission(wavesBalance: Long) {
        viewState.showCommissionLoading()
        fee = 0L
        addSubscription(Observable.zip(
                githubServiceManager.getGlobalCommission(),
                nodeServiceManager.scriptAddressInfo(),
                BiFunction { t1: GlobalTransactionCommissionResponse,
                             t2: ScriptInfoResponse ->
                    return@BiFunction Pair(t1, t2)
                })
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({ triple ->
                    val commission = triple.first
                    val scriptInfo = triple.second
                    val params = GlobalTransactionCommissionResponse.ParamsResponse()
                    params.transactionType = BaseTransaction.CREATE_LEASING
                    params.smartAccount = scriptInfo.extraFee != 0L
                    fee = TransactionCommissionUtil.countCommission(commission, params)
                    viewState.showCommissionSuccess(fee)
                    viewState.afterSuccessLoadWavesBalance(wavesBalance)
                }, {
                    it.printStackTrace()
                    fee = 0L
                    viewState.showCommissionError()
                }))
    }
}
