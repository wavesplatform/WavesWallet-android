package com.wavesplatform.wallet.v2.ui.home.dex.markets

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.data.model.remote.response.Market
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import javax.inject.Inject

@InjectViewState
class DexMarketsPresenter @Inject constructor() : BasePresenter<DexMarketsView>() {
    fun getMarkets(){
        val list = arrayListOf<Market>( Market(amountAssetName = "Waves", priceAssetName = "BTC", amountAsset = "WAVES", priceAsset = "96AFUzFKebbwmJulY6evx9GrfYBkmn8LcUL0"),
                Market(amountAssetName = "Waves", priceAssetName = "BTC", amountAsset = "WAVES", priceAsset = "96AFUzFKebbwmJulY6evx9GrfYBkmn8LcUL0"),
                Market(amountAssetName = "Waves", priceAssetName = "BTC", amountAsset = "WAVES", priceAsset = "96AFUzFKebbwmJulY6evx9GrfYBkmn8LcUL0"),
                Market(amountAssetName = "Waves", priceAssetName = "BTC", amountAsset = "WAVES", priceAsset = "96AFUzFKebbwmJulY6evx9GrfYBkmn8LcUL0"),
                Market(amountAssetName = "Waves", priceAssetName = "BTC", amountAsset = "WAVES", priceAsset = "96AFUzFKebbwmJulY6evx9GrfYBkmn8LcUL0"),
                Market(amountAssetName = "Waves", priceAssetName = "BTC", amountAsset = "WAVES", priceAsset = "96AFUzFKebbwmJulY6evx9GrfYBkmn8LcUL0"),
                Market(amountAssetName = "please search me", priceAssetName = "yeah", amountAsset = "great", priceAsset = "search")
                )
        viewState.afterSuccessGetMarkets(list)
    }
}
