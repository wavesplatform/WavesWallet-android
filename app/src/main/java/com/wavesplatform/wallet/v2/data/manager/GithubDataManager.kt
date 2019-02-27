package com.wavesplatform.wallet.v2.data.manager

import com.wavesplatform.wallet.v2.data.manager.base.BaseDataManager
import com.wavesplatform.wallet.v2.data.model.remote.response.GlobalConfiguration
import com.wavesplatform.wallet.v2.data.model.remote.response.GlobalTransactionCommission
import com.wavesplatform.wallet.v2.data.model.remote.response.News
import com.wavesplatform.wallet.v2.data.model.remote.response.SpamAsset
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
