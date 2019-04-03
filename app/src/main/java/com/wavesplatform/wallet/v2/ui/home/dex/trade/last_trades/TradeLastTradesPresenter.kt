/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.dex.trade.last_trades

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.sdk.net.model.WatchMarket
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.sdk.utils.RxUtil
import javax.inject.Inject

@InjectViewState
class TradeLastTradesPresenter @Inject constructor() : BasePresenter<TradeLastTradesView>() {
    var watchMarket: WatchMarket? = null

    fun loadLastTrades() {
        addSubscription(apiDataManager.loadLastTradesByPair(watchMarket)
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
