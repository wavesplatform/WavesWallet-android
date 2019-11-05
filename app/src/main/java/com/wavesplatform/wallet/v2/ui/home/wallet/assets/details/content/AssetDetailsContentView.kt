/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet.assets.details.content

import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType
import com.wavesplatform.wallet.v2.data.model.local.HistoryItem
import com.wavesplatform.sdk.model.response.node.AssetBalanceResponse
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

@StateStrategyType(SkipStrategy::class)
interface AssetDetailsContentView : BaseMvpView {
    fun showLastTransactions(data: MutableList<HistoryItem>)
    fun onAssetAddressBalanceLoadSuccess(assetBalance: AssetBalanceResponse)
}
