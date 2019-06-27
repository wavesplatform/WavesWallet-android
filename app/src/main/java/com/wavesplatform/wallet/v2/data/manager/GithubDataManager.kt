/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.manager

import com.wavesplatform.wallet.v1.ui.auth.EnvironmentManager
import com.wavesplatform.wallet.v2.data.manager.base.BaseDataManager
import com.wavesplatform.wallet.v2.data.model.remote.response.*
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
                            val spamAsset = SpamAsset(scanner.nextLine().split(",")[0])
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

    fun loadNews(): Observable<News> {
        val newsUrl = if (preferencesHelper.useTestNews) {
            News.URL_TEST
        } else {
            News.URL
        }
        return githubService.news(newsUrl)
    }

    fun globalConfiguration(url: String): Observable<GlobalConfiguration> {
        return githubService.globalConfiguration(url)
    }

    fun getGlobalCommission(): Observable<GlobalTransactionCommission> {
        return githubService.globalCommission(EnvironmentManager.URL_COMMISSION_MAIN_NET)
                .onErrorResumeNext(githubService.globalCommission(EnvironmentManager.URL_RAW_COMMISSION_MAIN_NET))
    }

    fun loadLastAppVersion(): Observable<LastAppVersion> {
        return githubService.loadLastAppVersion()
    }
}
