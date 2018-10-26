package com.wavesplatform.wallet.v2.ui.home.dex

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.data.model.remote.response.MarketResponse
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import javax.inject.Inject

@InjectViewState
class DexPresenter @Inject constructor() :BasePresenter<DexView>(){

    fun loadActiveMarkets() {
        val list = arrayListOf<MarketResponse>( MarketResponse(amountAssetName = "Waves", priceAssetName = "BTC", amountAsset = "WAVES", priceAsset = "96AFUzFKebbwmJulY6evx9GrfYBkmn8LcUL0"),
                MarketResponse(amountAssetName = "Waves", priceAssetName = "BTC", amountAsset = "WAVES", priceAsset = "96AFUzFKebbwmJulY6evx9GrfYBkmn8LcUL0"),
                MarketResponse(amountAssetName = "Waves", priceAssetName = "BTC", amountAsset = "WAVES", priceAsset = "96AFUzFKebbwmJulY6evx9GrfYBkmn8LcUL0"),
                MarketResponse(amountAssetName = "Waves", priceAssetName = "BTC", amountAsset = "WAVES", priceAsset = "96AFUzFKebbwmJulY6evx9GrfYBkmn8LcUL0"),
                MarketResponse(amountAssetName = "Waves", priceAssetName = "BTC", amountAsset = "WAVES", priceAsset = "96AFUzFKebbwmJulY6evx9GrfYBkmn8LcUL0"),
                MarketResponse(amountAssetName = "Waves", priceAssetName = "BTC", amountAsset = "WAVES", priceAsset = "96AFUzFKebbwmJulY6evx9GrfYBkmn8LcUL0"),
                MarketResponse(amountAssetName = "Waves", priceAssetName = "BTC", amountAsset = "WAVES", priceAsset = "96AFUzFKebbwmJulY6evx9GrfYBkmn8LcUL0"),
                MarketResponse(amountAssetName = "Waves", priceAssetName = "BTC", amountAsset = "WAVES", priceAsset = "96AFUzFKebbwmJulY6evx9GrfYBkmn8LcUL0"),
                MarketResponse(amountAssetName = "Waves", priceAssetName = "BTC", amountAsset = "WAVES", priceAsset = "96AFUzFKebbwmJulY6evx9GrfYBkmn8LcUL0"),
                MarketResponse(amountAssetName = "Waves", priceAssetName = "BTC", amountAsset = "WAVES", priceAsset = "96AFUzFKebbwmJulY6evx9GrfYBkmn8LcUL0"),
                MarketResponse(amountAssetName = "Waves", priceAssetName = "BTC", amountAsset = "WAVES", priceAsset = "96AFUzFKebbwmJulY6evx9GrfYBkmn8LcUL0"),
                MarketResponse(amountAssetName = "Waves", priceAssetName = "BTC", amountAsset = "WAVES", priceAsset = "96AFUzFKebbwmJulY6evx9GrfYBkmn8LcUL0"),
                MarketResponse(amountAssetName = "Waves", priceAssetName = "BTC", amountAsset = "WAVES", priceAsset = "96AFUzFKebbwmJulY6evx9GrfYBkmn8LcUL0"),
                MarketResponse(amountAssetName = "Waves", priceAssetName = "BTC", amountAsset = "WAVES", priceAsset = "96AFUzFKebbwmJulY6evx9GrfYBkmn8LcUL0"),
                MarketResponse(amountAssetName = "please search me", priceAssetName = "yeah", amountAsset = "great", priceAsset = "search")
        )
        viewState.afterSuccessLoadMarkets(list)
    }

}
