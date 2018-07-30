package com.wavesplatform.wallet.v2.ui.home.dex.trade.last_trades

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.data.model.local.MyOrderItem
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.ui.home.history.TestObject
import java.util.*
import javax.inject.Inject

@InjectViewState
class TradeLastTradesPresenter @Inject constructor() : BasePresenter<TradeLastTradesView>() {
    fun loadLastTrades() {
        var data = ArrayList<TestObject>()
        data.add(TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), 523.061350, Random().nextDouble()))
        data.add(TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), 3.061350, Random().nextDouble()))
        data.add(TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), 0.061350, Random().nextDouble()))
        data.add(TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), 363.5061350, Random().nextDouble()))
        data.add(TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble()))
        data.add(TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble()))

        viewState.afterSuccessLoadLastTrades(data)
    }

}
