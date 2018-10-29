package com.wavesplatform.wallet.v2.data.manager

import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.vicpin.krealmextensions.queryAllAsSingle
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v1.crypto.Base58
import com.wavesplatform.wallet.v1.crypto.CryptoProvider
import com.wavesplatform.wallet.v1.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.manager.base.BaseDataManager
import com.wavesplatform.wallet.v2.data.model.remote.response.GlobalConfiguration
import com.wavesplatform.wallet.v2.data.model.remote.response.MarketResponse
import com.wavesplatform.wallet.v2.data.model.remote.response.Markets
import com.wavesplatform.wallet.v2.data.model.remote.response.SpamAsset
import com.wavesplatform.wallet.v2.util.isWaves
import com.wavesplatform.wallet.v2.util.notNull
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import pers.victor.ext.currentTimeMillis
import javax.inject.Inject

class MatcherDataManager @Inject constructor() : BaseDataManager() {

    fun loadReservedBalances(): Observable<Map<String, Long>> {
        val timestamp = currentTimeMillis
        var signature = ""
        App.getAccessManager().getWallet()?.privateKey.notNull { privateKey ->
            val bytes = Bytes.concat(Base58.decode(getPublicKeyStr()),
                    Longs.toByteArray(timestamp))
            signature = Base58.encode(CryptoProvider.sign(privateKey, bytes))
        }
        return matcherService.loadReservedBalances(getPublicKeyStr(), timestamp, signature)
    }

    fun getAllMarkets(): Observable<MutableList<MarketResponse>> {
        return Observable.zip(apiService.loadGlobalConfigurate(),
                Observable.zip(matcherService.getAllMarkets(), queryAllAsSingle<SpamAsset>().toObservable(), BiFunction { apiMarkets: Markets, spamAssets: List<SpamAsset> ->
                    val filteredSpamList = if (prefsUtil.getValue(PrefsUtil.KEY_DISABLE_SPAM_FILTER, false)) {
                        apiMarkets.markets
                    } else {
                        apiMarkets.markets.filter { market -> !spamAssets.any { it.assetId == market.priceAsset || it.assetId == market.amountAsset } }
                    }
                    return@BiFunction filteredSpamList
                }),
                queryAllAsSingle<MarketResponse>().toObservable(),
                Function3 { configure: GlobalConfiguration, apiMarkets: List<MarketResponse>, dbMarkets: List<MarketResponse> ->
                    return@Function3 Triple(configure, apiMarkets, dbMarkets)
                })
                .map {
                    it.second.forEach { market ->
                        market.id = market.amountAsset + market.priceAsset

                        market.amountAssetLongName = it.first.generalAssetIds.firstOrNull { it.assetId == market.amountAsset }?.displayName ?: market.amountAssetName
                        market.priceAssetLongName = it.first.generalAssetIds.firstOrNull { it.assetId == market.priceAsset }?.displayName ?: market.priceAssetName

                        market.amountAssetShortName = it.first.generalAssetIds.firstOrNull { it.assetId == market.amountAsset }?.gatewayId ?: market.amountAssetName
                        market.priceAssetShortName = it.first.generalAssetIds.firstOrNull { it.assetId == market.priceAsset }?.gatewayId ?: market.priceAssetName

                        market.popular = isPopularAsset(market.amountAsset, it.first.generalAssetIds) && isPopularAsset(market.priceAsset, it.first.generalAssetIds)

                        market.amountAssetDecimals = market.amountAssetInfo.decimals
                        market.priceAssetDecimals = market.priceAssetInfo.decimals

                        market.checked = it.third.any { it.id == market.id }
                    }
                    val list = sortByGatewayAssets(it.first.generalAssetIds, it.first.generalAssetIds.lastIndex, it.second).toMutableList()
                    return@map list
                }
    }

    private fun sortByGatewayAssets(generalAssets: List<GlobalConfiguration.GeneralAssetId>, position: Int, markets: List<MarketResponse>): List<MarketResponse> {
        if (position < 0) {
            return markets
        }
        val sortedList = markets.sortedByDescending { it.popular && it.amountAsset == generalAssets[position].assetId }
        return sortByGatewayAssets(generalAssets, position - 1, sortedList)
    }

    private fun isPopularAsset(asset: String, generalAssetIds: List<GlobalConfiguration.GeneralAssetId>): Boolean {
        if (asset.isWaves()) {
            return true
        }
        return generalAssetIds.any { it.assetId == asset }
    }
}
