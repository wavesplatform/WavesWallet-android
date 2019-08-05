package com.wavesplatform.wallet.v2.ui.widget

import com.wavesplatform.sdk.model.response.data.AssetInfoResponse
import com.wavesplatform.sdk.model.response.data.SearchPairResponse
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView
import com.wavesplatform.wallet.v2.ui.widget.adapters.TokenAdapter

interface MarketWidgetConfigureView : BaseMvpView {
    fun onUpdatePairs(assetPairList: ArrayList<TokenAdapter.TokenPair>)
    fun onUpdatePair(assetInfo: AssetInfoResponse, searchPairResponse: SearchPairResponse)
    fun onFailGetMarkets()
}