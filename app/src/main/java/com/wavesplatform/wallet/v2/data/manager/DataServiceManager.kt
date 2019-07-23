/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.manager

import com.vicpin.krealmextensions.queryFirst
import com.vicpin.krealmextensions.save
import com.vicpin.krealmextensions.saveAll
import com.wavesplatform.sdk.model.response.data.*
import com.wavesplatform.sdk.model.response.node.transaction.AliasTransactionResponse
import com.wavesplatform.wallet.v2.util.EnvironmentManager
import com.wavesplatform.sdk.utils.notNull
import com.wavesplatform.wallet.v2.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.manager.base.BaseServiceManager
import com.wavesplatform.wallet.v2.data.model.db.AliasDb
import com.wavesplatform.wallet.v2.data.model.db.AssetInfoDb
import io.reactivex.Observable
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataServiceManager @Inject constructor() : BaseServiceManager() {

    companion object {
        var DEFAULT_LAST_TRADES_LIMIT = 50
    }

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

    fun assetsInfoByIds(ids: List<String?>): Observable<List<AssetInfoResponse>> {
        if (ids.isEmpty()) {
            return Observable.just(listOf())
        } else {
            return dataService.assets(ids)
                    .map { response ->
                        val assetsInfo = response.data.mapTo(ArrayList()) { assetInfoData ->
                            val defaultAsset = EnvironmentManager.defaultAssets.firstOrNull {
                                it.assetId == assetInfoData.assetInfo.id
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
        }
    }

    fun loadLastTradesByPair(watchMarket: WatchMarketResponse?): Observable<ArrayList<LastTradesResponse.DataResponse.ExchangeTransactionResponse>> {
        return dataService.transactionsExchange(watchMarket?.market?.amountAsset, watchMarket?.market?.priceAsset, DEFAULT_LAST_TRADES_LIMIT)
                .map {
                    return@map it.data.mapTo(ArrayList()) { it.transaction }
                }
    }

    fun getLastTradeByPair(watchMarket: WatchMarketResponse?): Observable<ArrayList<LastTradesResponse.DataResponse.ExchangeTransactionResponse>> {
        return dataService.transactionsExchange(watchMarket?.market?.amountAsset, watchMarket?.market?.priceAsset, 1)
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
    ): Observable<List<CandlesResponse.CandleResponse>> {
        return dataService.candles(watchMarket?.market?.amountAsset,
                watchMarket?.market?.priceAsset, "${timeFrame}m", from, to)
                .map {
                    return@map it.candles.sortedBy { it.time }
                }
    }

    fun loadPairs(pairs: String? = null,
                  searchByAsset: String? = null,
                  searchByAssets: List<String>? = null,
                  matchExactly: Boolean? = null,
                  limit: Int = 30
    ): Observable<SearchPairResponse> {
        return dataService.pairs(pairs, searchByAsset, searchByAssets, matchExactly, limit)
    }
}
