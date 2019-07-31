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
import com.wavesplatform.wallet.v2.ui.widget.model.MarketWidgetActiveAsset
import com.wavesplatform.wallet.v2.ui.widget.model.MarketWidgetActiveAssetMockStore
import com.wavesplatform.wallet.v2.ui.widget.model.MarketWidgetActiveMarket
import com.wavesplatform.wallet.v2.ui.widget.model.MarketWidgetActiveMarketStore
import com.wavesplatform.wallet.v2.util.executeInBackground
import io.reactivex.disposables.CompositeDisposable
import pyxis.uzuki.live.richutilskt.utils.runDelayed
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
        val activeAssets = withDefaultPair(MarketWidgetActiveAssetMockStore.queryAll(context, widgetId))
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
                    MarketWidgetActiveMarketStore.saveAll(context, widgetId, prices)
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

            val usdData: MarketWidgetActiveMarket.UI.USDData
            val eurData: MarketWidgetActiveMarket.UI.EURData

            // TODO: refactor to method
            if (marketWidgetActiveMarket.assetInfo.id.isWavesId()) {
                val deltaPercentUsd = (wavesUSDAsset.data.lastPrice / wavesUSDAsset.data.firstPrice) * 100
                val percentUsd = deltaPercentUsd / wavesUSDAsset.data.firstPrice
                val priceUsd = wavesUSDAsset.data.lastPrice
                usdData = MarketWidgetActiveMarket.UI.USDData(priceUsd, percentUsd)

                val deltaPercentEur = (wavesEURAsset.data.lastPrice / wavesEURAsset.data.firstPrice) * 100
                val percentEur = deltaPercentEur / wavesEURAsset.data.firstPrice
                val priceEur = wavesEURAsset.data.lastPrice
                eurData = MarketWidgetActiveMarket.UI.EURData(priceEur, percentEur)
            } else {
                val deltaPercentUsd = (marketWidgetActiveMarket.data.lastPrice / marketWidgetActiveMarket.data.firstPrice) * 100
                val percentUsd = deltaPercentUsd / marketWidgetActiveMarket.data.firstPrice
                val priceUsd = (marketWidgetActiveMarket.data.volumeWaves?.div(marketWidgetActiveMarket.data.volume))?.times(wavesUSDAsset.data.lastPrice)
                        ?: 0.0
                usdData = MarketWidgetActiveMarket.UI.USDData(priceUsd, percentUsd)

                val deltaPercentEur = (marketWidgetActiveMarket.data.lastPrice / marketWidgetActiveMarket.data.firstPrice) * 100
                val percentEur = deltaPercentEur / marketWidgetActiveMarket.data.firstPrice
                val priceEur = (marketWidgetActiveMarket.data.volumeWaves?.div(marketWidgetActiveMarket.data.volume))?.times(wavesEURAsset.data.lastPrice)
                        ?: 0.0
                eurData = MarketWidgetActiveMarket.UI.EURData(priceEur, percentEur)
            }

            return@mapTo MarketWidgetActiveMarket.UI(marketWidgetActiveMarket.assetInfo.id,
                    marketWidgetActiveMarket.assetInfo.name,
                    usdData,
                    eurData)
        })
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