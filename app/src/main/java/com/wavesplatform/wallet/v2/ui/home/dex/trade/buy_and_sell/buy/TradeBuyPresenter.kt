package com.wavesplatform.wallet.v2.ui.home.dex.trade.buy_and_sell.buy

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import pers.victor.ext.app
import javax.inject.Inject

@InjectViewState
class TradeBuyPresenter @Inject constructor() : BasePresenter<TradeBuyView>() {
    var selectedExpiration = 5
    var newSelectedExpiration = 5
    val expirationList = arrayOf(app.getString(R.string.buy_5_min), app.getString(R.string.buy_30_min), app.getString(R.string.buy_1_hour),
            app.getString(R.string.buy_1_day), app.getString(R.string.buy_1_week), app.getString(R.string.buy_30_days))
}
