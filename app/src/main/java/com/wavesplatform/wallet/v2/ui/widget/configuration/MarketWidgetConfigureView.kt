/*
 * Created by Eduard Zaydel on 8/8/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.widget.configuration

import com.wavesplatform.sdk.model.response.data.AssetInfoResponse
import com.wavesplatform.sdk.model.response.data.SearchPairResponse
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface MarketWidgetConfigureView : BaseMvpView {
    fun onUpdatePairs(assetPairList: ArrayList<MarketWidgetConfigurationMarketsAdapter.TokenPair>)
    fun onUpdatePair(assetInfo: AssetInfoResponse, searchPairResponse: SearchPairResponse)
    fun onFailGetMarkets()
}