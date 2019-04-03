/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.manager

import com.wavesplatform.sdk.net.model.response.GlobalConfiguration
import com.wavesplatform.sdk.net.model.response.GlobalTransactionCommission
import com.wavesplatform.sdk.net.model.response.News
import com.wavesplatform.sdk.net.model.response.SpamAsset
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
        return githubService.news(News.URL)
    }

    fun globalConfiguration(url: String): Observable<GlobalConfiguration> {
        return githubService.globalConfiguration(url)
    }

    fun getGlobalCommission(): Observable<GlobalTransactionCommission> {
        return githubService.globalCommission()
    }
}
