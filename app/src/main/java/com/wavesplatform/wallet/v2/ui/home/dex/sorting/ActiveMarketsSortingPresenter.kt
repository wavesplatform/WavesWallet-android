package com.wavesplatform.wallet.v2.ui.home.dex.sorting

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.saveAll
import com.wavesplatform.wallet.v2.data.model.remote.response.MarketResponse
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import com.wavesplatform.wallet.v2.util.queryAllUserData
import io.reactivex.Observable
import pyxis.uzuki.live.richutilskt.utils.runAsync
import javax.inject.Inject

@InjectViewState
class ActiveMarketsSortingPresenter @Inject constructor() : BasePresenter<ActiveMarketsSortingView>() {
    var needToUpdate: Boolean = false

    fun loadMarkets() {
        runAsync {
            addSubscription(Observable.just(queryAllUserData<MarketResponse>())
                    .compose(RxUtil.applyObservableDefaultSchedulers())
                    .subscribe({
                        val markets = it.sortedBy { it.position }.toMutableList()
                        viewState.afterSuccessLoadMarkets(markets)
                    }, {
                        it.printStackTrace()
                    }))
        }
    }

    fun saveSortedPositions(data: List<MarketResponse>) {
        runAsync {
            data.forEachIndexed { index, market ->
                market.position = index
            }
            data.saveAll()
        }
    }
}
