/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.quick_action.receive.card

import com.wavesplatform.sdk.net.model.response.AssetBalanceResponse
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface CardView : BaseMvpView {
    fun showWaves(asset: AssetBalanceResponse?)
    fun showRate(rate: String?)
    fun showLimits(min: String?, max: String?, fiat: String?)
    fun showError(message: String)
    fun onGatewayError()
}
