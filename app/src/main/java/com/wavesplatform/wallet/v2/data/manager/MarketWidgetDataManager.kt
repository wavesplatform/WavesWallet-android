/*
 * Created by Eduard Zaydel on 8/8/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.manager

import android.content.Context
import com.wavesplatform.sdk.model.request.data.PairRequest
import com.wavesplatform.sdk.model.response.data.SearchPairResponse
import com.wavesplatform.sdk.utils.isWaves
import com.wavesplatform.sdk.utils.isWavesId
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.local.widget.MarketWidgetActiveAsset
import com.wavesplatform.wallet.v2.data.model.local.widget.MarketWidgetActiveMarket
import com.wavesplatform.wallet.v2.data.model.local.widget.MarketWidgetSettings
import com.wavesplatform.wallet.v2.util.executeInBackground
import io.reactivex.disposables.CompositeDisposable
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarketWidgetDataManager @Inject constructor() {

    @Inject
    lateinit var dataServiceManager: DataServiceManager

    private val compositeDisposable = CompositeDisposable()

    fun loadMarketsPrices(context: Context,
                          widgetId: Int,
                          successListener: () -> Unit,
                          errorListener: () -> Unit) {

        val activeAssets = withDefaultPair(MarketWidgetSettings.assetsSettings().queryAll(context, widgetId))
        val activeAssetsIds = activeAssets.map { it.amountAsset + "/" + it.priceAsset }

        compositeDisposable.add(dataServiceManager.loadPairs(PairRequest(pairs = activeAssetsIds))
                .executeInBackground()
                .map { response ->
                    return@map activeAssets
                            .mapIndexedTo(mutableListOf(), { index, marketWidgetActiveAsset ->
                                MarketWidgetActiveMarket(marketWidgetActiveAsset,
                                        response.data[index].data ?: SearchPairResponse.Pair.Data())
                            })
                }
                .map { activeMarkets ->
                    val prices = calculatePrice(activeMarkets)
                    MarketWidgetSettings.marketsSettings().saveAll(context, widgetId, prices)
                }
                .subscribe({
                    successListener.invoke()
                }, {
                    errorListener.invoke()
                    it.printStackTrace()
                }))
    }

    private fun calculatePrice(activeMarkets: MutableList<MarketWidgetActiveMarket>): MutableList<MarketWidgetActiveMarket.UI> {
        val wavesUSDAsset = activeMarkets.first { it.assetInfo.id == Constants.Fiat.USD_ID }
        val wavesEURAsset = activeMarkets.first { it.assetInfo.id == Constants.Fiat.EUR_ID }

        val filteredMarkets = activeMarkets
                .filter { it.assetInfo.id != Constants.Fiat.USD_ID && it.assetInfo.id != Constants.Fiat.EUR_ID }
                .toMutableList()

        return filteredMarkets.mapTo(mutableListOf(), { marketWidgetActiveMarket ->

            val usdData: MarketWidgetActiveMarket.UI.PriceData
            val eurData: MarketWidgetActiveMarket.UI.PriceData

            if (marketWidgetActiveMarket.assetInfo.id.isWavesId() || marketWidgetActiveMarket.assetInfo.id.isWaves()) {
                usdData = calculateWavesPriceFor(wavesUSDAsset)
                eurData = calculateWavesPriceFor(wavesEURAsset)
            } else {
                usdData = calculateTokenPriceFor(marketWidgetActiveMarket, wavesUSDAsset)
                eurData = calculateTokenPriceFor(marketWidgetActiveMarket, wavesEURAsset)
            }

            return@mapTo MarketWidgetActiveMarket.UI(marketWidgetActiveMarket.assetInfo.id,
                    marketWidgetActiveMarket.assetInfo.name,
                    usdData,
                    eurData)
        })
    }

    private fun calculateTokenPriceFor(activeMarket: MarketWidgetActiveMarket,
                                       wavesCurrencyAsset: MarketWidgetActiveMarket): MarketWidgetActiveMarket.UI.PriceData {
        val deltaPercentUsd = (activeMarket.data.lastPrice - activeMarket.data.firstPrice) * BigDecimal(100)
        val percentUsd =
                if (activeMarket.data.lastPrice != BigDecimal.ZERO) deltaPercentUsd / activeMarket.data.lastPrice
                else BigDecimal.ZERO

        val priceUsd =
                if (activeMarket.assetInfo.amountAsset.isWavesId() || activeMarket.assetInfo.amountAsset.isWaves()) {
                    if (activeMarket.data.quoteVolume != null && activeMarket.data.quoteVolume != BigDecimal.ZERO)
                        (activeMarket.data.volume.div(activeMarket.data.quoteVolume!!)).times(wavesCurrencyAsset.data.lastPrice)
                    else {
                        BigDecimal.ZERO
                    }
                } else {
                    if (activeMarket.data.volumeWaves != null && activeMarket.data.volume != BigDecimal.ZERO)
                        (activeMarket.data.volumeWaves!!.div(activeMarket.data.volume)).times(wavesCurrencyAsset.data.lastPrice)
                    else {
                        BigDecimal.ZERO
                    }
                }


        return MarketWidgetActiveMarket.UI.PriceData(priceUsd, percentUsd)
    }

    private fun calculateWavesPriceFor(wavesCurrencyAsset: MarketWidgetActiveMarket): MarketWidgetActiveMarket.UI.PriceData {
        val deltaPercentUsd = (wavesCurrencyAsset.data.lastPrice - wavesCurrencyAsset.data.firstPrice) * BigDecimal(100)
        val percentUsd = deltaPercentUsd / wavesCurrencyAsset.data.lastPrice
        val priceUsd = wavesCurrencyAsset.data.lastPrice
        return MarketWidgetActiveMarket.UI.PriceData(priceUsd, percentUsd)
    }

    private fun withDefaultPair(assets: MutableList<MarketWidgetActiveAsset>): MutableList<MarketWidgetActiveAsset> {
        val result = mutableListOf<MarketWidgetActiveAsset>()
        result.add(MarketWidgetActiveAsset("Dollar", Constants.Fiat.USD_ID, "WAVES", Constants.Fiat.USD_ID))
        result.add(MarketWidgetActiveAsset("Euro", Constants.Fiat.EUR_ID, "WAVES", Constants.Fiat.EUR_ID))
        result.addAll(assets)
        return result
    }

    fun clearSubscription() {
        compositeDisposable.clear()
    }

}