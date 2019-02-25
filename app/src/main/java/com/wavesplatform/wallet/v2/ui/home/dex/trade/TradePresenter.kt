package com.wavesplatform.wallet.v2.ui.home.dex.trade

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.data.model.local.WatchMarket
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import javax.inject.Inject

@InjectViewState
class TradePresenter @Inject constructor() : BasePresenter<TradeView>() {
    var watchMarket: WatchMarket? = null
}
