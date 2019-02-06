package com.wavesplatform.wallet.v2.ui.home.quick_action.send.fee

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v1.data.rxjava.RxUtil
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalances
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import javax.inject.Inject

@InjectViewState
class SponsoredFeeDetailsPresenter @Inject constructor() : BasePresenter<SponsoredFeeDetailsView>() {

    fun loadSponsoredAssets(listener: (MutableList<AssetBalance>) -> Unit) {
        addSubscription(Observable.zip(
                nodeDataManager.assetsBalances()
                        .flatMap { Observable.fromIterable(it.balances) }
                        .filter { it.minSponsoredAssetFee ?: 0 > 0 }
                        .toList().toObservable()
                        .map {
                            return@map it.toMutableList()
                        },
                nodeDataManager.loadWavesBalance(),
                BiFunction { assetsBalance: MutableList<AssetBalance>, wavesBalance: AssetBalance ->
                    assetsBalance.add(0, wavesBalance)
                    return@BiFunction assetsBalance
                })
                .compose(RxUtil.applySchedulersToObservable())
                .subscribe {
                    listener.invoke(it)
                })
    }

}
