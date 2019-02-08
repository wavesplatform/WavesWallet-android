package com.wavesplatform.wallet.v2.ui.home.quick_action.send

import com.wavesplatform.wallet.v2.data.model.local.PaymentConfirmationDetails
import com.wavesplatform.sdk.model.response.AssetBalance
import com.wavesplatform.sdk.model.response.coinomat.XRate
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface SendView : BaseMvpView {

    fun onShowError(errorMsgRes: Int)
    fun onShowPaymentDetails(details: PaymentConfirmationDetails)
    fun showXRate(xRate: XRate, ticker: String)
    fun showXRateError()
    fun setRecipientValid(valid: Boolean?)
    fun showCommissionLoading()
    fun showCommissionSuccess(unscaledAmount: Long)
    fun showCommissionError()
    fun showLoadAssetSuccess(assetBalance: AssetBalance)
    fun showLoadAssetError(errorMsgRes: Int)
}
