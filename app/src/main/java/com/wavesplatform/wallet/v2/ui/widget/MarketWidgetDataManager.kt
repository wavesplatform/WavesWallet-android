/*
 * Created by Eduard Zaydel on 30/7/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.widget

import android.content.Context
import com.wavesplatform.sdk.model.request.data.PairRequest
import com.wavesplatform.sdk.model.response.data.SearchPairResponse
import com.wavesplatform.sdk.utils.isWavesId
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.manager.DataServiceManager
import com.wavesplatform.wallet.v2.ui.widget.model.*
import com.wavesplatform.wallet.v2.util.executeInBackground
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class MarketWidgetDataManager @Inject constructor() {

    @Inject
    lateinit var dataServiceManager: DataServiceManager
    @Inject
    lateinit var activeAssetStore: MarketWidgetActiveStore<MarketWidgetActiveAsset>
    @Inject
    lateinit var activeMarketStore: MarketWidgetActiveStore<MarketWidgetActiveMarket.UI>

    private val compositeDisposable = CompositeDisposable()

    fun loadMarketsPrices(context: Context,
                          widgetId: Int,
                          successListener: () -> Unit,
                          errorListener: () -> Unit) {

        val activeAssets = withDefaultPair(activeAssetStore.queryAll(context, widgetId))
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
                    activeMarketStore.saveAll(context, widgetId, prices)
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

            if (marketWidgetActiveMarket.assetInfo.id.isWavesId()) {
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
                                       wavesUSDAsset: MarketWidgetActiveMarket): MarketWidgetActiveMarket.UI.PriceData {
        val deltaPercentUsd = (activeMarket.data.lastPrice - activeMarket.data.firstPrice) * 100
        val percentUsd = deltaPercentUsd / activeMarket.data.lastPrice
        val priceUsd = (activeMarket.data.volumeWaves?.div(activeMarket.data.volume))?.times(wavesUSDAsset.data.lastPrice)
                ?: 0.0
        return MarketWidgetActiveMarket.UI.PriceData(priceUsd, percentUsd)
    }

    private fun calculateWavesPriceFor(wavesCurrencyAsset: MarketWidgetActiveMarket): MarketWidgetActiveMarket.UI.PriceData {
        val deltaPercentUsd = (wavesCurrencyAsset.data.lastPrice - wavesCurrencyAsset.data.firstPrice) * 100
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