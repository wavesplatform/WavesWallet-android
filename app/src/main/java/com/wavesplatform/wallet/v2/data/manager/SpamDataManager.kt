package com.wavesplatform.wallet.v2.data.manager

import com.vicpin.krealmextensions.saveAll
import com.wavesplatform.wallet.v2.data.model.remote.response.SpamAsset
import io.reactivex.Observable
import java.util.*
import javax.inject.Inject

class SpamDataManager @Inject constructor() : DataManager() {

    fun loadSpamAssets(): Observable<ArrayList<SpamAsset>> {
        return spamService.spamAssets()
                .map({
                    val scanner = Scanner(it)
                    val spam = arrayListOf<SpamAsset>()
                    while (scanner.hasNextLine()) {
                        spam.add(SpamAsset(scanner.nextLine().split(",")[0]))
                    }
                    scanner.close()
                    spam.saveAll()
                    return@map spam
                })
    }

}
