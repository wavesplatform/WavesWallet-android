package com.wavesplatform.sdk.manager

import com.wavesplatform.sdk.manager.base.BaseDataManager
import com.wavesplatform.sdk.model.response.SpamAsset
import io.reactivex.Observable
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpamDataManager @Inject constructor() : BaseDataManager() {

    fun isValidNewSpamUrl(url: String): Observable<Boolean> {
        return spamService.spamAssets(url)
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
}
