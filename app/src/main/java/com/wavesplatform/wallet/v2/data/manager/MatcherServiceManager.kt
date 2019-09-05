/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.manager

import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.gson.internal.LinkedTreeMap
import com.vicpin.krealmextensions.queryAllAsSingle
import com.wavesplatform.sdk.crypto.WavesCrypto
import com.wavesplatform.sdk.model.response.data.WatchMarketResponse
import com.wavesplatform.sdk.model.request.matcher.CancelOrderRequest
import com.wavesplatform.sdk.model.request.matcher.CreateOrderRequest
import com.wavesplatform.sdk.model.response.matcher.AssetPairOrderResponse
import com.wavesplatform.sdk.model.response.matcher.MarketResponse
import com.wavesplatform.sdk.model.response.matcher.MatcherSettingsResponse
import com.wavesplatform.sdk.model.response.matcher.OrderBookResponse
import com.wavesplatform.wallet.v2.util.EnvironmentManager
import com.wavesplatform.sdk.utils.notNull
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.Events
import com.wavesplatform.wallet.v2.data.manager.base.BaseServiceManager
import com.wavesplatform.wallet.v2.data.model.service.cofigs.GlobalConfigurationResponse
import com.wavesplatform.wallet.v2.data.model.db.userdb.MarketResponseDb
import com.wavesplatform.wallet.v2.data.model.db.SpamAssetDb
import com.wavesplatform.wallet.v2.data.model.service.cofigs.SpamAssetResponse
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatcherServiceManager @Inject constructor() : BaseServiceManager() {

    fun loadReservedBalances(): Observable<Map<String, Long>> {
        val timestamp = EnvironmentManager.getTime()
        var signature = ""
        App.getAccessManager().getWallet()?.privateKey.notNull { privateKey ->
            val bytes = Bytes.concat(WavesCrypto.base58decode(getPublicKeyStr()),
                    Longs.toByteArray(timestamp))
            signature = WavesCrypto.base58encode(
                    WavesCrypto.signBytesWithPrivateKey(bytes, WavesCrypto.base58encode(privateKey)))
        }
        return matcherService.balanceReserved(getPublicKeyStr(), timestamp, signature)
    }

    fun loadMyOrders(watchMarket: WatchMarketResponse?): Observable<List<AssetPairOrderResponse>> {
        val timestamp = EnvironmentManager.getTime()
        var signature = ""
        App.getAccessManager().getWallet()?.privateKey.notNull { privateKey ->
            val bytes = Bytes.concat(WavesCrypto.base58decode(getPublicKeyStr()),
                    Longs.toByteArray(timestamp))
            signature = WavesCrypto.base58encode(
                    WavesCrypto.signBytesWithPrivateKey(bytes, WavesCrypto.base58encode(privateKey)))
        }
        return matcherService.myOrders(watchMarket?.market?.amountAsset, watchMarket?.market?.priceAsset, getPublicKeyStr(), signature, timestamp)
    }

    fun loadOrderBook(amountAssetId: String, priceAssetId: String?): Observable<OrderBookResponse> {
        return matcherService.orderBook(amountAssetId, priceAssetId)
    }

    fun cancelOrder(orderId: String?, amountAsset: String?, priceAsset: String?): Observable<Any> {
        val request = CancelOrderRequest()
        request.sender = getPublicKeyStr()
        request.orderId = orderId ?: ""
        App.getAccessManager().getWallet()?.privateKey.notNull {
            request.sign(it)
        }
        return matcherService.cancelOrder(amountAsset, priceAsset, request)
                .doOnNext {
                    rxEventBus.post(Events.UpdateAssetsBalance())
                }
    }

    fun getBalanceFromAssetPair(watchMarket: WatchMarketResponse?): Observable<LinkedTreeMap<String, Long>> {
        return matcherService.orderBookTradableBalance(watchMarket?.market?.amountAsset, watchMarket?.market?.priceAsset, getAddress())
    }

    fun getMatcherKey(): Observable<String> {
        return matcherService.matcherPublicKey()
    }

    fun placeOrder(orderRequest: CreateOrderRequest): Observable<Any> {
        orderRequest.senderPublicKey = getPublicKeyStr()
        App.getAccessManager().getWallet()?.privateKey.notNull { privateKey ->
            orderRequest.sign(privateKey)
        }
        return matcherService.createOrder(orderRequest)
                .doOnNext {
                    rxEventBus.post(Events.UpdateAssetsBalance())
                }
    }

    fun getSettings(): Observable<MatcherSettingsResponse> {
        return matcherService.getMatcherSettings()
    }

    fun getSettingsRates(): Observable<MutableMap<String, Double>> {
        return matcherService.getMatcherSettingsRates()
    }
}
