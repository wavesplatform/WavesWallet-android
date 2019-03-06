package com.wavesplatform.wallet.v2.ui.home.dex.trade.buy_and_sell

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.data.model.local.BuySellData
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import javax.inject.Inject

@InjectViewState
class TradeBuyAndSellPresenter @Inject constructor() : BasePresenter<TradeBuyAndSellView>() {
     var data: BuySellData? = BuySellData()
}
