/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.dex

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryAllAsSingle
import com.wavesplatform.sdk.model.response.data.WatchMarketResponse
import com.wavesplatform.wallet.v2.data.model.db.userdb.MarketResponseDb
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.sdk.utils.RxUtil
import io.reactivex.disposables.CompositeDisposable
import pyxis.uzuki.live.richutilskt.utils.runAsync
import javax.inject.Inject

@InjectViewState
class DexPresenter @Inject constructor() : BasePresenter<DexView>() {

    private var pairSubscriptions = CompositeDisposable()
    var hideShadow: Boolean = true

    fun loadActiveMarkets() {
        runAsync {
            addSubscription(queryAllAsSingle<MarketResponseDb>().toObservable()
                    .compose(RxUtil.applyObservableDefaultSchedulers())
                    .subscribe({
                        val markets = it.sortedBy { it.position }.mapTo(ArrayList()) {
                            return@mapTo WatchMarketResponse(it.convertFromDb())
                        }
                        viewState.afterSuccessLoadMarkets(markets)
                    }, {
                        it.printStackTrace()
                    }))
        }
    }

    fun loadDexPairInfo(watchMarket: WatchMarketResponse, index: Int) {
        pairSubscriptions.add(dataServiceManager.loadDexPairInfo(watchMarket)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({
                    viewState.afterSuccessLoadPairInfo(it, index)
                }, {
                    viewState.stopLoading()
                    it.printStackTrace()
                }, {
                    viewState.stopLoading()
                }))
    }

    fun clearOldPairsSubscriptions() {
        pairSubscriptions.clear()
    }
}
