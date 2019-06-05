/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.dex.markets

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.deleteAll
import com.vicpin.krealmextensions.saveAll
import com.wavesplatform.sdk.model.response.MarketResponse
import com.wavesplatform.wallet.v2.data.model.db.userdb.MarketResponseDb
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.sdk.utils.RxUtil
import pyxis.uzuki.live.richutilskt.utils.runAsync
import javax.inject.Inject

@InjectViewState
class DexMarketsPresenter @Inject constructor() : BasePresenter<DexMarketsView>() {
    var needToUpdate: Boolean = false

    fun getMarkets() {
        runAsync {
            addSubscription(matcherDataManager.getAllMarkets()
                    .compose(RxUtil.applyObservableDefaultSchedulers())
                    .subscribe({
                        viewState.afterSuccessGetMarkets(it)
                    }, {
                        viewState.afterFailGetMarkets()
                        it.printStackTrace()
                    }))
        }
    }

    fun saveSelectedMarkets(data: List<MarketResponse>) {
        deleteAll<MarketResponseDb>()
        val selectedMarkets = data.filter { it.checked }
        MarketResponseDb.convertToDb(selectedMarkets).saveAll()
    }
}
