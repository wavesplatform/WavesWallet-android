package com.wavesplatform.wallet.v2.data.remote

import com.wavesplatform.wallet.v1.payload.TransactionsInfo
import com.wavesplatform.wallet.v1.payload.WavesBalance
import com.wavesplatform.wallet.v1.request.IssueTransactionRequest
import com.wavesplatform.wallet.v1.request.ReissueTransactionRequest
import com.wavesplatform.wallet.v1.request.TransferTransactionRequest
import com.wavesplatform.wallet.v2.data.model.remote.response.*
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.http.*

interface ApiService {

    @GET("aliases")
    fun aliases(@Query("address") address: String?): Observable<AliasesResponse>

    @GET("aliases/{alias}")
    fun alias(@Path("alias") alias: String?): Observable<AliasData>

    @GET("aliases/{assetId}")
    fun assetDetails(@Path("assetId") assetId: String?): Observable<AssetBalance>
}
