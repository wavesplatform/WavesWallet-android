package com.wavesplatform.wallet.v2.data.remote

import com.wavesplatform.wallet.v2.data.model.remote.response.CoinomatLimit
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface CoinomatService {

    @GET("limits.php")
    fun limits(@Query("crypto") crypto: String?,
               @Query("address") address: String?,
               @Query("fiat") fiat: String?): Observable<CoinomatLimit>

    @GET("rate.php")
    fun rate(@Query("crypto") crypto: String?,
             @Query("address") address: String?,
             @Query("fiat") fiat: String?,
             @Query("amount") amount: String?): Observable<String>
}