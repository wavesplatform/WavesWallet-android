package com.wavesplatform.wallet.v2.ui.home.quick_action.send

import com.wavesplatform.wallet.v1.ui.assets.PaymentConfirmationDetails
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface SendView : BaseMvpView {

    fun onShowError(res: Int, toastType: String)
    fun onShowPaymentDetails(details: PaymentConfirmationDetails)

}
