package com.wavesplatform.wallet.v2.data.manager

import com.wavesplatform.wallet.v1.payload.Candle
import com.wavesplatform.wallet.v2.data.manager.base.BaseDataManager
import com.wavesplatform.wallet.v2.data.model.local.WatchMarket
import com.wavesplatform.wallet.v2.data.model.remote.response.LastTrade
import io.reactivex.Observable
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataFeedManager @Inject constructor() : BaseDataManager() {
    fun getTradesByPair(watchMarket: WatchMarket?, limit: Int): Observable<List<LastTrade>> {
        return dataFeedService.getTradesByPair(watchMarket?.market?.amountAsset, watchMarket?.market?.priceAsset, limit)
                .onErrorResumeNext(Observable.just(arrayListOf()))
    }


    fun loadCandlesOnInterval(watchMarket: WatchMarket?,
                              timeFrame: Int,
                              from: Long,
                              to: Long): Observable<List<Candle>> {
        return dataFeedService.getCandlesOnInterval(watchMarket?.market?.amountAsset,
                watchMarket?.market?.priceAsset, timeFrame, from, to)
                .flatMap { candles ->
                    Collections.sort<Candle>(candles) { o1, o2 -> o1.timestamp!!.compareTo(o2.timestamp) }
                    Observable.just<List<Candle>>(candles)
                }
    }
}
