package com.wavesplatform.sdk.manager.base

import com.wavesplatform.sdk.Wavesplatform
import com.wavesplatform.sdk.service.ApiService
import com.wavesplatform.sdk.service.CoinomatService
import com.wavesplatform.sdk.service.MatcherService
import com.wavesplatform.sdk.service.NodeService
import com.wavesplatform.wallet.v2.data.remote.*
import javax.inject.Inject

open class BaseDataManager @Inject constructor() {

    @Inject
    lateinit var nodeService: NodeService
    lateinit var apiService: ApiService
    @Inject
    lateinit var spamService: SpamService
    @Inject
    lateinit var coinomatService: CoinomatService
    @Inject
    lateinit var matcherService: MatcherService

    fun getAddress(): String? {
        return Wavesplatform.get().getWallet().address
    }

    fun getPublicKeyStr(): String? {
        return Wavesplatform.get().getWallet().publicKeyStr
    }
}
