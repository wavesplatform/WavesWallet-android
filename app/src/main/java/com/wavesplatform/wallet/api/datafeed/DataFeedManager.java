package com.wavesplatform.wallet.api.datafeed;

import com.google.gson.GsonBuilder;
import com.ihsanbal.logging.Level;
import com.ihsanbal.logging.LoggingInterceptor;
import com.wavesplatform.wallet.data.factory.RxErrorHandlingCallAdapterFactory;
import com.wavesplatform.wallet.payload.Candle;
import com.wavesplatform.wallet.payload.TickerMarket;
import com.wavesplatform.wallet.payload.TradesMarket;
import com.wavesplatform.wallet.ui.auth.EnvironmentManager;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import okhttp3.internal.platform.Platform;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class DataFeedManager {

    private static DataFeedManager instance = new DataFeedManager();
    private final DataFeedApi service;
    private OkHttpClient okHttpClient;

    public DataFeedManager() {
        okHttpClient = new OkHttpClient.Builder()
                //.addInterceptor(OkLogInterceptor.builder().withAllLogData().build())
                .addInterceptor(new LoggingInterceptor.Builder()
                        .loggable(false)
                        .setLevel(Level.BASIC)
                        .log(Platform.INFO)
                        .request("Request")
                        .response("Response").build()).build();

        Retrofit retrofit = new Retrofit.Builder()
                //.baseUrl("http://localhost:6869")
                .baseUrl(EnvironmentManager.get().current().getDataFeedUrl())
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().create()))
                .addCallAdapterFactory(new RxErrorHandlingCallAdapterFactory())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient)
                .build();
        service = retrofit.create(DataFeedApi.class);
    }

    public static DataFeedManager get() {
        return instance;
    }

    public static DataFeedManager createInstance() {
        try {
            instance = new DataFeedManager();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instance;
    }

    public Observable<List<Candle>> loadCandlesOnInterval(String amountAssets,
                                                          String priceAssests,
                                                          int timeFrame,
                                                          long from,
                                                          long to) {
        return service.getCandlesOnInterval(amountAssets,
                priceAssests, timeFrame, from, to)
                .flatMap(candles -> {
                    Collections.sort(candles, (o1, o2) -> o1.getTimestamp().compareTo(o2.getTimestamp()));
                    return Observable.just(candles);
                });
    }

    public Observable<TickerMarket> getTickerByPair(String amountAssets, String priceAsset) {
        return Observable.interval(0, 30, TimeUnit.SECONDS)
                .retry(3)
                .flatMap(aLong -> service.getTickerByPair(amountAssets, priceAsset));
    }

    public Observable<TradesMarket> getTradesByPair(String amountAssets, String priceAsset) {
        return Observable.interval(0, 5, TimeUnit.SECONDS)
                .retry(3)
                .flatMap(aLong -> service.getTradesByPair(amountAssets, priceAsset, "1")
                        .flatMap(Observable::fromIterable)
                        .take(1));
    }

    public Observable<List<TradesMarket>> getTradesByPairWithoutInterval(String amountAssets, String priceAsset, String limit) {
        return service.getTradesByPair(amountAssets, priceAsset, limit);
    }
}
