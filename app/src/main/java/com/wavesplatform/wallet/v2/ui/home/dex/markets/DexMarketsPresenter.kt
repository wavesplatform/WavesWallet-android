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
import com.wavesplatform.sdk.utils.*
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.db.SpamAssetDb
import com.wavesplatform.wallet.v2.data.model.db.userdb.MarketResponseDb
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.EnvironmentManager
import com.wavesplatform.wallet.v2.util.PrefsUtil
import io.reactivex.Observable
import javax.inject.Inject

@InjectViewState
class DexMarketsPresenter @Inject constructor() : BasePresenter<DexMarketsView>() {

    var needToUpdate: Boolean = false
    private val defaultAssets: MutableList<String> = mutableListOf()

    init {
        EnvironmentManager.defaultAssets.forEach {
            defaultAssets.add(it.assetId)
        }
        defaultAssets.add(Constants.MrtGeneralAsset.assetId)
        defaultAssets.add(Constants.WctGeneralAsset.assetId)
    }

    fun initLoad() {
        val initPairsList = createInitPairs()
        var requestResult: SearchPairResponse? = null
        val assetIds = hashSetOf<String>()

        addSubscription(dataServiceManager.loadPairs(
                PairRequest(
                        pairs = initPairsList,
                        limit = 200,
                        matcher = EnvironmentManager.getMatcherAddress()))
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
                    dataServiceManager.assets(ids = assetIds.toList())
                }
                .flatMap {
                    Observable.just(createMarkets(requestResult, it))
                }
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({ initMarkets ->
                    viewState.afterSuccessGetMarkets(initMarkets)
                }, {
                    viewState.afterFailGetMarkets()
                    it.printStackTrace()
                }))
    }

    fun search(query: String) {

        if (query.isEmpty()) {
            initLoad()
            return
        }

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
                    dataServiceManager.assets(ids = assetIds.toList())
                }
                .flatMap {
                    Observable.just(createMarkets(searchResult, it))
                }
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({ foundMarkets ->
                    viewState.afterSuccessGetMarkets(foundMarkets)
                }, {
                    viewState.afterFailGetMarkets()
                    it.printStackTrace()
                }))
    }

    private fun createMarkets(searchResult: SearchPairResponse?, assets: List<AssetInfoResponse>)
            : MutableList<MarketResponse> {

        val marketMap = hashMapOf<String, MarketResponse>()
        val savedMarkets = queryAll<MarketResponseDb>()

        searchResult?.data?.forEach { data ->
            if (data.data == null || data.amountAsset == null || data.priceAsset == null) {
                return@forEach
            }
            val market = createMarket(data, assets, savedMarkets)
            marketMap[market.id ?: ""] = market
        }

        val sortedList = sort(marketMap.values.toMutableList())
        return filterSpam(sortedList)
    }


    private fun sort(markets: MutableList<MarketResponse>): MutableList<MarketResponse> {

        // configure hash groups
        val hashGroup = linkedMapOf<String, MutableList<MarketResponse>>()

        defaultAssets.forEach {
            if (it.isWaves()) {
                hashGroup[WavesConstants.WAVES_ASSET_ID_EMPTY] = mutableListOf()
            } else {
                hashGroup[it] = mutableListOf()
            }
        }

        val other = "other"
        hashGroup[other] = mutableListOf()

        // fill information and sort by group
        markets.forEach { market ->

            val group = if (market.amountAsset.isWaves()) {
                hashGroup[WavesConstants.WAVES_ASSET_ID_EMPTY]
            } else {
                hashGroup[market.amountAsset]
            }

            if (group != null) {
                group.add(market)
            } else {
                hashGroup[other]?.add(market)
            }
        }

        val sortedMarketsList = mutableListOf<MarketResponse>()

        hashGroup.values.forEach {
            sortedMarketsList.addAll(it)
        }

        return sortedMarketsList
    }


    private fun filterSpam(markets: MutableList<MarketResponse>): MutableList<MarketResponse> {
        val spamAssets = queryAll<SpamAssetDb>()

        val filteredSpamList = if (prefsUtil.getValue(PrefsUtil.KEY_ENABLE_SPAM_FILTER, true)) {
            markets.filter { market ->
                val amountSpam = spamAssets.firstOrNull {
                    it.assetId == market.amountAsset
                }
                val priceSpam = spamAssets.firstOrNull {
                    it.assetId == market.priceAsset
                }
                amountSpam == null && priceSpam == null
            }
        } else {
            markets
        }

        return filteredSpamList.toMutableList()
    }


    private fun createMarket(data: SearchPairResponse.Pair,
                             assets: List<AssetInfoResponse>,
                             savedMarkets: List<MarketResponseDb>): MarketResponse {

        val market = MarketResponse()

        val amountAsset = assets.firstOrNull { it.id == data.amountAsset }
        val priceAsset = assets.firstOrNull { it.id == data.priceAsset }

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

        if (defaultAssets.firstOrNull { it == amountAsset?.id } != null
                || defaultAssets.firstOrNull { it == priceAsset?.id } != null) {
            market.popular = true
        }

        return market
    }

    private fun createInitPairs(): MutableList<String> {
        val initPairsList = mutableListOf<String>()

        val firstMainAssets = if (FIRST_MAIN_ASSETS_ID < defaultAssets.size) {
            defaultAssets.subList(0, FIRST_MAIN_ASSETS_ID)
        } else {
            defaultAssets
        }

        for (amountAssetId in firstMainAssets) {
            for (priceAssetId in defaultAssets) {

                var price = priceAssetId
                var amount = amountAssetId

                if (price.isWavesId()) {
                    price = WavesConstants.WAVES_ASSET_ID_FILLED
                }

                if (amount.isWavesId()) {
                    amount = WavesConstants.WAVES_ASSET_ID_FILLED
                }

                if (amount != price) {
                    initPairsList.add("$amount/$price")
                    initPairsList.add("$price/$amount")
                }
            }
        }
        return initPairsList
    }

    companion object {
        const val FIRST_MAIN_ASSETS_ID = 4
    }
}
