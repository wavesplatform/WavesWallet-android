package com.wavesplatform.wallet.v2.data.manager

import com.wavesplatform.wallet.v2.data.manager.base.BaseDataManager
import com.wavesplatform.wallet.v2.data.model.local.WatchMarket
import com.wavesplatform.wallet.v2.data.model.remote.response.LastTrade
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataFeedManager @Inject constructor() : BaseDataManager() {
    fun getTradesByPair(watchMarket: WatchMarket?, limit: Int): Observable<List<LastTrade>> {
        return dataFeedService.getTradesByPair(watchMarket?.market?.amountAsset, watchMarket?.market?.priceAsset, limit)
    }
}
