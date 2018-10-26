package com.wavesplatform.wallet.v2.ui.home.dex.sorting

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryAllAsSingle
import com.wavesplatform.wallet.v1.payload.Market
import com.wavesplatform.wallet.v2.data.model.remote.response.MarketResponse
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import pyxis.uzuki.live.richutilskt.utils.runAsync
import javax.inject.Inject

@InjectViewState
class ActiveMarketsSortingPresenter @Inject constructor() : BasePresenter<ActiveMarketsSortingView>() {
    fun loadMarkets() {
        runAsync {
            addSubscription(queryAllAsSingle<MarketResponse>().toObservable()
                    .compose(RxUtil.applyObservableDefaultSchedulers())
                    .subscribe({
                        viewState.afterSuccessLoadMarkets(it)
                    }, {
                        it.printStackTrace()
                    }))
        }
    }
}
