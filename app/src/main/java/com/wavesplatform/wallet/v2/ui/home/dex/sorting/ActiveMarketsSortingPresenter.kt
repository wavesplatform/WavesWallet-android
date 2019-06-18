/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.dex.sorting

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryAllAsSingle
import com.vicpin.krealmextensions.saveAll
import com.wavesplatform.sdk.model.response.matcher.MarketResponse
import com.wavesplatform.wallet.v2.data.model.db.userdb.MarketResponseDb
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.sdk.utils.RxUtil
import pyxis.uzuki.live.richutilskt.utils.runAsync
import javax.inject.Inject

@InjectViewState
class ActiveMarketsSortingPresenter @Inject constructor() : BasePresenter<ActiveMarketsSortingView>() {
    var needToUpdate: Boolean = false

    fun loadMarkets() {
        runAsync {
            addSubscription(queryAllAsSingle<MarketResponseDb>().toObservable()
                    .compose(RxUtil.applyObservableDefaultSchedulers())
                    .subscribe({
                        val markets = it.sortedBy { it.position }.toMutableList()
                        viewState.afterSuccessLoadMarkets(MarketResponseDb.convertFromDb(markets))
                    }, {
                        it.printStackTrace()
                    }))
        }
    }

    fun saveSortedPositions(data: List<MarketResponse>) {
        runAsync {
            data.forEachIndexed { index, market ->
                market.position = index
            }
            MarketResponseDb.convertToDb(data).saveAll()
        }
    }
}
