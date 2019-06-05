/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.dex.trade

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.sdk.model.response.WatchMarketResponse
import com.wavesplatform.sdk.model.response.AssetInfoResponse
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.sdk.utils.RxUtil
import javax.inject.Inject

@InjectViewState
class TradePresenter @Inject constructor() : BasePresenter<TradeView>() {
    var watchMarket: WatchMarketResponse? = null
    var amountAssetInfo: AssetInfoResponse? = null
    var priceAssetInfo: AssetInfoResponse? = null

    fun loadAssetsInfoOfPair() {
        addSubscription(apiDataManager.assetsInfoByIds(arrayListOf(watchMarket?.market?.amountAsset, watchMarket?.market?.priceAsset))
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe { assetsInfo ->
                    val map = assetsInfo.associateBy { it.id }
                    amountAssetInfo = map[watchMarket?.market?.amountAsset]
                    priceAssetInfo = map[watchMarket?.market?.priceAsset]
                })
    }
}
