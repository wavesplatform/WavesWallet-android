/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.net.service

import com.wavesplatform.sdk.model.response.*
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * The goal of this service is to provide a simple
 * and convenient way to get data from Waves blockchain.
 * For more information: [WavesPlatform API Swagger]({https://api.wavesplatform.com/v0/docs/)
 */
interface ApiService {

    /**
     * Get a list of aliases for a given address
     */
    @GET("v0/aliases")
    fun aliases(@Query("address") address: String?): Observable<AliasesResponse>

    /**
     * Get address for alias
     */
    @GET("v0/aliases/{alias}")
    fun alias(@Path("alias") alias: String?): Observable<AliasDataResponse>

    /**
     * Get asset info by asset ID
     */
    @GET("v0/assets")
    fun assetsInfoByIds(@Query("ids") ids: List<String?>): Observable<AssetsInfoResponse>

    /**
     * Get pair info by amount and price assets
     */
    @GET("v0/pairs/{amountAsset}/{priceAsset}")
    fun loadDexPairInfo(@Path("amountAsset") amountAsset: String?,
                        @Path("priceAsset") priceAsset: String?): Observable<PairResponse>

    /**
     * Get a list of exchange transactions by applying filters
     */
    @GET("v0/transactions/exchange")
    fun loadLastTradesByPair(
        @Query("amountAsset") amountAsset: String?,
        @Query("priceAsset") priceAsset: String?,
        @Query("limit") limit: Int
    ): Observable<LastTradesResponse>

    /**
     * Get candles by amount and price assets. Maximum amount of candles in response – 1440.
     */
    @GET("candles/{amountAsset}/{priceAsset}")
    fun loadCandles(
        @Path("amountAsset") amountAsset: String?,
        @Path("priceAsset") priceAsset: String?,
        @Query("interval") timeframe: String,
        @Query("timeStart") from: Long,
        @Query("timeEnd") timeEnd: Long
    ): Observable<CandlesResponse>
}
