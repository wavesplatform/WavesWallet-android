package com.wavesplatform.wallet.v2.ui.home.quick_action.receive.cryptocurrency

import com.wavesplatform.wallet.v2.data.model.remote.response.coinomat.GetTunnel
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface СryptocurrencyView : BaseMvpView{
    fun showTunnel(tunnel: GetTunnel?)
    fun showError(message: String?)

}
