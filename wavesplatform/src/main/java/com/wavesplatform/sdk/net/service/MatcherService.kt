/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.net.service

import com.google.gson.internal.LinkedTreeMap
import com.wavesplatform.sdk.net.model.request.CancelOrderRequest
import com.wavesplatform.sdk.net.model.request.OrderRequest
import com.wavesplatform.sdk.net.model.response.Markets
import com.wavesplatform.sdk.net.model.response.OrderBook
import com.wavesplatform.sdk.net.model.response.OrderResponse
import io.reactivex.Observable
import retrofit2.http.*

/**
 * Matcher Service for DEX
 * For more information: [Matcher Swagger]({https://matcher.wavesplatform.com/api-docs/index.html#/matcher)
 */
interface MatcherService {

    /**
     * Get non-zero balance of open orders
     */
    @GET("matcher/balance/reserved/{publicKey}")
    fun loadReservedBalances(
        @Path("publicKey") publicKey: String?,
        @Header("Timestamp") timestamp: Long,
        @Header("Signature") signature: String
    ): Observable<Map<String, Long>>

    /**
     * Get the open trading markets along with trading pairs meta data
     */
    @GET("matcher/orderbook")
    fun getAllMarkets(): Observable<Markets>

    @GET("matcher/orderbook/{amountAsset}/{priceAsset}/tradableBalance/{address}")
    fun getBalanceFromAssetPair(
        @Path("amountAsset") amountAsset: String?,
        @Path("priceAsset") priceAsset: String?,
        @Path("address") address: String?
    ): Observable<LinkedTreeMap<String, Long>>

    /**
     * Get Order Book for a given Asset Pair
     */
    @GET("matcher/orderbook/{amountAsset}/{priceAsset}")
    fun getOrderBook(
        @Path("amountAsset") amountAsset: String?,
        @Path("priceAsset") priceAsset: String?
    ): Observable<OrderBook>

    /**
     * Get Order History for a given Asset Pair and Public Key
     */
    @GET("matcher/orderbook/{amountAsset}/{priceAsset}/publicKey/{publicKey}")
    fun getMyOrders(
        @Path("amountAsset") amountAsset: String?,
        @Path("priceAsset") priceAsset: String?,
        @Path("publicKey") publicKey: String?,
        @Header("signature") signature: String?,
        @Header("timestamp") timestamp: Long
    ): Observable<List<OrderResponse>>

    /**
     * Cancel previously submitted order if it's not already filled completely
     */
    @POST("matcher/orderbook/{amountAsset}/{priceAsset}/cancel")
    fun cancelOrder(
        @Path("amountAsset") amountAsset: String?,
        @Path("priceAsset") priceAsset: String?,
        @Body cancelOrderRequest: CancelOrderRequest?
    ): Observable<Any>

    /**
     * Get matcher public key
     */
    @GET("matcher")
    fun getMatcherKey(): Observable<String>

    /**
     * Place a new limit order (buy or sell)
     */
    @POST("matcher/orderbook")
    fun placeOrder(@Body orderRequest: OrderRequest): Observable<Any>
}
