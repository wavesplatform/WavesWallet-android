package com.wavesplatform.wallet.v2.data.manager

import android.util.Log
import com.vicpin.krealmextensions.queryFirst
import com.vicpin.krealmextensions.save
import com.vicpin.krealmextensions.saveAll
import com.wavesplatform.wallet.v1.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.manager.base.BaseDataManager
import com.wavesplatform.wallet.v2.data.model.local.WatchMarket
import com.wavesplatform.wallet.v2.data.model.remote.response.Alias
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetInfo
import com.wavesplatform.wallet.v2.data.model.remote.response.LastTradesResponse
import com.wavesplatform.wallet.v2.util.notNull
import io.reactivex.Observable
import pers.victor.ext.currentTimeMillis
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiDataManager @Inject constructor() : BaseDataManager() {

    companion object {
        var DEFAULT_LAST_TRADES_LIMIT = 50
    }

    fun loadAliases(): Observable<List<Alias>> {
        return apiService.aliases(getAddress())
                .map {
                    val aliases = it.data.mapTo(ArrayList()) {
                        it.alias.own = true
                        return@mapTo it.alias
                    }
                    aliases.saveAll()
                    return@map aliases
                }
    }

    fun loadDexPairInfo(watchMarket: WatchMarket): Observable<WatchMarket> {
        return Observable.interval(0, 30, TimeUnit.SECONDS)
                .retry(3)
                .flatMap {
                    apiService.loadDexPairInfo(watchMarket.market.amountAsset, watchMarket.market.priceAsset)
                            .map {
                                prefsUtil.setValue(PrefsUtil.KEY_LAST_UPDATE_DEX_INFO, currentTimeMillis)
                                watchMarket.pairResponse = it
                                return@map watchMarket
                            }
                }
                .onErrorResumeNext(Observable.empty())
    }

    fun loadAlias(alias: String): Observable<Alias> {
        val localAlias = queryFirst<Alias> { equalTo("alias", alias) }

        if (localAlias != null) {
            return Observable.just(localAlias)
        } else {
            return apiService.alias(alias)
                    .map {
                        it.alias.own = false
                        it.alias.save()
                        return@map it.alias
                    }
        }
    }

    fun assetsInfoByIds(ids: List<String?>): Observable<List<AssetInfo>> {
        return apiService.assetsInfoByIds(ids)
                .map { it ->
                    val assetsInfo = it.data.mapTo(ArrayList()) { assetInfoData ->
                        val defaultAsset = Constants.defaultAssets.firstOrNull { it.assetId == assetInfoData.assetInfo.id }

                        defaultAsset.notNull { assetBalance ->
                            assetBalance.getName().notNull {
                                assetInfoData.assetInfo.name = it
                            }

                        }

                        return@mapTo assetInfoData.assetInfo
                    }
                    assetsInfo.saveAll()
                    return@map assetsInfo
                }
    }

    fun loadLastTradesByPair(watchMarket: WatchMarket?): Observable<ArrayList<LastTradesResponse.Data.ExchangeTransaction>> {
        return apiService.loadLastTradesByPair(watchMarket?.market?.amountAsset, watchMarket?.market?.priceAsset, DEFAULT_LAST_TRADES_LIMIT)
                .map {
                    return@map it.data.mapTo(ArrayList()) { it.transaction }
                }
    }
}
