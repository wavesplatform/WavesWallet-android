package com.wavesplatform.wallet.v2.data.remote

import com.wavesplatform.wallet.v2.data.model.remote.response.*
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("v0/aliases")
    fun aliases(@Query("address") address: String?): Observable<AliasesResponse>

    @GET("v0/aliases/{alias}")
    fun alias(@Path("alias") alias: String?): Observable<AliasData>

    @GET("v0/assets")
    fun assetsInfoByIds(@Query("ids") ids: List<String?>): Observable<AssetsInfoResponse>

    @GET("v0/pairs/{amountAsset}/{priceAsset}")
    fun loadDexPairInfo(@Path("amountAsset") amountAsset: String?, @Path("priceAsset") priceAsset: String?): Observable<PairResponse>

    @GET("v0/transactions/exchange")
    fun loadLastTradesByPair(
        @Query("amountAsset") amountAsset: String?,
        @Query("priceAsset") priceAsset: String?,
        @Query("limit") limit: Int
    ): Observable<LastTradesResponse>

    @GET("candles/{amountAsset}/{priceAsset}")
    fun loadCandles(
        @Path("amountAsset") amountAsset: String?,
        @Path("priceAsset") priceAsset: String?,
        @Query("interval") timeframe: String,
        @Query("timeStart") from: Long,
        @Query("timeEnd") timeEnd: Long
    ): Observable<CandlesResponse>
}
