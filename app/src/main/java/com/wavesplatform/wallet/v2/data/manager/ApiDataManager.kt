/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.manager

import com.vicpin.krealmextensions.queryFirst
import com.vicpin.krealmextensions.save
import com.vicpin.krealmextensions.saveAll
import com.wavesplatform.sdk.model.response.WatchMarketResponse
import com.wavesplatform.sdk.model.response.AliasResponse
import com.wavesplatform.sdk.model.response.AssetInfoResponse
import com.wavesplatform.sdk.model.response.CandlesResponse
import com.wavesplatform.sdk.model.response.LastTradesResponse
import com.wavesplatform.wallet.v2.util.EnvironmentManager
import com.wavesplatform.sdk.utils.notNull
import com.wavesplatform.wallet.v2.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.manager.base.BaseDataManager
import com.wavesplatform.wallet.v2.data.model.db.AliasDb
import com.wavesplatform.wallet.v2.data.model.db.AssetInfoDb
import io.reactivex.Observable
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiDataManager @Inject constructor() : BaseDataManager() {

    companion object {
        var DEFAULT_LAST_TRADES_LIMIT = 50
    }

    fun loadAliases(): Observable<List<AliasResponse>> {
        return apiService.aliases(getAddress())
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
                    apiService.loadDexPairInfo(watchMarket.market.amountAsset, watchMarket.market.priceAsset)
                            .map {
                                prefsUtil.setValue(PrefsUtil.KEY_LAST_UPDATE_DEX_INFO, EnvironmentManager.getTime())
                                watchMarket.pairResponse = it
                                return@map watchMarket
                            }
                }
                .onErrorResumeNext(Observable.empty())
    }

    fun loadAlias(alias: String): Observable<AliasResponse> {
        val localAlias = queryFirst<AliasDb> { equalTo("alias", alias) }

        if (localAlias != null) {
            return Observable.just(localAlias.convertFromDb())
        } else {
            return apiService.alias(alias)
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
            return apiService.assetsInfoByIds(ids)
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
        return apiService.loadLastTradesByPair(watchMarket?.market?.amountAsset, watchMarket?.market?.priceAsset, DEFAULT_LAST_TRADES_LIMIT)
                .map {
                    return@map it.data.mapTo(ArrayList()) { it.transaction }
                }
    }

    fun getLastTradeByPair(watchMarket: WatchMarketResponse?): Observable<ArrayList<LastTradesResponse.DataResponse.ExchangeTransactionResponse>> {
        return apiService.loadLastTradesByPair(watchMarket?.market?.amountAsset, watchMarket?.market?.priceAsset, 1)
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
        return apiService.loadCandles(watchMarket?.market?.amountAsset,
                watchMarket?.market?.priceAsset, "${timeFrame}m", from, to)
                .map {
                    return@map it.candles.sortedBy { it.time }
                }
    }
}
