package com.wavesplatform.wallet.v2.ui.home.profile.addresses.alias

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryAllAsSingle
import com.wavesplatform.sdk.net.model.response.Alias
import com.wavesplatform.sdk.net.model.response.GlobalTransactionCommission
import com.wavesplatform.sdk.net.model.response.ScriptInfo
import com.wavesplatform.sdk.net.model.response.Transaction
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.sdk.utils.RxUtil
import com.wavesplatform.sdk.utils.TransactionUtil
import com.wavesplatform.wallet.v2.data.model.db.AliasDb
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import pyxis.uzuki.live.richutilskt.utils.runAsync
import pyxis.uzuki.live.richutilskt.utils.runOnUiThread
import javax.inject.Inject

@InjectViewState
class AliasPresenter @Inject constructor() : BasePresenter<AliasView>() {

    var fee = 0L

    fun loadAliases(callback: (List<Alias>) -> Unit) {
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
                githubDataManager.getGlobalCommission(),
                nodeDataManager.scriptAddressInfo(),
                BiFunction { t1: GlobalTransactionCommission,
                             t2: ScriptInfo ->
                    return@BiFunction Pair(t1, t2)
                })
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({ pair ->
                    val commission = pair.first
                    val scriptInfo = pair.second
                    val params = GlobalTransactionCommission.Params()
                    params.transactionType = Transaction.CREATE_ALIAS
                    params.smartAccount = scriptInfo.extraFee != 0L
                    fee = TransactionUtil.countCommission(commission, params)
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
