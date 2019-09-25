/*
 * Created by Eduard Zaydel on 8/8/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.widget.configuration

import android.content.Context
import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.sdk.model.response.data.AssetInfoResponse
import com.wavesplatform.sdk.model.response.data.SearchPairResponse
import com.wavesplatform.sdk.utils.*
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.local.widget.MarketWidgetActiveAsset
import com.wavesplatform.wallet.v2.data.model.local.widget.MarketWidgetSettings
import com.wavesplatform.wallet.v2.data.model.local.widget.MarketWidgetStyle
import com.wavesplatform.wallet.v2.data.model.local.widget.MarketWidgetUpdateInterval
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.EnvironmentManager
import com.wavesplatform.wallet.v2.util.correctPair
import com.wavesplatform.wallet.v2.util.isFiat
import com.wavesplatform.wallet.v2.util.mapCorrectPairs
import javax.inject.Inject


@InjectViewState
class MarketWidgetConfigurePresenter @Inject constructor() : BasePresenter<MarketWidgetConfigureView>() {

    var assets = arrayListOf<String>()
    var themeName = MarketWidgetStyle.CLASSIC
    var intervalUpdate = MarketWidgetUpdateInterval.MIN_10
    var widgetAssetPairs = arrayListOf<MarketWidgetActiveAsset>()
    var canAddPair = false
    private val pricesOrder = mutableListOf<String>()

    fun loadAssetsPairs(context: Context, widgetId: Int) {
        val initAssetList = MarketWidgetSettings.assetsSettings()
                .queryAll(context, widgetId).isEmpty()

        setInitAppWidgetConfig(context, widgetId)

        addSubscription(
                matcherServiceManager.getSettings()
                        .flatMap {
                            pricesOrder.clear()
                            pricesOrder.addAll(it.priceAssets)
                            dataServiceManager.assets(ids = assets)
                        }
                        .compose(RxUtil.applyObservableDefaultSchedulers())
                        .subscribe({ assetInfoList ->

                            val pairs = mapCorrectPairs(
                                    pricesOrder, createPairs(assets))

                            val tokenPairList =
                                    arrayListOf<MarketWidgetConfigurationMarketsAdapter.TokenPair>()

                            pairs.forEach { (amountAssetId, priceAssetId ) ->
                                val amountAssetInfo = assetInfoList
                                        .firstOrNull { it.id == amountAssetId }
                                val priceAssetInfo = assetInfoList
                                        .firstOrNull { it.id == priceAssetId }

                                val assetInfoResponse = when {
                                    isFiat(amountAssetId) -> priceAssetInfo
                                    isFiat(priceAssetId) -> amountAssetInfo
                                    amountAssetId.isWaves() -> priceAssetInfo
                                    else -> amountAssetInfo
                                }

                                assetInfoResponse.notNull {
                                    tokenPairList.add(
                                            MarketWidgetConfigurationMarketsAdapter.TokenPair(
                                                    it,
                                                    SearchPairResponse.Pair(
                                                            amountAsset = amountAssetId,
                                                            priceAsset = priceAssetId)))
                                }
                            }

                            val assetsMaxCount = if (initAssetList) {
                                INIT_WIDGET_VIEW_ASSETS_MAX_COUNT
                            } else {
                                WIDGET_VIEW_ASSETS_MAX_COUNT
                            }

                            when {
                                tokenPairList.isEmpty() ->
                                    viewState.onFailGetMarkets()
                                tokenPairList.size > assetsMaxCount ->
                                    viewState.onUpdatePairs(tokenPairList.subList(0, assetsMaxCount))
                                else ->
                                    viewState.onUpdatePairs(tokenPairList)
                            }
                        }, {
                            it.printStackTrace()
                            viewState.onFailGetMarkets()
                        })
        )
    }

    fun loadAssetPair(assetInfo: AssetInfoResponse) {
        val pair = correctPair(
                pricesOrder, Pair(assetInfo.id, WavesConstants.WAVES_ASSET_ID_FILLED))

        val tokenPairList =
                arrayListOf<MarketWidgetConfigurationMarketsAdapter.TokenPair>()
        tokenPairList.add(
                MarketWidgetConfigurationMarketsAdapter.TokenPair(
                        assetInfo, SearchPairResponse.Pair(amountAsset = pair.first,
                        priceAsset = pair.second)))

        viewState.onAddPairs(tokenPairList)
    }

    private fun setInitAppWidgetConfig(context: Context, widgetId: Int) {
        val assetsList =
                MarketWidgetSettings.assetsSettings().queryAll(context, widgetId)

        if (assetsList.isEmpty()) {
            val crypto = Constants.defaultCrypto().toList()
            val limitedCrypto = if (crypto.size > INIT_WIDGET_VIEW_ASSETS_MAX_COUNT) {
                crypto.subList(0, INIT_WIDGET_VIEW_ASSETS_MAX_COUNT)
            } else {
                crypto
            }

            limitedCrypto.forEach {
                if (it.isWavesId()) {
                    assets.add(WavesConstants.WAVES_ASSET_ID_FILLED)
                } else {
                    assets.add(it)
                }
            }
        } else {
            assets.clear()
            assetsList.forEach {
                assets.add(MarketWidgetActiveAsset.getMainAssetId(it))
            }
        }

        intervalUpdate = MarketWidgetSettings.intervalSettings().getInterval(context, widgetId)
        themeName = MarketWidgetSettings.themeSettings().getTheme(context, widgetId)
    }

    private fun createPairs(assets: List<String>): MutableList<Pair<String, String>> {
        val initPairsList = mutableListOf<Pair<String, String>>()

        for (priceAssetId in assets) {
            if (priceAssetId.isWaves()) {
                initPairsList.add(Pair(
                        WavesConstants.WAVES_ASSET_ID_FILLED,
                        EnvironmentManager.environment.externalProperties.usdId))
            } else {
                initPairsList.add(Pair(
                        WavesConstants.WAVES_ASSET_ID_FILLED,
                        priceAssetId))
            }
        }
        return initPairsList
    }

    companion object {
        private const val INIT_WIDGET_VIEW_ASSETS_MAX_COUNT = 9
        private const val WIDGET_VIEW_ASSETS_MAX_COUNT = 10
    }
}