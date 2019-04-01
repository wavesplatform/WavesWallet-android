/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet.assets.token_burn.confirmation

import com.wavesplatform.wallet.v2.data.model.remote.request.BurnRequest
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface TokenBurnConfirmationView : BaseMvpView {
    fun onShowBurnSuccess(tx: BurnRequest?, totalBurn: Boolean)
    fun onShowError(errorMessageRes: String)
    fun failedTokenBurnCauseSmart()
}
