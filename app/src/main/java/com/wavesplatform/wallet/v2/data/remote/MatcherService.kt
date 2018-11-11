package com.wavesplatform.wallet.v2.data.remote

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface MatcherService {

    @GET("matcher/balance/reserved/{publicKey}")
    fun loadReservedBalances(@Path("publicKey") publicKey: String?,
                             @Header("Timestamp") timestamp: Long,
                             @Header("Signature") signature: String): Observable<Map<String, Long>>
}
