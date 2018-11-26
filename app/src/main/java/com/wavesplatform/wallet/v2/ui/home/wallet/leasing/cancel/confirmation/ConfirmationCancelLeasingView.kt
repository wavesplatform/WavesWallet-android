package com.wavesplatform.wallet.v2.ui.home.wallet.leasing.cancel.confirmation

import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface ConfirmationCancelLeasingView : BaseMvpView{
    fun successCancelLeasing()
    fun failedCancelLeasing()
}
