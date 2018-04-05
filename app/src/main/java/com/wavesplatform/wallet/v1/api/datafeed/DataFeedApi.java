package com.wavesplatform.wallet.v1.api.datafeed;

import com.wavesplatform.wallet.v1.payload.Candle;
import com.wavesplatform.wallet.v1.payload.TickerMarket;
import com.wavesplatform.wallet.v1.payload.TradesMarket;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface DataFeedApi {

    @GET("candles/{amountAsset}/{priceAsset}/{timeframe}/{from}/{to}")
    Observable<List<Candle>> getCandlesOnInterval(@Path("amountAsset") final String amountAsset,
                                                  @Path("priceAsset") final String priceAsset,
                                                  @Path("timeframe") final int timeframe,
                                                  @Path("from") final long from,
                                                  @Path("to") final long to);

    @GET("candles/{amountAsset}/{priceAsset}/{timeframe}/{count}")
    Observable<List<Candle>> getCandlesOnCount(@Path("amountAsset") final String amountAsset,
                                                  @Path("priceAsset") final String priceAsset,
                                                  @Path("timeframe") final int timeframe,
                                                  @Path("count") final long count);

    @GET("ticker/{amountAsset}/{priceAsset}")
    Observable<TickerMarket> getTickerByPair(@Path("amountAsset") final String amountAsset, @Path("priceAsset") final String priceAsset);


    @GET("trades/{amountAsset}/{priceAsset}/{limit}")
    Observable<List<TradesMarket>> getTradesByPair(@Path("amountAsset") final String amountAsset, @Path("priceAsset") final String priceAsset,
                                                   @Path("limit") final String limit);
}
