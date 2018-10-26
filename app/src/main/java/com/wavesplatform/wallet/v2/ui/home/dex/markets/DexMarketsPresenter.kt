package com.wavesplatform.wallet.v2.ui.home.dex.markets

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.saveAll
import com.wavesplatform.wallet.v2.data.model.remote.response.MarketResponse
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import pyxis.uzuki.live.richutilskt.utils.runAsync
import javax.inject.Inject

@InjectViewState
class DexMarketsPresenter @Inject constructor() : BasePresenter<DexMarketsView>() {
    fun getMarkets() {
        runAsync {
            addSubscription(matcherDataManager.getAllMarkets()
                    .compose(RxUtil.applyObservableDefaultSchedulers())
                    .subscribe({
                        viewState.afterSuccessGetMarkets(it)
                    }, {
                        it.printStackTrace()
                    }))
        }
    }

    fun saveSelectedMarkets(data: List<MarketResponse>) {
        runAsync {
            val selectedMarkets = data.filter { it.checked }
            selectedMarkets.saveAll()
        }
    }
}
