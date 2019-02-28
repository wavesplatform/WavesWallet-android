package com.wavesplatform.sdk.service

import com.wavesplatform.sdk.model.response.GlobalConfiguration
import com.wavesplatform.sdk.model.response.GlobalTransactionCommission
import com.wavesplatform.sdk.model.response.News
import com.wavesplatform.sdk.utils.EnvironmentManager
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Url

interface GithubService {

    @GET
    fun spamAssets(@Url url: String = EnvironmentManager.servers.spamUrl): Observable<String>

    @GET
    fun news(@Url url: String = News.URL): Observable<News>

    @GET
    fun globalConfiguration(@Url url: String = EnvironmentManager.environment.url)
            : Observable<GlobalConfiguration>

    @GET
    fun globalCommission(@Url url: String = EnvironmentManager.URL_COMMISSION_MAIN_NET)
            : Observable<GlobalTransactionCommission>
}
