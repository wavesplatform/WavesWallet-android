package com.wavesplatform.sdk.manager.base

import android.content.Context
import com.wavesplatform.sdk.Wavesplatform
import com.wavesplatform.sdk.service.*
import retrofit2.CallAdapter
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class BaseDataManager @Inject constructor(var context: Context,
                                               var factory: CallAdapter.Factory?) {

    var nodeService = NodeService.create(context, factory ?: RxJava2CallAdapterFactory.create())
    var apiService: ApiService = ApiService.create()
    var spamService: SpamService = SpamService.create()
    var coinomatService: CoinomatService = CoinomatService.create()
    var matcherService: MatcherService = MatcherService.create()

    fun setCallAdapterFactory(factory: CallAdapter.Factory) {
        nodeService = NodeService.create(context, factory)
    }
}
