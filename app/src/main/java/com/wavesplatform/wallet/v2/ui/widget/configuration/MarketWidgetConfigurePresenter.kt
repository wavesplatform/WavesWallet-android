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
import com.wavesplatform.wallet.v2.util.isFiat
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import javax.inject.Inject


@InjectViewState
class MarketWidgetConfigurePresenter @Inject constructor() : BasePresenter<MarketWidgetConfigureView>() {

    var assets = arrayListOf<String>()
    var themeName = MarketWidgetStyle.CLASSIC
    var intervalUpdate = MarketWidgetUpdateInterval.MIN_10
    var widgetAssetPairs = arrayListOf<MarketWidgetActiveAsset>()
    var canAddPair = false

    fun loadAssetsPairs(context: Context, widgetId: Int) {

        val initAssetList = MarketWidgetSettings.assetsSettings().queryAll(context, widgetId).isEmpty()

        setInitAppWidgetConfig(context, widgetId)

        val pairsList = createPairs(assets)
        addSubscription(
                Observable.zip(dataServiceManager.assets(ids = assets),
                        dataServiceManager.loadPairs(pairs = pairsList)
                                .flatMap { pairs ->
                                    Observable.just(getFilledPairs(pairs, pairsList, initAssetList))
                                },
                        BiFunction { assetInfoList: List<AssetInfoResponse>, searchPair: List<SearchPairResponse.Pair> ->
                            return@BiFunction Pair(assetInfoList, searchPair)
                        })
                        .compose(RxUtil.applyObservableDefaultSchedulers())
                        .subscribe({ (assetInfoList, searchPair) ->
                            val tokenPairList = arrayListOf<MarketWidgetConfigurationMarketsAdapter.TokenPair>()
                            searchPair.forEach { assetPair ->

                                val id = when {
                                    assetPair.priceAsset?.isWaves() == true ->
                                        assetPair.amountAsset
                                    isFiat(assetPair.priceAsset ?: "") ->
                                        assetPair.amountAsset
                                    else ->
                                        assetPair.priceAsset
                                }

                                val assetInfo = assetInfoList.firstOrNull { it.id == id }
                                assetInfo.notNull {
                                    tokenPairList.add(
                                            MarketWidgetConfigurationMarketsAdapter.TokenPair(
                                                    it, assetPair))
                                }
                            }
                            viewState.onUpdatePairs(tokenPairList)
                        }, {
                            it.printStackTrace()
                            viewState.onFailGetMarkets()
                        })
        )
    }

    fun loadAssetPair(assetInfo: AssetInfoResponse) {
        val pairsList = createPairs(mutableListOf(assetInfo.id))
        addSubscription(dataServiceManager.loadPairs(pairs = pairsList)
                .flatMap { pairs ->
                    Observable.just(getFilledPairs(pairs, pairsList, false))
                }
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe(
                        { searchPair ->
                            val tokenPairList = arrayListOf<MarketWidgetConfigurationMarketsAdapter.TokenPair>()
                            searchPair.forEach { assetPair ->
                                assetInfo.notNull {
                                    tokenPairList.add(
                                            MarketWidgetConfigurationMarketsAdapter.TokenPair(
                                                    it, assetPair))
                                }
                            }
                            viewState.onAddPairs(tokenPairList)
                        },
                        {
                            it.printStackTrace()
                            viewState.onFailGetMarkets()
                        }))
    }

    private fun setInitAppWidgetConfig(context: Context, widgetId: Int) {
        val assetsList =
                MarketWidgetSettings.assetsSettings().queryAll(context, widgetId)

        if (assetsList.isEmpty()) {
            Constants.defaultCrypto().toList().forEach {
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

    private fun createPairs(assets: List<String>): MutableList<String> {
        val initPairsList = mutableListOf<String>()

        for (priceAssetId in assets) {
            if (priceAssetId.isWaves()) {
                initPairsList.add("${WavesConstants.WAVES_ASSET_ID_FILLED}/${EnvironmentManager.environment.externalProperties.usdId}")
                initPairsList.add("${EnvironmentManager.environment.externalProperties.usdId}/${WavesConstants.WAVES_ASSET_ID_FILLED}")
                continue
            } else {
                initPairsList.add("${WavesConstants.WAVES_ASSET_ID_FILLED}/$priceAssetId")
                initPairsList.add("$priceAssetId/${WavesConstants.WAVES_ASSET_ID_FILLED}")
            }
        }
        return initPairsList
    }

    private fun getFilledPairs(pairs: SearchPairResponse, assets: MutableList<String>,
                               init: Boolean = true)
            : MutableList<SearchPairResponse.Pair> {
        val filledResult = mutableListOf<SearchPairResponse.Pair>()
        pairs.data.forEachIndexed { index, item ->
            if (item.data != null && index < assets.size) {
                val pair = assets[index].split("/")
                item.amountAsset = pair[0]
                item.priceAsset = pair[1]
                filledResult.add(item)
            }
        }

        val assetsMaxCount = if (init) {
            9
        } else {
            10
        }

        return if (filledResult.size > assetsMaxCount) {
            filledResult.subList(0, assetsMaxCount)
        } else {
            filledResult
        }
    }
}