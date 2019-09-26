/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet.assets.token_burn

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.sdk.model.request.node.BaseTransaction
import com.wavesplatform.sdk.model.response.node.AssetBalanceResponse
import com.wavesplatform.sdk.model.response.node.AssetsDetailsResponse
import com.wavesplatform.sdk.model.response.node.ScriptInfoResponse
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.sdk.utils.RxUtil
import com.wavesplatform.wallet.v2.data.model.service.configs.GlobalTransactionCommissionResponse
import com.wavesplatform.wallet.v2.util.TransactionCommissionUtil
import io.reactivex.Observable
import io.reactivex.functions.Function3
import javax.inject.Inject

@InjectViewState
class TokenBurnPresenter @Inject constructor() : BasePresenter<TokenBurnView>() {
    var quantityValidation = false
    var wavesBalance: AssetBalanceResponse = AssetBalanceResponse()
    var assetBalance = AssetBalanceResponse()
    var fee = 0L

    fun isAllFieldsValid(): Boolean {
        return quantityValidation && fee > 0L
    }

    fun loadWavesBalance() {
        addSubscription(nodeServiceManager.loadWavesBalance()
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe {
                    wavesBalance = it
                })
    }

    fun loadCommission(assetId: String?) {
        viewState.showCommissionLoading()
        fee = 0L
        addSubscription(Observable.zip(
                githubServiceManager.getGlobalCommission(),
                nodeServiceManager.scriptAddressInfo(),
                nodeServiceManager.assetDetails(assetId),
                Function3 { t1: GlobalTransactionCommissionResponse,
                            t2: ScriptInfoResponse,
                            t3: AssetsDetailsResponse ->
                    return@Function3 Triple(t1, t2, t3)
                })
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({ triple ->
                    val commission = triple.first
                    val scriptInfo = triple.second
                    val assetsDetails = triple.third
                    val params = GlobalTransactionCommissionResponse.ParamsResponse()
                    params.transactionType = BaseTransaction.BURN
                    params.smartAccount = scriptInfo.extraFee != 0L
                    params.smartAsset = assetsDetails.scripted
                    fee = TransactionCommissionUtil.countCommission(commission, params)
                    viewState.showCommissionSuccess(fee)
                }, {
                    it.printStackTrace()
                    fee = 0L
                    viewState.showCommissionError()
                }))
    }
}
