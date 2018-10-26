package com.wavesplatform.wallet.v2.ui.home.dex

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryAllAsSingle
import com.wavesplatform.wallet.v2.data.model.local.WatchMarket
import com.wavesplatform.wallet.v2.data.model.remote.response.MarketResponse
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import pyxis.uzuki.live.richutilskt.utils.runAsync
import javax.inject.Inject

@InjectViewState
class DexPresenter @Inject constructor() : BasePresenter<DexView>() {

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

}
