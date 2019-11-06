/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.manager

import com.google.gson.Gson
import com.vicpin.krealmextensions.deleteAll
import com.vicpin.krealmextensions.saveAll
import com.wavesplatform.sdk.WavesSdk
import com.wavesplatform.sdk.net.NetworkException
import com.wavesplatform.sdk.net.OnErrorListener
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.manager.base.BaseServiceManager
import com.wavesplatform.wallet.v2.data.model.db.SpamAssetDb
import com.wavesplatform.wallet.v2.data.model.service.configs.GlobalConfigurationResponse
import com.wavesplatform.wallet.v2.data.model.service.configs.GlobalTransactionCommissionResponse
import com.wavesplatform.wallet.v2.data.model.service.configs.NewsResponse
import com.wavesplatform.wallet.v2.data.model.service.configs.SpamAssetResponse
import com.wavesplatform.wallet.v2.data.remote.GithubService
import com.wavesplatform.wallet.v2.util.ClientEnvironment
import com.wavesplatform.wallet.v2.util.EnvironmentManager
import com.wavesplatform.wallet.v2.util.PrefsUtil
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

    fun loadSpamAssets(): Observable<ArrayList<SpamAssetResponse>> {
        return githubService.spamAssets(prefsUtil.getValue(PrefsUtil.KEY_SPAM_URL, EnvironmentManager.servers.spamUrl))
                .onErrorReturn {
                    ClientEnvironment.loadJsonFromAsset(App.getAppContext(), Constants.GITHUB_SPAM_FILE)
                }
                .map {
                    val scanner = Scanner(it)
                    val spam = arrayListOf<SpamAssetResponse>()
                    while (scanner.hasNextLine()) {
                        spam.add(SpamAssetResponse(scanner.nextLine().split(",")[0]))
                    }
                    scanner.close()

                    // clear old spam list and save new
                    deleteAll<SpamAssetDb>()
                    SpamAssetDb.convertToDb(spam).saveAll()

                    return@map spam
                }
    }

    fun loadNews(): Observable<NewsResponse> {
        val newsUrl = if (preferencesHelper.useTestNews) {
            Constants.News.URL_TEST
        } else {
            Constants.News.URL
        }
        return githubService.news(newsUrl)
    }

    fun getGlobalCommission(): Observable<GlobalTransactionCommissionResponse> {
        return githubService.globalCommission()
                .onErrorReturn {
                    return@onErrorReturn Gson().fromJson(
                            ClientEnvironment.loadJsonFromAsset(App.getAppContext(), Constants.GITHUB_FEE_FILE),
                            GlobalTransactionCommissionResponse::class.java)
                }
    }

    companion object {

        private var onErrorListener: OnErrorListener? = null

        fun create(onErrorListener: OnErrorListener? = null): GithubService {
            this.onErrorListener = onErrorListener
            return WavesSdk.service().createService(Constants.URL_GITHUB_CONFIG,
                    object : OnErrorListener {
                        override fun onError(exception: NetworkException) {
                            GithubServiceManager.onErrorListener?.onError(exception)
                        }
                    })
                    .create(GithubService::class.java)
        }

        fun removeOnErrorListener() {
            onErrorListener = null
        }
    }
}
