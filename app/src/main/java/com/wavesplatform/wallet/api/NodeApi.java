package com.wavesplatform.wallet.api;

import com.wavesplatform.wallet.payload.AssetBalances;
import com.wavesplatform.wallet.payload.Candle;
import com.wavesplatform.wallet.payload.Transaction;
import com.wavesplatform.wallet.payload.WavesBalance;
import com.wavesplatform.wallet.request.IssueTransactionRequest;
import com.wavesplatform.wallet.request.ReissueTransactionRequest;
import com.wavesplatform.wallet.request.TransferTransactionRequest;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.Call;
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

    @POST("/assets/broadcast/transfer")
    Observable<TransferTransactionRequest> broadcastTransfer(@Body TransferTransactionRequest tx);

    @POST("/assets/broadcast/issue")
    Observable<IssueTransactionRequest> broadcastIssue(@Body IssueTransactionRequest tx);

    @POST("/assets/broadcast/reissue")
    Observable<ReissueTransactionRequest> broadcastReissue(@Body ReissueTransactionRequest tx);

    @GET("/transactions/unconfirmed")
    Observable<List<Transaction>> unconfirmedTransactions();
}
