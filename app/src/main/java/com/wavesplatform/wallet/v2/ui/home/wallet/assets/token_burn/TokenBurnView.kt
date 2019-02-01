package com.wavesplatform.wallet.v2.ui.home.wallet.assets.token_burn

import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface TokenBurnView : BaseMvpView {
    fun showCommissionLoading()
    fun showCommissionSuccess(unscaledAmount: Long)
    fun showCommissionError()
}
