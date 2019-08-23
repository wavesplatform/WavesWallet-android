/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.dex.trade.last_trades

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.data.model.remote.response.WatchMarketResponse
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.sdk.utils.RxUtil
import javax.inject.Inject

@InjectViewState
class TradeLastTradesPresenter @Inject constructor() : BasePresenter<TradeLastTradesView>() {
    var watchMarket: WatchMarketResponse? = null

    fun loadLastTrades() {
        addSubscription(dataServiceManager.loadLastTradesByPair(watchMarket)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({
                    val sortedByTimestamp = it.sortedByDescending { it.timestamp }
                    viewState.afterSuccessLoadLastTrades(sortedByTimestamp)
                }, {
                    it.printStackTrace()
                    viewState.afterFailedLoadLastTrades()
                }))
    }
}
