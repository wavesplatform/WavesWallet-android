/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v1.api;

import com.wavesplatform.wallet.v1.payload.AssetBalances;
import com.wavesplatform.wallet.v1.payload.Transaction;
import com.wavesplatform.wallet.v1.payload.TransactionsInfo;
import com.wavesplatform.wallet.v1.payload.WavesBalance;
import com.wavesplatform.wallet.v1.request.IssueTransactionRequest;
import com.wavesplatform.wallet.v1.request.ReissueTransactionRequest;
import com.wavesplatform.wallet.v1.request.TransferTransactionRequest;
import com.wavesplatform.wallet.v2.data.model.remote.request.TransactionsBroadcastRequest;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface NodeApi {
    @GET("/assets/balance/{address}")
    Observable<AssetBalances> assetsBalance(@Path("address") String address);

    @GET("/addresses/balance/{address}")
    Observable<WavesBalance> wavesBalance(@Path("address") String address);

    @GET("/transactions/address/{address}/limit/{limit}")
    Observable<List<List<Transaction>>> transactionList(@Path("address") String address, @Path("limit") int limit);

    @GET("transactions/info/{asset}")
    Observable<TransactionsInfo> getTransactionsInfo(@Path("asset") final String asset);

    @POST("/assets/broadcast/transfer")
    Observable<TransferTransactionRequest> broadcastTransfer(@Body TransferTransactionRequest tx);

    @POST("/transactions/broadcast")
    Observable<TransactionsBroadcastRequest> transactionsBroadcast(@Body TransactionsBroadcastRequest tx);

    @POST("/assets/broadcast/issue")
    Observable<IssueTransactionRequest> broadcastIssue(@Body IssueTransactionRequest tx);

    @POST("/assets/broadcast/reissue")
    Observable<ReissueTransactionRequest> broadcastReissue(@Body ReissueTransactionRequest tx);

    @GET("/transactions/unconfirmed")
    Observable<List<Transaction>> unconfirmedTransactions();
}
