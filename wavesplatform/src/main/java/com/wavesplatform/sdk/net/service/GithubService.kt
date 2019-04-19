/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.net.service

import com.wavesplatform.sdk.net.model.response.GlobalConfigurationResponse
import com.wavesplatform.sdk.net.model.response.GlobalTransactionCommissionResponse
import com.wavesplatform.sdk.net.model.response.NewsResponse
import com.wavesplatform.sdk.utils.Constants
import com.wavesplatform.sdk.utils.EnvironmentManager
import com.wavesplatform.sdk.net.model.LastAppVersionResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Url

interface GithubService {

    @GET
    fun spamAssets(@Url url: String = EnvironmentManager.servers.spamUrl): Observable<String>

    @GET
    fun news(@Url url: String = NewsResponse.URL): Observable<NewsResponse>

    @GET
    fun globalConfiguration(@Url url: String = EnvironmentManager.environment.url): Observable<GlobalConfigurationResponse>

    @GET
    fun globalCommission(@Url url: String = EnvironmentManager.URL_COMMISSION_MAIN_NET): Observable<GlobalTransactionCommissionResponse>

    @GET
    fun loadLastAppVersion(@Url url: String = Constants.URL_VERSION): Observable<LastAppVersionResponse>
}
