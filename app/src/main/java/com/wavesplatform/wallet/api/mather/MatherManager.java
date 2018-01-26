package com.wavesplatform.wallet.api.mather;

import com.google.gson.internal.LinkedTreeMap;
import com.ihsanbal.logging.Level;
import com.ihsanbal.logging.LoggingInterceptor;
import com.wavesplatform.wallet.crypto.PublicKeyAccount;
import com.wavesplatform.wallet.data.factory.RxErrorHandlingCallAdapterFactory;
import com.wavesplatform.wallet.payload.Markets;
import com.wavesplatform.wallet.payload.MyOrder;
import com.wavesplatform.wallet.payload.OrderBook;
import com.wavesplatform.wallet.payload.TransactionsInfo;
import com.wavesplatform.wallet.request.CancelOrderRequest;
import com.wavesplatform.wallet.request.OrderRequest;
import com.wavesplatform.wallet.ui.auth.EnvironmentManager;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import okhttp3.internal.platform.Platform;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class MatherManager {
    private static MatherManager instance;
    private final MatherApi service;
    private OkHttpClient okHttpClient;

    public static MatherManager get() {
        return instance;
    }

    public static MatherManager createInstance(String pubKey) {
        try {
            instance = new MatherManager(pubKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instance;
    }

    public String getAddress() {
        return publicKeyAccount.getAddress();
    }

    private final PublicKeyAccount publicKeyAccount;

    private MatherManager(String pubKey) throws PublicKeyAccount.InvalidPublicKey {
        okHttpClient = new OkHttpClient.Builder()
                //.addInterceptor(OkLogInterceptor.builder().withAllLogData().build())
                .addInterceptor(new LoggingInterceptor.Builder()
                        .loggable(false)
                        .setLevel(Level.BASIC)
                        .log(Platform.INFO)
                        .request("Request")
                        .response("Response")
                        .build())
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(EnvironmentManager.get().current().getMatherUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(new RxErrorHandlingCallAdapterFactory())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient)
                .build();
        service = retrofit.create(MatherApi.class);

        this.publicKeyAccount = new PublicKeyAccount(pubKey);
    }

    public Observable<Markets> getAllMarkets() {
        return service.getAllMarkets();
    }

    public Observable<OrderBook> getOrderBookInterval(final String amountAsset, final String priceAsset) {
        return Observable.interval(0, 5, TimeUnit.SECONDS)
                .retry(3)
                .flatMap(aLong -> service.getOrderBook(amountAsset, priceAsset));
    }

    public Observable<OrderBook> getOrderBook(final String amountAsset, final String priceAsset) {
        return service.getOrderBook(amountAsset, priceAsset);
    }

    public Observable<TransactionsInfo> getTransactionsInfo(final String asset) {
        return service.getTransactionsInfo(asset);
    }

    public Observable<LinkedTreeMap<String, Long>> getBalanceFromAssetPair(final String amountAsset,
                                                                           final String priceAsset,
                                                                           final String address) {
        return service.getBalanceFromAssetPair(amountAsset, priceAsset, address);
    }

    public Observable<List<MyOrder>> getMyOrders(final String amountAsset,
                                                 final String priceAsset,
                                                 final String publicKey,
                                                 final String signature,
                                                 final long timestamp) {
        return service.getMyOrders(amountAsset, priceAsset, publicKey, signature, timestamp);
    }

    public Observable<String> getMatcherKey() {
        return service.getMatcherKey();
    }

    public Observable<Object> placeOrder(OrderRequest orderRequest) {
        return service.placeOrder(orderRequest);
    }

    public Observable<Object> cancelOrder(String amountAsset, String priceAsset, CancelOrderRequest cancelOrderRequest) {
        return service.cancelOrder( amountAsset, priceAsset, cancelOrderRequest);
    }

    public Observable<Object> deleteOrder(String amountAsset, String priceAsset, CancelOrderRequest cancelOrderRequest) {
        return service.deleteOrder( amountAsset, priceAsset, cancelOrderRequest);
    }
}
