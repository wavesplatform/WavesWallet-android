/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.dex.markets

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryAll
import com.wavesplatform.sdk.model.response.data.AssetInfoResponse
import com.wavesplatform.sdk.model.response.data.SearchPairResponse
import com.wavesplatform.sdk.model.response.matcher.MarketResponse
import com.wavesplatform.sdk.utils.RxUtil
import com.wavesplatform.wallet.v2.data.model.db.userdb.MarketResponseDb
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import io.reactivex.Observable
import pyxis.uzuki.live.richutilskt.utils.runAsync
import pyxis.uzuki.live.richutilskt.utils.runOnUiThread
import javax.inject.Inject

@InjectViewState
class DexMarketsPresenter @Inject constructor() : BasePresenter<DexMarketsView>() {
    var needToUpdate: Boolean = false

    fun initLoad() {
        runAsync {

            val WAVES = "WAVES"
            val USD = "Ft8X1v1LTa1ABafufpaCWyVj8KkaxUWE6xBhW6sNFJck"
            val BTC = "8LQW8f7P5d5PZM7GtZEBgaqRPGSzS3DfPuiXrURJ4AJS"

            val mainAssets = mutableListOf(WAVES, USD, BTC)

            val generalAssetIdList = mutableListOf("WAVES",
                    "Gtb1WRznfchDnTh37ezoDTJ4wcoKaRsKqKjJjy7nm2zU",
                    "Ft8X1v1LTa1ABafufpaCWyVj8KkaxUWE6xBhW6sNFJck", // USD
                    "8LQW8f7P5d5PZM7GtZEBgaqRPGSzS3DfPuiXrURJ4AJS", // BTC
                    "474jTeYx2r2Va35794tCScAXWJG9hU2HcgxzMowaZUnu",
                    "HZk1mbfuJpmxU1Fs4AX5MWLVYtctsNcg6e2C6VKqK8zk",
                    "BrjUWjndUanm5VsJkbUip8VRYy6LWJePtxya3FNv4TQa",
                    "zMFqXuoyrn5w17PFurTqxB7GsS71fp9dfk6XFwxbPCy",
                    "2mX5DzVKWrAJw8iwdJnV2qtoeVG9h5nTDpTqC1wb1WEN",
                    "B3uGHFRpSUuGEDWjqB9LWWxafQj8VTvpMucEyoxzws5H",
                    "725Yv9oceWsB4GsYwyy4A52kEwyVrL5avubkeChSnL46",
                    "AxAmJaro7BJ4KasYiZhw7HkjwgYtt2nekPuF2CN9LMym",
                    "5WvPKSJXzVE2orvbkJ8wsQmmQKqTv9sGBPksV4adViw3",
                    "DHgwrRvVyqJsepd32YbBqUeDH4GJ1N984X8QoekjgH8J",
                    "4uK8i4ThRGbehENwa6MxyLtxAjAo1Rj9fduborGExarC",
                    "7FzrHF1pueRFrPEupz6oiVGTUZqe8epvC7ggWUx8n1bd",
                    "4LHHvYGNKJUg5hj65aGD5vgScvCBmLpdRFtjokvCjSL8")

            val initPairsList = mutableListOf<String>()
            for (amountAssetId in mainAssets) {
                for (priceAssetId in generalAssetIdList) {
                    if (amountAssetId != priceAssetId) {
                        initPairsList.add("$amountAssetId/$priceAssetId")
                    }
                }
            }

            var searchResult: SearchPairResponse? = null
            val assetIds = hashSetOf<String>()

            addSubscription(dataServiceManager.loadPairs(pairs = initPairsList)
                    .flatMap { result ->
                        for (index in 0 until result.data.size) {
                            if (result.data[index].data != null) {
                                val pair = initPairsList[index].split("/")
                                assetIds.add(pair[0])
                                assetIds.add(pair[1])
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
}
