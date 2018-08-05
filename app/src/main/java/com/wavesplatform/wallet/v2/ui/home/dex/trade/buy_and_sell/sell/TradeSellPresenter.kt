package com.wavesplatform.wallet.v2.ui.home.dex.trade.buy_and_sell.sell

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import pers.victor.ext.app
import javax.inject.Inject

@InjectViewState
class TradeSellPresenter @Inject constructor() : BasePresenter<TradeSellView>() {
    var selectedExpiration = 5
    var newSelectedExpiration = 5
    val expirationList = arrayOf(app.getString(R.string.buy_and_sell_5_min), app.getString(R.string.buy_and_sell_30_min), app.getString(R.string.buy_and_sell_1_hour),
            app.getString(R.string.buy_and_sell_1_day), app.getString(R.string.buy_and_sell_1_week), app.getString(R.string.buy_and_sell_30_days))
}
