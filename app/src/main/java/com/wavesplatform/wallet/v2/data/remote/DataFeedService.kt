package com.wavesplatform.wallet.v2.data.remote

import com.wavesplatform.wallet.v1.payload.Candle
import com.wavesplatform.wallet.v1.payload.TickerMarket
import com.wavesplatform.wallet.v1.payload.TradesMarket
import com.wavesplatform.wallet.v2.data.model.remote.response.LastTrade

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path

interface DataFeedService {

    @GET("candles/{amountAsset}/{priceAsset}/{timeframe}/{from}/{to}")
    fun getCandlesOnInterval(@Path("amountAsset") amountAsset: String?,
                             @Path("priceAsset") priceAsset: String?,
                             @Path("timeframe") timeframe: Int,
                             @Path("from") from: Long,
                             @Path("to") to: Long): Observable<List<Candle>>

    @GET("candles/{amountAsset}/{priceAsset}/{timeframe}/{count}")
    fun getCandlesOnCount(@Path("amountAsset") amountAsset: String?,
                          @Path("priceAsset") priceAsset: String?,
                          @Path("timeframe") timeframe: Int,
                          @Path("count") count: Long): Observable<List<Candle>>

    @GET("trades/{amountAsset}/{priceAsset}/{limit}")
    fun getTradesByPair(@Path("amountAsset") amountAsset: String?, @Path("priceAsset") priceAsset: String?,
                        @Path("limit") limit: Int): Observable<List<LastTrade>>
}
