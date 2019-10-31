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
import com.wavesplatform.sdk.utils.WavesConstants
import com.wavesplatform.sdk.utils.isWaves
import com.wavesplatform.sdk.utils.notNull
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.db.SpamAssetDb
import com.wavesplatform.wallet.v2.data.model.db.userdb.MarketResponseDb
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.EnvironmentManager
import com.wavesplatform.wallet.v2.util.PrefsUtil
import com.wavesplatform.wallet.v2.util.mapCorrectPairs
import com.wavesplatform.wallet.v2.util.safeLet
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import javax.inject.Inject

@InjectViewState
class DexMarketsPresenter @Inject constructor() : BasePresenter<DexMarketsView>() {

    var needToUpdate: Boolean = false
    private val defaultAssets: MutableList<String> = mutableListOf()
    private val pricesOrder: MutableList<String> = mutableListOf()

    init {
        EnvironmentManager.defaultAssets.forEach {
            if (it.assetId == WavesConstants.WAVES_ASSET_ID_EMPTY) {
                defaultAssets.add(WavesConstants.WAVES_ASSET_ID_FILLED)
            } else {
                defaultAssets.add(it.assetId)
            }
        }
        defaultAssets.add(Constants.MrtGeneralAsset.assetId)
        defaultAssets.add(Constants.WctGeneralAsset.assetId)
    }

    fun search(query: String) {

        val assetPair = when {
            query.isEmpty() -> arrayListOf()
            query.contains('/') -> query.split("/")
            query.contains('\\') -> query.split("\\")
            else -> arrayListOf(query)
        }
        val observablePair =
                createObservablePair(assetPair)
        val assetInfoHashMap = hashMapOf<String, AssetInfoResponse>()

        addSubscription(matcherServiceManager.getSettings()
                .flatMap {
                    pricesOrder.clear()
                    pricesOrder.addAll(it.priceAssets)
                    getAllAssetInfoObservable(observablePair) }
                .flatMap { (amountAssetInfoList,
                                   priceAssetInfoList) ->

                    if (amountAssetInfoList.isEmpty() || priceAssetInfoList.isEmpty()) {
                        throw Exception(ERROR_CANT_FIND_ASSETS)
                    }

                    amountAssetInfoList.forEach { item ->
                        item.notNull {
                            assetInfoHashMap[it.id] = it
                        }
                    }
                    priceAssetInfoList.forEach {
                        assetInfoHashMap[it.id] = it
                    }

                    getCorrectPairsObservable(amountAssetInfoList, priceAssetInfoList)
                }
                .flatMap { (pairsKeys, pairsMap,
                                   searchPairResponse) ->
                    getFilledSortedPairsObservable(
                            searchPairResponse, pairsKeys, pairsMap, assetInfoHashMap)
                }
                .flatMap { (searchPair, assetInfoList) ->
                    getFilledSortedMarketsObservable(searchPair, assetInfoList)
                }
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({ foundMarkets ->
                    viewState.afterSuccessGetMarkets(foundMarkets)
                }, {
                    if (it.message == ERROR_CANT_FIND_ASSETS) {
                        viewState.afterFailGetMarkets(App.appContext.getString(
                                R.string.market_widget_config_cant_find_currency_pair))
                    } else {
                        viewState.afterFailGetMarkets()
                        it.printStackTrace()
                    }
                }))
    }

    private fun getFilledSortedPairsObservable(
            searchPairResponse: SearchPairResponse,
            pairsKeys: List<String>,
            pairsMap: HashMap<String, Pair<String, String>>,
            assetInfoHashMap: HashMap<String, AssetInfoResponse>)
            : Observable<Pair<SearchPairResponse, List<AssetInfoResponse>>>? {
        val assetIds = hashSetOf<String>()
        searchPairResponse.data.forEachIndexed { index, data ->
            val key = pairsKeys[index]
            val pair = pairsMap[key] ?: Pair("", "")
            data.amountAsset = pair.first
            data.priceAsset = pair.second
            assetIds.add(pair.first)
            assetIds.add(pair.second)
        }

        searchPairResponse.data = searchPairResponse.data.sortedBy { it.data?.volumeWaves }

        return Observable.zip(
                Observable.just(searchPairResponse),
                Observable.just(assetInfoHashMap.values.toList()),
                BiFunction { searchPair: SearchPairResponse, assetInfoList: List<AssetInfoResponse> ->
                    return@BiFunction Pair(searchPair, assetInfoList)
                })
    }

    private fun getCorrectPairsObservable(
            amountAssetInfoList: List<AssetInfoResponse>,
            priceAssetInfoList: List<AssetInfoResponse>)
            : Observable<Triple<List<String>, HashMap<String, Pair<String, String>>, SearchPairResponse>>? {
        val pairs = mutableListOf<Pair<String, String>>()
        amountAssetInfoList.forEach { amount ->
            priceAssetInfoList.forEach { price ->
                safeLet(amount, price) { safeAmount, safePrice ->
                    if (safeAmount.id != safePrice.id) {
                        pairs.add(Pair(safeAmount.id, safePrice.id))
                    }
                }
            }
        }

        val pairsMap = hashMapOf<String, Pair<String, String>>()
        mapCorrectPairs(pricesOrder, pairs).forEach {
            pairsMap[it.first + "/" + it.second] = it
        }
        val pairsKeys = pairsMap.keys.toList()
        return Observable.zip(
                Observable.just(pairsKeys),
                Observable.just(pairsMap),
                dataServiceManager.loadPairs(
                        PairRequest(
                                pairs = pairsKeys,
                                matcher = EnvironmentManager.getMatcherAddress())),
                Function3 { keys: List<String>,
                            map: HashMap<String, Pair<String, String>>,
                            searchPairResponse: SearchPairResponse ->
                    return@Function3 Triple(keys, map, searchPairResponse)
                })
    }

    private fun getAllAssetInfoObservable(observablePair: Pair<Observable<List<AssetInfoResponse>>, Observable<List<AssetInfoResponse>>>): Observable<Pair<List<AssetInfoResponse>, List<AssetInfoResponse>>>? {
        return Observable.zip(
                observablePair.first,
                observablePair.second,
                BiFunction { amountAssetInfoList: List<AssetInfoResponse>,
                             priceAssetInfoList: List<AssetInfoResponse> ->
                    return@BiFunction Pair(amountAssetInfoList, priceAssetInfoList)
                })
    }

    private fun createObservablePair(assetPair: List<String>)
            : Pair<Observable<List<AssetInfoResponse>>, Observable<List<AssetInfoResponse>>> {
        val observableAmount: Observable<List<AssetInfoResponse>>
        val observablePrice: Observable<List<AssetInfoResponse>>
        when {
            assetPair.size == 2 -> {
                observableAmount = dataServiceManager.assets(search = assetPair[0])
                observablePrice = if (assetPair[1] == "") {
                    dataServiceManager.assets(ids = defaultAssets)
                } else {
                    dataServiceManager.assets(search = assetPair[1])
                }
            }
            assetPair.size == 1 -> {
                observableAmount = dataServiceManager.assets(search = assetPair[0])
                observablePrice = dataServiceManager.assets(ids = defaultAssets)
            }
            else -> {
                observableAmount = dataServiceManager.assets(ids = defaultAssets)
                observablePrice = dataServiceManager.assets(
                        ids = defaultAssets.subList(0, FIRST_MAIN_ASSETS_ID))
            }
        }
        return Pair(observableAmount, observablePrice)
    }

    private fun getFilledSortedMarketsObservable(searchResult: SearchPairResponse?, assets: List<AssetInfoResponse>)
            : Observable<MutableList<MarketResponse>> {

        val marketMap = hashMapOf<String, MarketResponse>()
        val savedMarkets = queryAll<MarketResponseDb>()

        searchResult?.data?.forEach { data ->
            if (data.amountAsset == null || data.priceAsset == null) {
                return@forEach
            }
            val market = createMarket(data, assets, savedMarkets)
            marketMap[market.id ?: ""] = market
        }

        val sortedList = sort(marketMap.values.toMutableList())

        return Observable.just(filterSpam(sortedList))
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


    private fun sort(markets: MutableList<MarketResponse>): MutableList<MarketResponse> {

        // configure hash groups
        val hashGroup =
                linkedMapOf<String, MutableList<MarketResponse>>()

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
        val spamAssets = queryAll<SpamAssetDb>().associateBy { it.assetId }

        val filteredSpamList = if (prefsUtil.getValue(
                        PrefsUtil.KEY_ENABLE_SPAM_FILTER, true)) {
            markets.filter { market ->
                val amountSpam = spamAssets.containsKey(market.amountAsset)
                val priceSpam = spamAssets.containsKey(market.priceAsset)
                !amountSpam && !priceSpam
            }
        } else {
            markets
        }

        return filteredSpamList.toMutableList()
    }

    companion object {
        const val FIRST_MAIN_ASSETS_ID = 4
        const val ERROR_CANT_FIND_ASSETS = "ERROR_CANT_FIND_ASSETS"
    }
}
