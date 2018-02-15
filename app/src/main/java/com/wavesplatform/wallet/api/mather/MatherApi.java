package com.wavesplatform.wallet.api.mather;

import com.google.gson.internal.LinkedTreeMap;
import com.wavesplatform.wallet.payload.Markets;
import com.wavesplatform.wallet.payload.MyOrder;
import com.wavesplatform.wallet.payload.OrderBook;
import com.wavesplatform.wallet.payload.TransactionsInfo;
import com.wavesplatform.wallet.request.CancelOrderRequest;
import com.wavesplatform.wallet.request.OrderRequest;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface MatherApi {
    @GET("matcher/orderbook")
    Observable<Markets> getAllMarkets();

    @GET("matcher/orderbook/{amountAsset}/{priceAsset}/tradableBalance/{address}")
    Observable<LinkedTreeMap<String, Long>> getBalanceFromAssetPair(@Path("amountAsset") final String amountAsset,
                                                                    @Path("priceAsset") final String priceAsset,
                                                                    @Path("address") final String address);

    @GET("matcher/orderbook/{amountAsset}/{priceAsset}")
    Observable<OrderBook> getOrderBook(@Path("amountAsset") final String amountAsset,
                                       @Path("priceAsset") final String priceAsset);


    @GET("matcher/orderbook/{amountAsset}/{priceAsset}/publicKey/{publicKey}")
    Observable<List<MyOrder>> getMyOrders(@Path("amountAsset") final String amountAsset,
                                          @Path("priceAsset") final String priceAsset,
                                          @Path("publicKey") final String publicKey,
                                          @Header("signature") final String signature,
                                          @Header("timestamp") final long timestamp);

    @GET("matcher")
    Observable<String> getMatcherKey();

    @POST("matcher/orderbook")
    Observable<Object> placeOrder(@Body OrderRequest orderRequest);

    @POST("matcher/orderbook/{amountAsset}/{priceAsset}/cancel")
    Observable<Object> cancelOrder(@Path("amountAsset") final String amountAsset,
                                  @Path("priceAsset") final String priceAsset,
                                  @Body CancelOrderRequest cancelOrderRequest);


    @POST("matcher/orderbook/{amountAsset}/{priceAsset}/delete")
    Observable<Object> deleteOrder(@Path("amountAsset") final String amountAsset,
                                  @Path("priceAsset") final String priceAsset,
                                  @Body CancelOrderRequest cancelOrderRequest);
}
