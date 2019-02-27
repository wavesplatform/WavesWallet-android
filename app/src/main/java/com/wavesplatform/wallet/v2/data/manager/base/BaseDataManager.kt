package com.wavesplatform.wallet.v2.data.manager.base

import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v1.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.local.PreferencesHelper
import com.wavesplatform.wallet.v2.data.remote.*
import com.wavesplatform.wallet.v2.util.RxEventBus
import javax.inject.Inject

open class BaseDataManager @Inject constructor() {

    @Inject
    lateinit var nodeService: NodeService
    @Inject
    lateinit var apiService: ApiService
    @Inject
    lateinit var githubService: GithubService
    @Inject
    lateinit var coinomatService: CoinomatService
    @Inject
    lateinit var matcherService: MatcherService
    @Inject
    lateinit var preferencesHelper: PreferencesHelper
    @Inject
    lateinit var prefsUtil: PrefsUtil
    @Inject
    lateinit var rxEventBus: RxEventBus

    fun getAddress(): String? {
        return App.getAccessManager().getWallet()?.address
    }

    fun getPublicKeyStr(): String? {
        return App.getAccessManager().getWallet()?.publicKeyStr
    }
}
