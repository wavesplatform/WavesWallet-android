package com.wavesplatform.wallet.v2.ui.home.dex.trade.last_trades

import android.util.Log
import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.data.model.local.WatchMarket
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import javax.inject.Inject

@InjectViewState
class TradeLastTradesPresenter @Inject constructor() : BasePresenter<TradeLastTradesView>() {
    var watchMarket: WatchMarket? = null

    fun loadLastTrades() {
        addSubscription(apiDataManager.loadLastTradesByPair(watchMarket)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({
                    Log.d("test","test")
//                    val sortedByTimestamp = it.sortedByDescending { it.timestamp }
//                    viewState.afterSuccessLoadLastTrades(sortedByTimestamp)
                }, {
                    it.printStackTrace()
//                    viewState.afterFailedLoadLastTrades()
                }))
    }

}
