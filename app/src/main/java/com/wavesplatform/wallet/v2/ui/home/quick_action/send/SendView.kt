package com.wavesplatform.wallet.v2.ui.home.quick_action.send

import com.wavesplatform.wallet.v1.ui.assets.PaymentConfirmationDetails
import com.wavesplatform.wallet.v2.data.model.remote.response.coinomat.XRate
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface SendView : BaseMvpView {

    fun onShowError(res: Int)
    fun onShowPaymentDetails(details: PaymentConfirmationDetails)
    fun showXRate(xRate: XRate?)
    fun showXRateError()

}
