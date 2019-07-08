/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.manager.base

import com.wavesplatform.sdk.WavesSdk
import com.wavesplatform.sdk.net.service.*
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v2.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.local.PreferencesHelper
import com.wavesplatform.wallet.v2.data.manager.GithubServiceManager
import com.wavesplatform.wallet.v2.data.manager.gateway.manager.CoinomatDataManager
import com.wavesplatform.wallet.v2.data.manager.gateway.manager.GatewayDataManager
import com.wavesplatform.wallet.v2.data.remote.CoinomatService
import com.wavesplatform.wallet.v2.data.remote.GatewayService
import com.wavesplatform.wallet.v2.data.remote.GithubService
import com.wavesplatform.wallet.v2.util.RxEventBus
import com.wavesplatform.wallet.v2.util.WavesWallet
import javax.inject.Inject

open class BaseServiceManager @Inject constructor() {

    var nodeService: NodeService = WavesSdk.service().getNode()
    var dataService: DataService = WavesSdk.service().getDataService()
    var matcherService: MatcherService = WavesSdk.service().getMatcher()
    var githubService: GithubService = GithubServiceManager.create()
    var coinomatService: CoinomatService = CoinomatDataManager.create()
    var gatewayService: GatewayService = GatewayDataManager.create()
    var preferencesHelper: PreferencesHelper = PreferencesHelper(App.getAppContext())
    var prefsUtil: PrefsUtil = PrefsUtil(App.getAppContext())
    var rxEventBus: RxEventBus = RxEventBus()


    fun getAddress(): String {
        return WavesWallet.getAddress()
    }

    fun getPublicKeyStr(): String {
        return WavesWallet.getPublicKeyStr()
    }

    fun getPrivateKey(): ByteArray {
        return WavesWallet.getPrivateKey()
    }
}
