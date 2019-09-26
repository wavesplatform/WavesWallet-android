/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.remote

import com.wavesplatform.wallet.v2.data.model.service.cofigs.LastAppVersionResponse
import com.wavesplatform.wallet.v2.data.model.service.cofigs.GlobalConfigurationResponse
import com.wavesplatform.wallet.v2.data.model.service.cofigs.GlobalTransactionCommissionResponse
import com.wavesplatform.wallet.v2.data.model.service.cofigs.NewsResponse
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.util.EnvironmentManager
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url

interface GithubService {

    @GET
    fun spamAssets(@Url url: String = EnvironmentManager.servers.spamUrl): Observable<String>

    @GET
    fun globalConfiguration(@Url url: String = EnvironmentManager.environment.url): Observable<GlobalConfigurationResponse>

    @GET("{newsPath}")
    fun news(@Path("newsPath") newsPath: String? = Constants.News.URL): Observable<NewsResponse>

    @GET("{commissionPath}")
    fun globalCommission(@Path("commissionPath") commissionPath: String?
                         = EnvironmentManager.URL_COMMISSION_MAIN_NET)
            : Observable<GlobalTransactionCommissionResponse>

    @GET("{versionPath}")
    fun loadLastAppVersion(@Path("versionPath") versionPath: String? = Constants.URL_GITHUB_CONFIG_VERSION)
            : Observable<LastAppVersionResponse>
}
