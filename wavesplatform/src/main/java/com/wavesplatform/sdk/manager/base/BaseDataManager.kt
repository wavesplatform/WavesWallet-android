package com.wavesplatform.sdk.manager.base

import com.wavesplatform.sdk.service.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class BaseDataManager @Inject constructor() {

    var nodeService = NodeService.create()
    var apiService: ApiService = ApiService.create()
    var spamService: SpamService = SpamService.create()
    var coinomatService: CoinomatService = CoinomatService.create()
    var matcherService: MatcherService = MatcherService.create()
}
