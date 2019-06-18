/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.manager.base

import com.wavesplatform.sdk.WavesPlatform
import com.wavesplatform.sdk.net.service.*
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v2.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.local.PreferencesHelper
import com.wavesplatform.wallet.v2.data.manager.CoinomatServiceManager
import com.wavesplatform.wallet.v2.data.manager.GithubServiceManager
import com.wavesplatform.wallet.v2.data.manager.service.CoinomatService
import com.wavesplatform.wallet.v2.data.manager.service.GithubService
import com.wavesplatform.wallet.v2.util.RxEventBus
import com.wavesplatform.wallet.v2.util.WavesWallet
import javax.inject.Inject

open class BaseServiceManager @Inject constructor() {

    var nodeService: NodeService = WavesPlatform.service().getNode()
    var apiService: DataService = WavesPlatform.service().getDataService()
    var matcherService: MatcherService = WavesPlatform.service().getMatcher()
    var githubService: GithubService = GithubServiceManager.create()
    var coinomatService: CoinomatService = CoinomatServiceManager.create()
    var preferencesHelper: PreferencesHelper = PreferencesHelper(App.getAppContext())
    var prefsUtil: PrefsUtil = PrefsUtil(App.getAppContext())
    var rxEventBus: RxEventBus = RxEventBus()


    fun getAddress(): String {
        return WavesWallet.getAddress()
    }

    fun getPublicKeyStr(): String {
        return WavesWallet.getPublicKeyStr()
    }
}
