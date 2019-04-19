/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.manager

import com.wavesplatform.sdk.net.model.LastAppVersionResponse
import com.wavesplatform.sdk.net.model.response.GlobalConfigurationResponse
import com.wavesplatform.sdk.net.model.response.GlobalTransactionCommissionResponse
import com.wavesplatform.sdk.net.model.response.NewsResponse
import com.wavesplatform.sdk.net.model.response.SpamAssetResponse
import com.wavesplatform.wallet.v2.data.manager.base.BaseDataManager
import io.reactivex.Observable
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GithubDataManager @Inject constructor() : BaseDataManager() {

    fun isValidNewSpamUrl(url: String): Observable<Boolean> {
        return githubService.spamAssets(url)
                .map {
                    val scanner = Scanner(it)
                    try {
                        if (scanner.hasNextLine()) {
                            val spamAsset = SpamAssetResponse(scanner.nextLine().split(",")[0])
                            return@map !spamAsset.assetId.isNullOrEmpty()
                        } else {
                            return@map false
                        }
                    } catch (e: Throwable) {
                        return@map false
                    } finally {
                        scanner.close()
                    }
                }
    }

    fun loadNews(): Observable<NewsResponse> {
        return githubService.news(NewsResponse.URL)
    }

    fun globalConfiguration(url: String): Observable<GlobalConfigurationResponse> {
        return githubService.globalConfiguration(url)
    }

    fun getGlobalCommission(): Observable<GlobalTransactionCommissionResponse> {
        return githubService.globalCommission()
    }

    fun loadLastAppVersion(): Observable<LastAppVersionResponse> {
        return githubService.loadLastAppVersion()
    }
}
