package com.wavesplatform.wallet.v2.data.manager

import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v2.data.local.PreferencesHelper
import com.wavesplatform.wallet.v2.data.remote.ApiService
import com.wavesplatform.wallet.v2.data.remote.NodeService
import com.wavesplatform.wallet.v2.data.remote.SpamService
import javax.inject.Inject

open class DataManager @Inject constructor() {

    @Inject
    lateinit var nodeService: NodeService
    @Inject
    lateinit var apiService: ApiService
    @Inject
    lateinit var spamService: SpamService
    @Inject
    lateinit var preferencesHelper: PreferencesHelper

    fun getAddress(): String? {
        return App.getAccessManager().getWallet()?.address
    }

    fun getPublicKeyStr(): String? {
        return App.getAccessManager().getWallet()?.publicKeyStr
    }
}
