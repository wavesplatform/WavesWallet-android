/*
 * Created by Eduard Zaydel on 8/8/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.widget.configuration

import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface MarketWidgetConfigureView : BaseMvpView {
    fun onUpdatePairs(assetPairList: List<MarketWidgetConfigurationMarketsAdapter.TokenPair>)
    fun onAddPairs(assetPairList: List<MarketWidgetConfigurationMarketsAdapter.TokenPair>)
    fun onFailGetMarkets()
}