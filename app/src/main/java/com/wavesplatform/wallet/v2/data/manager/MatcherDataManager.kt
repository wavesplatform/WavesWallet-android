package com.wavesplatform.wallet.v2.data.manager

import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v1.crypto.Base58
import com.wavesplatform.wallet.v1.crypto.CryptoProvider
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.manager.base.BaseDataManager
import com.wavesplatform.wallet.v2.data.model.remote.response.Market
import com.wavesplatform.wallet.v2.data.model.remote.response.Markets
import com.wavesplatform.wallet.v2.util.notNull
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
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

    fun getAllMarkets(): Observable<MutableList<Market>> {
        return Observable.zip(apiService.loadVerifiedAssetsWithShortName(), matcherService.getAllMarkets(), BiFunction { t1: Map<String, String>, t2: Markets ->
            return@BiFunction Pair(t1, t2)
        }).map {
            val markets = it.second.markets
            markets.forEach { market ->
                market.id = market.amountAsset + market.priceAsset

                market.amountAssetLongName = Constants.defaultAssets.firstOrNull { it.assetId == market.amountAsset }?.getName() ?: market.amountAssetName
                market.priceAssetLongName = Constants.defaultAssets.firstOrNull { it.assetId == market.priceAsset }?.getName() ?: market.priceAssetName

                market.amountAssetShortName = it.first[market.amountAsset] ?: market.amountAssetName
                market.priceAssetShortName = it.first[market.priceAsset] ?: market.priceAssetName

                market.popular = isPopularAmountAsset(market) && isPopularPriceAsset(market)
            }
            return@map markets.toMutableList()
        }
    }

    private fun isPopularPriceAsset(market: Market): Boolean {
        if (market.priceAsset.toLowerCase() == Constants.wavesAssetInfo.name.toLowerCase()) {
            return true
        }
        return Constants.defaultAssets.any { it.assetId == market.priceAsset }
    }


    private fun isPopularAmountAsset(market: Market): Boolean {
        if (market.amountAsset.toLowerCase() == Constants.wavesAssetInfo.name.toLowerCase()) {
            return true
        }
        return Constants.defaultAssets.any { it.assetId == market.amountAsset }
    }
}
