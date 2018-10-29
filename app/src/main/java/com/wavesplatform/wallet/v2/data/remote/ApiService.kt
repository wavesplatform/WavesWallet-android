package com.wavesplatform.wallet.v2.data.remote

import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.remote.response.*
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface ApiService {

    @GET("aliases")
    fun aliases(@Query("address") address: String?): Observable<AliasesResponse>

    @GET("aliases/{alias}")
    fun alias(@Path("alias") alias: String?): Observable<AliasData>

    @GET("assets")
    fun assetsInfoByIds(@Query("ids") ids: List<String?>): Observable<AssetsInfoResponse>

    @GET
    fun loadGlobalConfigurate(@Url url: String = Constants.URL_GLOBAL_CONFIGURATION): Observable<GlobalConfiguration>
}
