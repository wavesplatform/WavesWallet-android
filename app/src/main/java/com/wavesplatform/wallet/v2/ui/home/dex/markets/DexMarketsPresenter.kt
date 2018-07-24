package com.wavesplatform.wallet.v2.ui.home.dex.markets

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.data.model.remote.response.Market
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import javax.inject.Inject

@InjectViewState
class DexMarketsPresenter @Inject constructor() : BasePresenter<DexMarketsView>() {
    fun getMarkets(){
        val list = arrayListOf<Market>(Market(amountAssetName = "Waves", priceAssetName = "BTC"),
                Market(amountAssetName = "Waves", priceAssetName = "BTC"),
                Market(amountAssetName = "Waves", priceAssetName = "BTC"),
                Market(amountAssetName = "Waves", priceAssetName = "BTC"),
                Market(amountAssetName = "Waves", priceAssetName = "BTC"),
                Market(amountAssetName = "Waves", priceAssetName = "BTC"),
                Market(amountAssetName = "Waves", priceAssetName = "BTC"),
                Market(amountAssetName = "Waves", priceAssetName = "BTC"),
                Market(amountAssetName = "Test", priceAssetName = "Test1"),Market(amountAssetName = "Waves", priceAssetName = "BTC"),
                Market(amountAssetName = "Waves", priceAssetName = "BTC")
                )
        viewState.afterSuccessGetMarkets(list)
    }
}
