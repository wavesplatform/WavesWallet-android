/*
 * Created by Eduard Zaydel on 8/8/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.manager

import android.content.Context
import com.wavesplatform.sdk.model.request.data.PairRequest
import com.wavesplatform.sdk.model.response.data.SearchPairResponse
import com.wavesplatform.sdk.utils.WavesConstants
import com.wavesplatform.sdk.utils.isWaves
import com.wavesplatform.sdk.utils.isWavesId
import com.wavesplatform.wallet.v2.data.model.local.widget.MarketWidgetActiveAsset
import com.wavesplatform.wallet.v2.data.model.local.widget.MarketWidgetActiveMarket
import com.wavesplatform.wallet.v2.data.model.local.widget.MarketWidgetSettings
import com.wavesplatform.wallet.v2.util.EnvironmentManager
import com.wavesplatform.wallet.v2.util.executeInBackground
import io.reactivex.Observable
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

        compositeDisposable.add(dataServiceManager.loadPairs(
                PairRequest(
                        pairs = activeAssetsIds,
                        matcher = EnvironmentManager.getMatcherAddress()))
                .flatMap { searchResult ->
                    Observable.fromIterable(activeAssets)
                            .flatMap { activeAsset ->
                                dataServiceManager.getLastExchangesByPair(activeAsset.amountAsset, activeAsset.priceAsset, DEFAULT_LIMIT)
                                        .map { transactions ->
                                            if (transactions.isEmpty()) {
                                                return@map activeAsset to 0.0
                                            } else {
                                                return@map activeAsset to transactions.sumByDouble { it.price } / transactions.size
                                            }
                                        }
                            }
                            .map { (activeAsset, price) ->
                                val assetIndex = activeAssets.indexOf(activeAsset)
                                MarketWidgetActiveMarket(activeAsset,
                                        searchResult.data[assetIndex].data
                                                ?: SearchPairResponse.Pair.Data(),
                                        price)
                            }
                            .toList().toObservable()
                }
                .map { activeMarkets ->
                    val prices = calculatePrice(activeMarkets)
                    MarketWidgetSettings.marketsSettings().saveAll(context, widgetId, prices)
                }
                .executeInBackground()
                .subscribe({
                    successListener.invoke()
                }, {
                    errorListener.invoke()
                    it.printStackTrace()
                }))
    }

    private fun calculatePrice(activeMarkets: MutableList<MarketWidgetActiveMarket>): MutableList<MarketWidgetActiveMarket.UI> {
        val wavesUSDAsset = activeMarkets.first { it.assetInfo.id == EnvironmentManager.environment.externalProperties.usdId }
        val wavesEURAsset = activeMarkets.first { it.assetInfo.id == EnvironmentManager.environment.externalProperties.eurId }

        val filteredMarkets = activeMarkets
                .filter { it.assetInfo.id != EnvironmentManager.environment.externalProperties.usdId && it.assetInfo.id != EnvironmentManager.environment.externalProperties.eurId }
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
        val deltaPercent = (activeMarket.data.lastPrice - activeMarket.data.firstPrice) * BigDecimal(100)
        val percent =
                if (activeMarket.data.lastPrice != BigDecimal.ZERO) deltaPercent / activeMarket.data.lastPrice
                else BigDecimal.ZERO

        val price =
                if (activeMarket.assetInfo.amountAsset.isWavesId() || activeMarket.assetInfo.amountAsset.isWaves()) {
                    if (activeMarket.assetPrice != 0.0)
                        (1 / activeMarket.assetPrice * wavesCurrencyAsset.assetPrice).toBigDecimal()
                    else {
                        BigDecimal.ZERO
                    }
                } else {
                    if (activeMarket.assetPrice != 0.0)
                        (activeMarket.assetPrice * wavesCurrencyAsset.assetPrice).toBigDecimal()
                    else {
                        BigDecimal.ZERO
                    }
                }


        return MarketWidgetActiveMarket.UI.PriceData(price, percent)
    }

    private fun calculateWavesPriceFor(wavesCurrencyAsset: MarketWidgetActiveMarket): MarketWidgetActiveMarket.UI.PriceData {
        val deltaPercent = (wavesCurrencyAsset.data.lastPrice - wavesCurrencyAsset.data.firstPrice) * BigDecimal(100)
        val percent =
                if (wavesCurrencyAsset.data.lastPrice != BigDecimal.ZERO) deltaPercent / wavesCurrencyAsset.data.lastPrice
                else BigDecimal.ZERO
        val price = wavesCurrencyAsset.data.lastPrice
        return MarketWidgetActiveMarket.UI.PriceData(price, percent)
    }

    private fun withDefaultPair(assets: MutableList<MarketWidgetActiveAsset>): MutableList<MarketWidgetActiveAsset> {
        val result = mutableListOf<MarketWidgetActiveAsset>()
        result.add(MarketWidgetActiveAsset(USD_NAME, EnvironmentManager.environment.externalProperties.usdId,
                WavesConstants.WAVES_ASSET_ID_FILLED, EnvironmentManager.environment.externalProperties.usdId))
        result.add(MarketWidgetActiveAsset(EUR_NAME, EnvironmentManager.environment.externalProperties.eurId,
                WavesConstants.WAVES_ASSET_ID_FILLED, EnvironmentManager.environment.externalProperties.eurId))
        result.addAll(assets)
        return result
    }

    fun clearSubscription() {
        compositeDisposable.clear()
    }

    companion object {
        const val USD_NAME = "Dollar"
        const val EUR_NAME = "Euro"
        const val DEFAULT_LIMIT = 5
    }

}