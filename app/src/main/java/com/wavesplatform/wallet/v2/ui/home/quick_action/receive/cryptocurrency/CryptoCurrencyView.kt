package com.wavesplatform.wallet.v2.ui.home.quick_action.receive.cryptocurrency

import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.wavesplatform.sdk.net.model.response.coinomat.GetTunnel
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

@StateStrategyType(SkipStrategy::class)
interface CryptoCurrencyView : BaseMvpView {
    fun onShowTunnel(tunnel: GetTunnel?)
    fun onShowError(message: String)
    fun onGatewayError()
}
