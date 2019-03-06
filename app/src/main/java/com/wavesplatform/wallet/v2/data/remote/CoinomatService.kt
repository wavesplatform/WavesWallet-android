package com.wavesplatform.wallet.v2.data.remote

import com.wavesplatform.wallet.v2.data.model.remote.response.coinomat.CreateTunnel
import com.wavesplatform.wallet.v2.data.model.remote.response.coinomat.GetTunnel
import com.wavesplatform.wallet.v2.data.model.remote.response.coinomat.Limit
import com.wavesplatform.wallet.v2.data.model.remote.response.coinomat.XRate
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface CoinomatService {

    @GET("v2/indacoin/limits.php")
    fun limits(
        @Query("crypto") crypto: String?,
        @Query("address") address: String?,
        @Query("fiat") fiat: String?
    ): Observable<Limit>

    @GET("v2/indacoin/rate.php")
    fun rate(
        @Query("crypto") crypto: String?,
        @Query("address") address: String?,
        @Query("fiat") fiat: String?,
        @Query("amount") amount: String?
    ): Observable<String>

    @GET("v1/create_tunnel.php")
    fun createTunnel(
        @Query("currency_from") currencyFrom: String?,
        @Query("currency_to") currencyTo: String?,
        @Query("wallet_to") address: String?,
        @Query("monero_payment_id") moneroPaymentId: String?
    ): Observable<CreateTunnel>

    @GET("v1/get_tunnel.php")
    fun getTunnel(
        @Query("xt_id") xtId: String?,
        @Query("k1") k1: String?,
        @Query("k2") k2: String?,
        @Query("lang") lang: String?
    ): Observable<GetTunnel>

    // https://coinomat.com/api/v1/get_xrate.php?f=WETH&t=ETH&lang=ru_RU
    @GET("v1/get_xrate.php")
    fun getXRate(
        @Query("f") from: String?,
        @Query("t") to: String?,
        @Query("lang") lang: String?
    ): Observable<XRate>

    companion object {
        const val GATEWAY_ADDRESS = "3PAs2qSeUAfgqSKS8LpZPKGYEjJKcud9Djr"
    }
}