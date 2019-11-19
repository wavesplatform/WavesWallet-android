/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.profile.addresses.alias

import moxy.InjectViewState
import com.vicpin.krealmextensions.queryAllAsSingle
import com.wavesplatform.sdk.model.request.node.BaseTransaction
import com.wavesplatform.sdk.model.response.node.transaction.AliasTransactionResponse
import com.wavesplatform.wallet.v2.data.model.service.configs.GlobalTransactionCommissionResponse
import com.wavesplatform.sdk.model.response.node.ScriptInfoResponse
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.sdk.utils.RxUtil
import com.wavesplatform.wallet.v2.data.model.db.AliasDb
import com.wavesplatform.wallet.v2.util.TransactionCommissionUtil
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import pyxis.uzuki.live.richutilskt.utils.runAsync
import pyxis.uzuki.live.richutilskt.utils.runOnUiThread
import javax.inject.Inject

@InjectViewState
class AliasPresenter @Inject constructor() : BasePresenter<AliasView>() {

    var fee = 0L

    fun loadAliases(callback: (List<AliasTransactionResponse>) -> Unit) {
        runAsync {
            addSubscription(
                    queryAllAsSingle<AliasDb>().toObservable()
                            .compose(RxUtil.applyObservableDefaultSchedulers())
                            .subscribe { aliases ->
                                val ownAliases = aliases.filter { it.own }.toMutableList()
                                runOnUiThread { callback.invoke(AliasDb.convertFromDb(ownAliases)) }
                            })
        }
    }

    fun loadCommission(callback: OnCommissionGetListener) {
        callback.showCommissionLoading()
        fee = 0L
        addSubscription(Observable.zip(
                githubServiceManager.getGlobalCommission(),
                nodeServiceManager.scriptAddressInfo(),
                BiFunction { t1: GlobalTransactionCommissionResponse,
                             t2: ScriptInfoResponse ->
                    return@BiFunction Pair(t1, t2)
                })
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({ pair ->
                    val commission = pair.first
                    val scriptInfo = pair.second
                    val params = GlobalTransactionCommissionResponse.ParamsResponse()
                    params.transactionType = BaseTransaction.CREATE_ALIAS
                    params.smartAccount = scriptInfo.extraFee != 0L
                    fee = TransactionCommissionUtil.countCommission(commission, params)
                    callback.showCommissionSuccess(fee)
                }, {
                    it.printStackTrace()
                    fee = 0L
                    callback.showCommissionError()
                }))
    }

    interface OnCommissionGetListener {
        fun showCommissionLoading()
        fun showCommissionSuccess(unscaledAmount: Long)
        fun showCommissionError()
    }
}
