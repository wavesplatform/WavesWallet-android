/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.dex

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryAllAsSingle
import com.wavesplatform.sdk.model.request.data.PairRequest
import com.wavesplatform.wallet.v2.data.model.remote.response.WatchMarketResponse
import com.wavesplatform.wallet.v2.data.model.db.userdb.MarketResponseDb
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.sdk.utils.RxUtil
import com.wavesplatform.wallet.v2.util.EnvironmentManager
import com.wavesplatform.wallet.v2.util.PrefsUtil
import com.wavesplatform.wallet.v2.util.executeInBackground
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import pyxis.uzuki.live.richutilskt.utils.runAsync
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@InjectViewState
class DexPresenter @Inject constructor() : BasePresenter<DexView>() {

    private var pairSubscriptions = CompositeDisposable()
    var hideShadow: Boolean = true

    fun loadActiveMarkets() {
        clearOldPairsSubscriptions()
        runAsync {
            addSubscription(queryAllAsSingle<MarketResponseDb>().toObservable()
                    .map { markets ->
                        return@map markets.sortedBy { it.position }.mapTo(ArrayList()) { market ->
                            return@mapTo WatchMarketResponse(market.convertFromDb())
                        }
                    }
                    .compose(RxUtil.applyObservableDefaultSchedulers())
                    .subscribe({ markets ->
                        viewState.afterSuccessLoadMarkets(markets)
                        loadPricesWithInterval(markets)
                    }, {
                        it.printStackTrace()
                    }))
        }
    }

    fun loadPricesWithInterval(markets: MutableList<WatchMarketResponse>) {
        pairSubscriptions.add(Observable.interval(0, 30, TimeUnit.SECONDS)
                .retry(3)
                .flatMap {
                    val activeAssetsIds = markets.map { it.market.amountAsset + "/" + it.market.priceAsset }

                    return@flatMap dataServiceManager.loadPairs(PairRequest(
                            pairs = activeAssetsIds,
                            matcher = EnvironmentManager.getMatcherAddress()))
                            .doOnNext {
                                prefsUtil.setValue(PrefsUtil.KEY_LAST_UPDATE_DEX_INFO, EnvironmentManager.getTime())
                            }
                }
                .onErrorResumeNext(Observable.empty())
                .executeInBackground()
                .subscribe({
                    markets.forEachIndexed { index, watchMarketResponse ->
                        watchMarketResponse.pairResponse = it.data[index].data
                    }
                    viewState.afterSuccessLoadMarkets(markets)
                }, {
                    viewState.afterFailedLoadPairInfo()
                    it.printStackTrace()
                }))
    }

    fun clearOldPairsSubscriptions() {
        pairSubscriptions.clear()
    }
}
