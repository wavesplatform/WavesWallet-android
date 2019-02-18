package com.wavesplatform.sdk.service

import com.wavesplatform.sdk.Constants
import com.wavesplatform.sdk.model.response.*
import io.reactivex.Observable
import ren.yale.android.retrofitcachelibrx2.RetrofitCache
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface ApiService {

    @GET("v0/aliases")
    fun aliases(@Query("address") address: String?): Observable<AliasesResponse>

    @GET("v0/aliases/{alias}")
    fun alias(@Path("alias") alias: String?): Observable<AliasData>

    @GET("v0/assets")
    fun assetsInfoByIds(@Query("ids") ids: List<String?>): Observable<AssetsInfoResponse>

    @GET
    fun loadGlobalConfiguration(@Url url: String = Constants.URL_CONFIG): Observable<GlobalConfiguration>

    @GET
    fun loadNews(@Url url: String): Observable<News>

    @GET
    fun loadGlobalCommission(@Url url: String = Constants.URL_COMMISSION)
            : Observable<GlobalTransactionCommission>

    @GET("v0/pairs/{amountAsset}/{priceAsset}")
    fun loadDexPairInfo(@Path("amountAsset") amountAsset: String?,
                        @Path("priceAsset") priceAsset: String?): Observable<PairResponse>

    @GET("v0/transactions/exchange")
    fun loadLastTradesByPair(@Query("amountAsset") amountAsset: String?,
                             @Query("priceAsset") priceAsset: String?,
                             @Query("limit") limit: Int): Observable<LastTradesResponse>

    @GET("candles/{amountAsset}/{priceAsset}")
    fun loadCandles(@Path("amountAsset") amountAsset: String?,
                    @Path("priceAsset") priceAsset: String?,
                    @Query("interval") timeFrame: String,
                    @Query("timeStart") from: Long,
                    @Query("timeEnd") timeEnd: Long): Observable<CandlesResponse>
}
