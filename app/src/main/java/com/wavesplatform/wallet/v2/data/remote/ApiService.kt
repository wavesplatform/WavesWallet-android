package com.wavesplatform.wallet.v2.data.remote

import com.wavesplatform.wallet.v2.data.model.remote.response.AliasData
import com.wavesplatform.wallet.v2.data.model.remote.response.AliasesResponse
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("aliases")
    fun aliases(@Query("address") address: String?): Observable<AliasesResponse>

    @GET("aliases/{alias}")
    fun alias(@Path("alias") alias: String?): Observable<AliasData>

    @GET("aliases/{assetId}")
    fun assetDetails(@Path("assetId") assetId: String?): Observable<AssetBalance>
}
