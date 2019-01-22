package com.wavesplatform.wallet.v2.ui.home.wallet.leasing.start

import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface StartLeasingView : BaseMvpView {
    fun afterSuccessLoadWavesBalance(waves: Long)
    fun showCommissionLoading()
    fun showCommissionSuccess(unscaledAmount: Long)
    fun showCommissionError()
}
