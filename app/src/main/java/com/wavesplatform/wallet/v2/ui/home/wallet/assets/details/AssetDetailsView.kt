/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet.assets.details

import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType
import com.wavesplatform.sdk.model.response.node.AssetBalanceResponse
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

@StateStrategyType(SkipStrategy::class)
interface AssetDetailsView : BaseMvpView {
    fun afterSuccessLoadAssets(sortedToFirstFavoriteList: MutableList<AssetBalanceResponse>)
    fun afterSuccessLoadTransaction()
}
