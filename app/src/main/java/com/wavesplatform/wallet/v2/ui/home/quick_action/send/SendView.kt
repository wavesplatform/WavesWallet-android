/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.quick_action.send

import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.data.model.remote.response.gateway.GatewayMetadata
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface SendView : BaseMvpView {
    fun onLoadMetadataSuccess(ticker: GatewayMetadata, gatewayTicket: String?)
    fun onLoadMetadataError()

    fun onShowError(errorMsgRes: Int)
    fun onShowPaymentDetails()
    fun setRecipientValid(valid: Boolean?)
    fun showCommissionLoading()
    fun showCommissionSuccess(unscaledAmount: Long)
    fun showCommissionError()
    fun showLoadAssetSuccess(assetBalance: AssetBalance)
    fun showLoadAssetError(errorMsgRes: Int)
    fun setDataFromUrl(url: String?)
}
