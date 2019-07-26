/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.dex.markets

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryAll
import com.wavesplatform.sdk.model.request.data.PairRequest
import com.wavesplatform.sdk.model.response.data.AssetInfoResponse
import com.wavesplatform.sdk.model.response.data.SearchPairResponse
import com.wavesplatform.sdk.model.response.matcher.MarketResponse
import com.wavesplatform.sdk.utils.RxUtil
import com.wavesplatform.sdk.utils.notNull
import com.wavesplatform.wallet.v2.data.model.db.userdb.MarketResponseDb
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.EnvironmentManager
import io.reactivex.Observable
import pyxis.uzuki.live.richutilskt.utils.runAsync
import pyxis.uzuki.live.richutilskt.utils.runOnUiThread
import javax.inject.Inject

@InjectViewState
class DexMarketsPresenter @Inject constructor() : BasePresenter<DexMarketsView>() {
    var needToUpdate: Boolean = false

    fun initLoad() {
        runAsync {

            val initPairsList = createInitPairs()
            var requestResult: SearchPairResponse? = null
            val assetIds = hashSetOf<String>()

            addSubscription(dataServiceManager.loadPairs(PairRequest(pairs = initPairsList, limit = 200))
                    .flatMap { result ->
                        for (index in 0 until result.data.size) {
                            if (result.data[index].data != null) {
                                val pair = initPairsList[index].split("/")
                                assetIds.add(pair[0])
                                assetIds.add(pair[1])

                                result.data[index].amountAsset = pair[0]
                                result.data[index].priceAsset = pair[1]
                            }
                        }
                        requestResult = result
                        dataServiceManager.assetsInfoByIds(assetIds.toList())
                    }
                    .flatMap {
                        Observable.just(createMarkets(requestResult, it))
                    }
                    .compose(RxUtil.applyObservableDefaultSchedulers())
                    .subscribe({
                        runOnUiThread {
                            viewState.afterSuccessGetMarkets(it)
                        }
                    }, {
                        runOnUiThread {
                            viewState.afterFailGetMarkets()
                        }
                        it.printStackTrace()
                    }))
        }
    }

    fun search(query: String) {

        if (query.isEmpty()) {
            return
        }

        runAsync {
            fun getObservableLoadPairs(char: Char): Observable<SearchPairResponse> {
                val searchByAssets = query.split(char).filter { it.trim().isNotEmpty() }
                return if (searchByAssets.size > 1) {
                    dataServiceManager.loadPairs(searchByAssets = searchByAssets)
                } else {
                    dataServiceManager.loadPairs(searchByAsset = searchByAssets[0])
                }
            }

            val observableSearch = when {
                query.contains('/') -> getObservableLoadPairs('/')
                query.contains('\\') -> getObservableLoadPairs('\\')
                else -> dataServiceManager.loadPairs(searchByAsset = query)
            }

            var searchResult: SearchPairResponse? = null
            val assetIds = hashSetOf<String>()

            addSubscription(observableSearch
                    .flatMap { result ->
                        searchResult = result
                        result.data.forEach {
                            it.amountAsset.notNull { assetId ->
                                assetIds.add(assetId)
                            }
                            it.priceAsset.notNull { assetId ->
                                assetIds.add(assetId)
                            }
                        }
                        dataServiceManager.assetsInfoByIds(assetIds.toList())
                    }
                    .flatMap {
                        Observable.just(createMarkets(searchResult, it))
                    }
                    .compose(RxUtil.applyObservableDefaultSchedulers())
                    .subscribe({
                        runOnUiThread {
                            viewState.afterSuccessGetMarkets(it)
                        }
                    }, {
                        runOnUiThread {
                            viewState.afterFailGetMarkets()
                        }
                        it.printStackTrace()
                    }))
        }
    }

    private fun createMarkets(searchResult: SearchPairResponse?, assets: List<AssetInfoResponse>)
            : MutableList<MarketResponse> {

        val marketList = mutableListOf<MarketResponse>()
        val savedMarkets = queryAll<MarketResponseDb>()

        searchResult?.data?.forEach { data ->
            if (data.data == null || data.amountAsset == null || data.priceAsset == null) {
                return@forEach
            }
            marketList.add(createMarket(data, assets, savedMarkets))
        }

        return marketList
    }

    private fun createMarket(data: SearchPairResponse.Pair,
                             assets: List<AssetInfoResponse>,
                             savedMarkets: List<MarketResponseDb>): MarketResponse {

        fun find(assetId: String): AssetInfoResponse? {
            assets.forEach {
                if (it.id == assetId) {
                    return it
                }
            }
            return null
        }

        val market = MarketResponse()
        val amountAsset = find(data.amountAsset!!)
        val priceAsset = find(data.priceAsset!!)

        market.id = data.amountAsset + data.priceAsset

        market.amountAsset = data.amountAsset!!
        market.priceAsset = data.priceAsset!!

        market.amountAssetLongName = amountAsset?.name
        market.priceAssetLongName = priceAsset?.name

        market.amountAssetShortName = amountAsset?.ticker ?: amountAsset?.name
        market.priceAssetShortName = priceAsset?.ticker ?: priceAsset?.name

        market.amountAssetDecimals = amountAsset?.precision ?: 8
        market.priceAssetDecimals = priceAsset?.precision ?: 8

        market.checked = savedMarkets.firstOrNull { it.id == market.id } != null

        return market
    }

    private fun createInitPairs(): MutableList<String> {
        val initPairsList = mutableListOf<String>()
        for (amountAssetId in EnvironmentManager.defaultAssets.subList(0, 4)) {
            for (priceAssetId in EnvironmentManager.defaultAssets) {

                var price = priceAssetId.assetId
                var amount = amountAssetId.assetId

                if (price == "") {
                    price = "WAVES"
                }

                if (amount == "") {
                    amount = "WAVES"
                }

                if (amount != price) {
                    initPairsList.add("$amount/$price")
                    initPairsList.add("$price/$amount")
                }
            }
        }
        return initPairsList
    }
}
