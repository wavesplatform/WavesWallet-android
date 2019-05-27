/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.manager

import com.wavesplatform.sdk.Wavesplatform
import com.wavesplatform.sdk.net.model.response.GlobalTransactionCommissionResponse
import com.wavesplatform.sdk.net.model.response.NewsResponse
import com.wavesplatform.sdk.net.model.response.SpamAssetResponse
import com.wavesplatform.sdk.utils.Constants
import com.wavesplatform.wallet.v2.data.manager.base.BaseDataManager
import com.wavesplatform.wallet.v2.data.manager.service.GithubService
import io.reactivex.Observable
import retrofit2.CallAdapter
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
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
        val newsUrl = if (preferencesHelper.useTestNews) {
            NewsResponse.URL_TEST
        } else {
            NewsResponse.URL
        }
        return githubService.news(newsUrl)
    }

    fun getGlobalCommission(): Observable<GlobalTransactionCommissionResponse> {
        return githubService.globalCommission()
    }

    companion object {
        fun create(adapterFactory: CallAdapter.Factory?, url: String)
                : GithubService {
            return Wavesplatform.createService(url,
                    adapterFactory ?: RxJava2CallAdapterFactory.create())
                    .create(GithubService::class.java)
        }
    }
}
