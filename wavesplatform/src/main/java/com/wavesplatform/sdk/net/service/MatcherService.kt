package com.wavesplatform.sdk.net.service

import com.google.gson.internal.LinkedTreeMap
import com.wavesplatform.sdk.net.model.request.CancelOrderRequest
import com.wavesplatform.sdk.net.model.request.OrderRequest
import com.wavesplatform.sdk.net.model.response.Markets
import com.wavesplatform.sdk.net.model.response.OrderBook
import com.wavesplatform.sdk.net.model.response.OrderResponse
import io.reactivex.Observable
import retrofit2.http.*

interface MatcherService {

    @GET("matcher/balance/reserved/{publicKey}")
    fun loadReservedBalances(
        @Path("publicKey") publicKey: String?,
        @Header("Timestamp") timestamp: Long,
        @Header("Signature") signature: String
    ): Observable<Map<String, Long>>

    @GET("matcher/orderbook")
    fun getAllMarkets(): Observable<Markets>

    @GET("matcher/orderbook/{amountAsset}/{priceAsset}/tradableBalance/{address}")
    fun getBalanceFromAssetPair(
        @Path("amountAsset") amountAsset: String?,
        @Path("priceAsset") priceAsset: String?,
        @Path("address") address: String?
    ): Observable<LinkedTreeMap<String, Long>>

    @GET("matcher/orderbook/{amountAsset}/{priceAsset}")
    fun getOrderBook(
        @Path("amountAsset") amountAsset: String?,
        @Path("priceAsset") priceAsset: String?
    ): Observable<OrderBook>

    @GET("matcher/orderbook/{amountAsset}/{priceAsset}/publicKey/{publicKey}")
    fun getMyOrders(
        @Path("amountAsset") amountAsset: String?,
        @Path("priceAsset") priceAsset: String?,
        @Path("publicKey") publicKey: String?,
        @Header("signature") signature: String?,
        @Header("timestamp") timestamp: Long
    ): Observable<List<OrderResponse>>

    @POST("matcher/orderbook/{amountAsset}/{priceAsset}/cancel")
    fun cancelOrder(
        @Path("amountAsset") amountAsset: String?,
        @Path("priceAsset") priceAsset: String?,
        @Body cancelOrderRequest: CancelOrderRequest?
    ): Observable<Any>

    @GET("matcher")
    fun getMatcherKey(): Observable<String>

    @POST("matcher/orderbook")
    fun placeOrder(@Body orderRequest: OrderRequest): Observable<Any>
}
