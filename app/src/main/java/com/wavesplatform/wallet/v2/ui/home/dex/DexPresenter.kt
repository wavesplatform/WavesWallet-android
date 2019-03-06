package com.wavesplatform.wallet.v2.ui.home.dex

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryAllAsSingle
import com.wavesplatform.wallet.v2.data.model.local.WatchMarket
import com.wavesplatform.wallet.v2.data.model.remote.response.MarketResponse
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import io.reactivex.disposables.CompositeDisposable
import pyxis.uzuki.live.richutilskt.utils.runAsync
import javax.inject.Inject

@InjectViewState
class DexPresenter @Inject constructor() : BasePresenter<DexView>() {

    var pairSubscriptions = CompositeDisposable()
    var hideShadow: Boolean = true

    fun loadActiveMarkets() {
        runAsync {
            addSubscription(queryAllAsSingle<MarketResponse>().toObservable()
                    .compose(RxUtil.applyObservableDefaultSchedulers())
                    .subscribe({
                        val markets = it.sortedBy { it.position }.mapTo(ArrayList()) {
                            return@mapTo WatchMarket(it)
                        }
                        viewState.afterSuccessLoadMarkets(markets)
                    }, {
                        it.printStackTrace()
                    }))
        }
    }

    fun loadDexPairInfo(watchMarket: WatchMarket, index: Int) {
        pairSubscriptions.add(apiDataManager.loadDexPairInfo(watchMarket)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({
                    viewState.afterSuccessLoadPairInfo(it, index)
                }, {
                    viewState.afterFailedLoadPairInfo()
                    it.printStackTrace()
                }))
    }

    fun clearOldPairsSubscriptions() {
        pairSubscriptions.clear()
    }
}
