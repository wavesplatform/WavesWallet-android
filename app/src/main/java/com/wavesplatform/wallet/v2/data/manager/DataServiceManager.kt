/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.manager

import com.vicpin.krealmextensions.queryFirst
import com.vicpin.krealmextensions.save
import com.vicpin.krealmextensions.saveAll
import com.wavesplatform.sdk.model.request.data.PairRequest
import com.wavesplatform.sdk.model.response.data.*
import com.wavesplatform.sdk.model.response.node.transaction.AliasTransactionResponse
import com.wavesplatform.sdk.utils.notNull
import com.wavesplatform.wallet.v2.data.manager.base.BaseServiceManager
import com.wavesplatform.wallet.v2.data.model.db.AliasDb
import com.wavesplatform.wallet.v2.data.model.db.AssetInfoDb
import com.wavesplatform.wallet.v2.data.model.local.ChartTimeFrame
import com.wavesplatform.wallet.v2.ui.home.dex.trade.last_trades.TradeLastTradesPresenter.Companion.DEFAULT_LAST_TRADES_LIMIT
import com.wavesplatform.wallet.v2.util.EnvironmentManager
import com.wavesplatform.wallet.v2.util.PrefsUtil
import io.reactivex.Observable
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataServiceManager @Inject constructor() : BaseServiceManager() {

    fun loadAliases(): Observable<List<AliasTransactionResponse>> {
        return dataService.aliases(getAddress())
                .map {
                    val aliases = it.data.mapTo(ArrayList()) {
                        it.alias.own = true
                        return@mapTo it.alias
                    }
                    AliasDb.convertToDb(aliases).saveAll()
                    return@map aliases
                }
    }

    fun loadDexPairInfo(watchMarket: WatchMarketResponse): Observable<WatchMarketResponse> {
        return Observable.interval(0, 30, TimeUnit.SECONDS)
                .retry(3)
                .flatMap {
                    dataService.pairs(watchMarket.market.amountAsset, watchMarket.market.priceAsset)
                            .map {
                                prefsUtil.setValue(PrefsUtil.KEY_LAST_UPDATE_DEX_INFO, EnvironmentManager.getTime())
                                watchMarket.pairResponse = it
                                return@map watchMarket
                            }
                }
                .onErrorResumeNext(Observable.empty())
    }

    fun loadAlias(alias: String): Observable<AliasTransactionResponse> {
        val localAlias = queryFirst<AliasDb> { equalTo("alias", alias) }

        if (localAlias != null) {
            return Observable.just(localAlias.convertFromDb())
        } else {
            return dataService.alias(alias)
                    .map {
                        it.alias.own = false
                        AliasDb(it.alias).save()
                        return@map it.alias
                    }
        }
    }

    fun assets(ids: List<String?>? = null, search: String? = null, limit: Int? = null): Observable<List<AssetInfoResponse>> {
        if (ids != null && ids.isNotEmpty()
                || search != null && search.isNotEmpty()) {
            return dataService.assets(ids = ids, search = search, limit = limit)
                    .map { response ->
                        val assetsInfo = response.data.mapTo(ArrayList()) { assetInfoData ->
                            val defaultAsset = EnvironmentManager.defaultAssets.firstOrNull {
                                assetInfoData.assetInfo != null && it.assetId == assetInfoData.assetInfo.id
                            }

                            defaultAsset.notNull { assetBalance ->
                                assetBalance.getName().notNull {
                                    assetInfoData.assetInfo.name = it
                                }
                            }

                            return@mapTo assetInfoData.assetInfo
                        }
                        AssetInfoDb.convertToDb(assetsInfo).saveAll()
                        return@map assetsInfo
                    }
        } else {
            return Observable.just(listOf())
        }
    }

    fun getLastExchangesByPair(
            amountAsset: String?,
            priceAsset: String?,
            limit: Int): Observable<ArrayList<LastTradesResponse.DataResponse.ExchangeTransactionResponse>> {
        return dataService.transactionsExchange(amountAsset, priceAsset, limit)
                .map {
                    return@map it.data.mapTo(ArrayList()) { it.transaction }
                }
                .onErrorResumeNext(Observable.just(arrayListOf()))
    }

    fun loadCandles(
            watchMarket: WatchMarketResponse?,
            timeFrame: Int,
            from: Long,
            to: Long
    ): Observable<List<CandlesResponse.Data.CandleResponse>> {
        val interval = ChartTimeFrame.findByServerTime(timeFrame)
        return dataService.candles(watchMarket?.market?.amountAsset,
                watchMarket?.market?.priceAsset, interval.interval, from, to,
                EnvironmentManager.getMatcherAddress())
                .map { response ->
                    val candles = mutableListOf<CandlesResponse.Data.CandleResponse>()
                    response.data.forEach { candles.add(it.data) }
                    return@map candles.sortedBy { it.getTimeInMillis() }
                }
    }

    fun loadPairs(pairs: List<String>? = null,
                  searchByAsset: String? = null,
                  searchByAssets: List<String>? = null,
                  matchExactly: Boolean? = null,
                  limit: Int = 30
    ): Observable<SearchPairResponse> {
        return dataService.pairs(
                pairs,
                searchByAsset,
                searchByAssets,
                matchExactly,
                limit,
                EnvironmentManager.getMatcherAddress())
    }

    fun loadPairs(request: PairRequest): Observable<SearchPairResponse> {
        return dataService.pairs(request)
    }
}
