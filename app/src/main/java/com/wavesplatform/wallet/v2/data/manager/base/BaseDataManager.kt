/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.manager.base

import com.wavesplatform.sdk.Wavesplatform
import com.wavesplatform.sdk.net.service.*
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v2.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.local.PreferencesHelper
import com.wavesplatform.wallet.v2.util.RxEventBus
import javax.inject.Inject

open class BaseDataManager @Inject constructor() {

    var nodeService: NodeService = Wavesplatform.getNodeService()
    var apiService: ApiService = Wavesplatform.getApiService()
    var githubService: GithubService = Wavesplatform.getGithubService()
    var coinomatService: CoinomatService = Wavesplatform.getCoinomatService()
    var matcherService: MatcherService = Wavesplatform.getMatcherService()
    var preferencesHelper: PreferencesHelper = PreferencesHelper(App.getAppContext())
    var prefsUtil: PrefsUtil = PrefsUtil(App.getAppContext())
    var rxEventBus: RxEventBus = RxEventBus()


    fun getAddress(): String {
        return Wavesplatform.getAddress()
    }

    fun getPublicKeyStr(): String {
        return Wavesplatform.getPublicKeyStr()
    }
}
