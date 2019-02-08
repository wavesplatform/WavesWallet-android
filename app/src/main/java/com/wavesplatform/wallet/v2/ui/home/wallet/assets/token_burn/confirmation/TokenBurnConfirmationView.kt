package com.wavesplatform.wallet.v2.ui.home.wallet.assets.token_burn.confirmation

import com.wavesplatform.sdk.model.request.BurnRequest
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface TokenBurnConfirmationView : BaseMvpView {
    fun onShowBurnSuccess(tx: BurnRequest?, totalBurn: Boolean)
    fun onShowError(errorMessageRes: String)
    fun failedTokenBurnCauseSmart()
}
