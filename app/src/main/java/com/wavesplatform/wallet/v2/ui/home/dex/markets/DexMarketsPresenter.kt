/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.dex.markets

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.deleteAll
import com.vicpin.krealmextensions.queryAll
import com.vicpin.krealmextensions.saveAll
import com.wavesplatform.sdk.model.response.data.AssetInfoResponse
import com.wavesplatform.sdk.model.response.data.SearchPairResponse
import com.wavesplatform.sdk.model.response.matcher.MarketResponse
import com.wavesplatform.sdk.utils.RxUtil
import com.wavesplatform.wallet.v2.data.model.db.userdb.MarketResponseDb
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import io.reactivex.Observable
import javax.inject.Inject

@InjectViewState
class DexMarketsPresenter @Inject constructor() : BasePresenter<DexMarketsView>() {
    var needToUpdate: Boolean = false

    fun search(query: String) {

        fun getObservableLoadPairs(char: Char): Observable<SearchPairResponse> {
            val searchByAssets = query.split(char).filter { it.trim().isNotEmpty() }
            return if (searchByAssets.size > 1) {
                dataServiceManager.loadPairs(searchByAssets = searchByAssets)
            } else {
                dataServiceManager.loadPairs(searchByAsset = searchByAssets[0])
            }
        }

        if (query.isEmpty()) {
            return
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
                        assetIds.add(it.amountAsset)
                        assetIds.add(it.priceAsset)
                    }
                    dataServiceManager.assetsInfoByIds(assetIds.toList())
                }
                .flatMap {
                    Observable.just(createMarkets(searchResult, it))
                }
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({
                    viewState.afterSuccessGetMarkets(it)
                }, {
                    viewState.afterFailGetMarkets()
                    it.printStackTrace()
                }))
    }


    private fun createMarkets(searchResult: SearchPairResponse?, assets: List<AssetInfoResponse>)
            : MutableList<MarketResponse> {

        fun find(assetId: String): AssetInfoResponse? {
            assets.forEach {
                if (it.id == assetId) {
                    return it
                }
            }
            return null
        }

        val marketList = mutableListOf<MarketResponse>()
        val savedMarkets = queryAll<MarketResponseDb>()

        searchResult?.data?.forEach { data ->
            val market = MarketResponse()
            val amountAsset = find(data.amountAsset)
            val priceAsset = find(data.priceAsset)

            market.id = data.amountAsset + data.priceAsset

            market.amountAsset = data.amountAsset
            market.priceAsset = data.priceAsset

            market.amountAssetLongName = amountAsset?.name
            market.priceAssetLongName = priceAsset?.name

            market.amountAssetShortName = amountAsset?.ticker ?: amountAsset?.name
            market.priceAssetShortName = priceAsset?.ticker ?: priceAsset?.name

            market.amountAssetDecimals = amountAsset?.precision ?: 8
            market.priceAssetDecimals = priceAsset?.precision ?: 8

            market.checked = savedMarkets.firstOrNull { it.id == market.id } != null

            marketList.add(market)
        }

        return marketList
    }

    fun saveSelectedMarkets(data: List<MarketResponse>) {
        deleteAll<MarketResponseDb>()
        val selectedMarkets = data.filter { it.checked }
        MarketResponseDb.convertToDb(selectedMarkets).saveAll()
    }
}
