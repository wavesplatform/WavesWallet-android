/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.manager

import com.wavesplatform.sdk.WavesPlatform
import com.wavesplatform.sdk.net.CallAdapterFactory
import com.wavesplatform.sdk.net.OnErrorListener
import com.wavesplatform.sdk.net.NetworkException
import com.wavesplatform.wallet.v2.data.model.service.cofigs.GlobalTransactionCommissionResponse
import com.wavesplatform.wallet.v2.data.model.service.cofigs.NewsResponse
import com.wavesplatform.wallet.v2.data.model.service.cofigs.SpamAssetResponse
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.manager.base.BaseServiceManager
import com.wavesplatform.wallet.v2.data.manager.service.GithubService
import io.reactivex.Observable
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GithubServiceManager @Inject constructor() : BaseServiceManager() {

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

        private var onErrorListener: OnErrorListener? = null

        fun create(onErrorListener: OnErrorListener? = null): GithubService {
            this.onErrorListener = onErrorListener
            val adapterFactory = CallAdapterFactory(object : OnErrorListener{
                override fun onError(exception: NetworkException) {
                    GithubServiceManager.onErrorListener?.onError(exception)
                }
            })
            return WavesPlatform.service().createService(Constants.URL_GITHUB_CONFIG, adapterFactory)
                    .create(GithubService::class.java)
        }

        fun removeOnErrorListener() {
            onErrorListener = null
        }
    }
}
