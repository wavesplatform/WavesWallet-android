package com.wavesplatform.wallet.v2.ui.home.dex.trade.buy_and_sell

import com.arellomobile.mvp.InjectViewState
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.wavesplatform.wallet.v2.data.model.local.BuySellData
import com.wavesplatform.wallet.v2.data.model.local.LastPriceItem
import com.wavesplatform.wallet.v2.data.model.local.WatchMarket
import com.wavesplatform.wallet.v2.data.model.remote.response.LastTrade
import com.wavesplatform.wallet.v2.data.model.remote.response.OrderBook
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.ui.home.dex.trade.orderbook.TradeOrderBookView
import com.wavesplatform.wallet.v2.util.RxUtil
import com.wavesplatform.wallet.v2.util.notNull
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import pyxis.uzuki.live.richutilskt.utils.runOnUiThread
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@InjectViewState
class TradeBuyAndSellPresenter @Inject constructor() : BasePresenter<TradeBuyAndSellView>() {
     var data: BuySellData? = BuySellData()

}
