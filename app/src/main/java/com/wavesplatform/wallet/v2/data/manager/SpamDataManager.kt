package com.wavesplatform.wallet.v2.data.manager

import com.vicpin.krealmextensions.deleteAll
import com.vicpin.krealmextensions.saveAll
import com.wavesplatform.wallet.BuildConfig
import com.wavesplatform.wallet.v1.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.remote.response.SpamAsset
import io.reactivex.Observable
import java.util.*
import javax.inject.Inject

class SpamDataManager @Inject constructor() : DataManager() {

    fun loadSpamAssets(): Observable<ArrayList<SpamAsset>> {
        return spamService.spamAssets(prefsUtil.getValue(PrefsUtil.KEY_SPAM_URL, Constants.URL_SPAM))
                .map {
                    val scanner = Scanner(it)
                    val spam = arrayListOf<SpamAsset>()
                    while (scanner.hasNextLine()) {
                        spam.add(SpamAsset(scanner.nextLine().split(",")[0]))
                    }
                    scanner.close()

                    // clear old spam list and save new
                    deleteAll<SpamAsset>()
                    spam.saveAll()

                    return@map spam
                }.map { spamListFromDb ->
                    if (prefsUtil.getValue(PrefsUtil.KEY_DISABLE_SPAM_FILTER, false)) {
                        return@map arrayListOf<SpamAsset>()
                    } else {
                        return@map spamListFromDb
                    }
                }
    }

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
