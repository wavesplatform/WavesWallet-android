package com.wavesplatform.wallet.v2.data.remote

import com.wavesplatform.wallet.v1.payload.TransactionsInfo
import com.wavesplatform.wallet.v1.payload.WavesBalance
import com.wavesplatform.wallet.v1.request.IssueTransactionRequest
import com.wavesplatform.wallet.v1.request.ReissueTransactionRequest
import com.wavesplatform.wallet.v1.request.TransferTransactionRequest
import com.wavesplatform.wallet.v2.data.model.remote.request.AliasRequest
import com.wavesplatform.wallet.v2.data.model.remote.request.CancelLeasingRequest
import com.wavesplatform.wallet.v2.data.model.remote.request.CreateLeasingRequest
import com.wavesplatform.wallet.v2.data.model.remote.response.Alias
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalances
import com.wavesplatform.wallet.v2.data.model.remote.response.Height
import com.wavesplatform.wallet.v2.data.model.remote.response.Transaction
import io.reactivex.Observable
import retrofit2.http.*

interface MatcherService {

    @GET("matcher/balance/reserved/{publicKey}")
    fun loadReservedBalances(@Path("publicKey") publicKey: String?,
                             @Header("Timestamp") timestamp: Long,
                             @Header("Signature") signature: String): Observable<Map<String, Long>>
}
