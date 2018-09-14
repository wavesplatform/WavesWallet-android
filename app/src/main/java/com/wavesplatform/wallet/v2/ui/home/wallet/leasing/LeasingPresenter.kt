package com.wavesplatform.wallet.v2.ui.home.wallet.leasing

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v1.data.rxjava.RxUtil
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.data.model.remote.response.Transaction
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.sumByLong
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import javax.inject.Inject

@InjectViewState
class LeasingPresenter @Inject constructor() : BasePresenter<LeasingView>() {

    fun getActiveLeasing() {
        addSubscription(Observable.zip(nodeDataManager.loadWavesBalance(), nodeDataManager.activeLeasing(),
                BiFunction { t1: AssetBalance, t2: List<Transaction> ->
                    return@BiFunction Pair(t1, t2)
                })
                .compose(RxUtil.applySchedulersToObservable())
                .subscribe {
                    val leasedSum = it.second.sumByLong { it.amount }
                    viewState.showBalances(it.first,
                            leasedSum, it.first.balance?.minus(leasedSum))
                    viewState.showActiveLeasingTransaction(it.second)
                })
    }

}
