package com.wavesplatform.sdk.manager

import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.gson.internal.LinkedTreeMap
import com.wavesplatform.sdk.Constants
import com.wavesplatform.sdk.Wavesplatform
import com.wavesplatform.sdk.crypto.Base58
import com.wavesplatform.sdk.crypto.CryptoProvider
import com.wavesplatform.sdk.manager.base.BaseDataManager
import com.wavesplatform.sdk.model.WatchMarket
import com.wavesplatform.sdk.model.request.CancelOrderRequest
import com.wavesplatform.sdk.model.request.OrderRequest
import com.wavesplatform.sdk.model.response.*
import com.wavesplatform.sdk.utils.notNull
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import pers.victor.ext.currentTimeMillis
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatcherDataManager @Inject constructor() : BaseDataManager() {
    var allMarketsList = mutableListOf<MarketResponse>()

    fun loadReservedBalances(): Observable<Map<String, Long>> {
        val timestamp = currentTimeMillis
        var signature = ""
        Wavesplatform.get().getWallet().privateKey.notNull { privateKey ->
            val bytes = Bytes.concat(Base58.decode(Wavesplatform.get().getWallet().publicKeyStr),
                    Longs.toByteArray(timestamp))
            signature = Base58.encode(CryptoProvider.sign(privateKey, bytes))
        }
        return matcherService.loadReservedBalances(
                Wavesplatform.get().getWallet().publicKeyStr, timestamp, signature)
    }

    fun loadMyOrders(watchMarket: WatchMarket?): Observable<List<OrderResponse>> {
        val timestamp = currentTimeMillis
        var signature = ""
        Wavesplatform.get().getWallet().privateKey.notNull { privateKey ->
            val bytes = Bytes.concat(Base58.decode(Wavesplatform.get().getWallet().publicKeyStr),
                    Longs.toByteArray(timestamp))
            signature = Base58.encode(CryptoProvider.sign(privateKey, bytes))
        }
        return matcherService.getMyOrders(
                watchMarket?.market?.amountAsset,
                watchMarket?.market?.priceAsset,
                Wavesplatform.get().getWallet().publicKeyStr,
                signature,
                timestamp)
    }

    fun loadOrderBook(watchMarket: WatchMarket?): Observable<OrderBook> {
        return matcherService.getOrderBook(
                watchMarket?.market?.amountAsset,
                watchMarket?.market?.priceAsset)
    }

    fun cancelOrder(orderId: String,
                    watchMarket: WatchMarket?,
                    cancelOrderRequest: CancelOrderRequest): Observable<Any> {
        cancelOrderRequest.sender = Wavesplatform.get().getWallet().publicKeyStr
        cancelOrderRequest.orderId = orderId
        Wavesplatform.get().getWallet().privateKey.notNull {
            cancelOrderRequest.sign(it)
        }
        return matcherService.cancelOrder(
                watchMarket?.market?.amountAsset,
                watchMarket?.market?.priceAsset, cancelOrderRequest)
                /*.doOnNext {
                    rxEventBus.post(Events.UpdateAssetsBalance())
                }*/
    }

    fun getBalanceFromAssetPair(watchMarket: WatchMarket?): Observable<LinkedTreeMap<String, Long>> {
        return matcherService.getBalanceFromAssetPair(
                watchMarket?.market?.amountAsset,
                watchMarket?.market?.priceAsset,
                Wavesplatform.get().getWallet().address)
    }

    fun getMatcherKey(): Observable<String> {
        return matcherService.getMatcherKey()
    }

    fun placeOrder(orderRequest: OrderRequest): Observable<Any> {
        orderRequest.senderPublicKey = Wavesplatform.get().getWallet().publicKeyStr
        Wavesplatform.get().getWallet().privateKey.notNull { privateKey ->
            orderRequest.sign(privateKey)
        }
        return matcherService.placeOrder(orderRequest)
                /*.doOnNext {
                    rxEventBus.post(Events.UpdateAssetsBalance())
                }*/
    }


    fun getAllMarkets(): Observable<MutableList<MarketResponse>> {
        if (allMarketsList.isEmpty()) {
            return Observable.zip(apiService.loadGlobalConfiguration()
                    .map {
                        val globalAssets = it.generalAssetIds.toMutableList()
                        globalAssets.add(Constants.MRTGeneralAsset)
                        globalAssets.add(Constants.WCTGeneralAsset)
                        return@map globalAssets.associateBy { it.assetId }
                    },
                    matcherService.getAllMarkets()
                            .map { it.markets },
                    BiFunction { configure: Map<String, GlobalConfiguration.GeneralAssetId>,
                                 apiMarkets: List<MarketResponse> ->
                        return@BiFunction Pair(configure, apiMarkets)
                    })
                    .flatMap {
                        // configure hash groups
                        val hashGroup = linkedMapOf<String, MutableList<MarketResponse>>()

                        it.first.keys.forEach {
                            hashGroup[it] = mutableListOf()
                        }
                        hashGroup[OTHER_GROUP] = mutableListOf()

                        // fill information and sort by group
                        it.second.forEach { market ->
                            market.id = market.amountAsset + market.priceAsset

                            market.amountAssetLongName = it.first[market.amountAsset]?.displayName
                                    ?: market.amountAssetName
                            market.priceAssetLongName = it.first[market.priceAsset]?.displayName
                                    ?: market.priceAssetName

                            market.amountAssetShortName = it.first[market.amountAsset]?.gatewayId
                                    ?: market.amountAssetName
                            market.priceAssetShortName = it.first[market.priceAsset]?.gatewayId
                                    ?: market.priceAssetName

                            market.popular = it.first[market.amountAsset] != null
                                    && it.first[market.priceAsset] != null

                            market.amountAssetDecimals = market.amountAssetInfo.decimals
                            market.priceAssetDecimals = market.priceAssetInfo.decimals

                            val group = hashGroup[market.amountAsset]
                            if (group != null) {
                                group.add(market)
                            } else {
                                hashGroup[OTHER_GROUP]?.add(market)
                            }
                        }

                        hashGroup.values.forEach {
                            allMarketsList.addAll(it)
                        }

                        //return@flatMap filterMarketsBySpamAndSelect(allMarketsList)
                        return@flatMap Observable.just(allMarketsList)

                    }
        } else {
            // return filterMarketsBySpamAndSelect(allMarketsList)
            return Observable.just(allMarketsList)
        }
    }

    fun getGlobalCommission(): Observable<GlobalTransactionCommission> {
        return apiService.loadGlobalCommission()
    }

    /*private fun filterMarketsBySpamAndSelect(markets: List<MarketResponse>): Observable<MutableList<MarketResponse>> {
        return Observable.zip(
                Observable.just(markets),
                queryAllAsSingle<SpamAsset>().toObservable()
                .map {
                    val map = it.associateBy { it.assetId }
                    return@map map
                },
                queryAllAsSingle<MarketResponse>().toObservable()
                        .map {
                            val map = it.associateBy { it.id }
                            return@map map
                        }
                , Function3 { apiMarkets: List<MarketResponse>, spamAssets: Map<String?, SpamAsset>, dbMarkets: Map<String?, MarketResponse> ->
            val filteredSpamList = if (prefsUtil.getValue(PrefsUtil.KEY_ENABLE_SPAM_FILTER, true)) {
                apiMarkets.filter { market -> spamAssets[market.amountAsset] == null && spamAssets[market.priceAsset] == null }
            } else {
                apiMarkets.toMutableList()
            }.toMutableList()

            filteredSpamList.forEach { market ->
                market.checked = dbMarkets[market.id] != null
            }

            return@Function3 filteredSpamList
        })
    }*/

    companion object {
        var OTHER_GROUP = "other"
    }
}
