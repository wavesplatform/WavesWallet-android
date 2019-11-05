/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.dex.trade

import moxy.InjectViewState
import com.wavesplatform.sdk.model.response.data.AssetInfoResponse
import com.wavesplatform.wallet.v2.data.model.remote.response.WatchMarketResponse
import com.wavesplatform.sdk.utils.RxUtil
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import javax.inject.Inject

@InjectViewState
class TradePresenter @Inject constructor() : BasePresenter<TradeView>() {
    var watchMarket: WatchMarketResponse? = null
    var amountAssetInfo: AssetInfoResponse? = null
    var priceAssetInfo: AssetInfoResponse? = null

    fun loadAssetsInfoOfPair() {
        addSubscription(dataServiceManager.assets(
                ids = arrayListOf(watchMarket?.market?.amountAsset, watchMarket?.market?.priceAsset))
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe { assetsInfo ->
                    val map = assetsInfo.associateBy { it.id }
                    amountAssetInfo = map[watchMarket?.market?.amountAsset]
                    priceAssetInfo = map[watchMarket?.market?.priceAsset]
                })
    }
}
