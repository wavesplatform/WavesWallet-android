package com.wavesplatform.wallet.v2.data.remote

import com.wavesplatform.wallet.v1.payload.TransactionsInfo
import com.wavesplatform.wallet.v1.payload.WavesBalance
import com.wavesplatform.wallet.v1.request.IssueTransactionRequest
import com.wavesplatform.wallet.v1.request.ReissueTransactionRequest
import com.wavesplatform.wallet.v1.request.TransferTransactionRequest
import com.wavesplatform.wallet.v2.data.model.remote.response.AliasesResponse
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalances
import com.wavesplatform.wallet.v2.data.model.remote.response.Transaction
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface NodeService {

    @GET("assets/balance/{address}")
    fun assetsBalance(@Path("address") address: String?): Observable<AssetBalances>

    @GET("addresses/balance/{address}")
    fun wavesBalance(@Path("address") address: String?): Observable<WavesBalance>

    @GET("transactions/address/{address}/limit/{limit}")
    fun transactionList(@Path("address") address: String?, @Path("limit") limit: Int): Observable<List<List<Transaction>>>

    @GET("transactions/info/{asset}")
    fun getTransactionsInfo(@Path("asset") asset: String): Observable<TransactionsInfo>

    @POST("assets/broadcast/transfer")
    fun broadcastTransfer(@Body tx: TransferTransactionRequest): Observable<TransferTransactionRequest>

    @POST("assets/broadcast/issue")
    fun broadcastIssue(@Body tx: IssueTransactionRequest): Observable<IssueTransactionRequest>

    @POST("assets/broadcast/reissue")
    fun broadcastReissue(@Body tx: ReissueTransactionRequest): Observable<ReissueTransactionRequest>

    @GET("transactions/unconfirmed")
    fun unconfirmedTransactions(): Observable<List<Transaction>>

}
