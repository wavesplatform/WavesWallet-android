package com.wavesplatform.wallet.v2.ui.home.quick_action.receive.invoice

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.data.manager.CoinomatManager
import com.wavesplatform.sdk.net.model.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import javax.inject.Inject

@InjectViewState
class InvoicePresenter @Inject constructor() : BasePresenter<InvoiceView>() {

    @Inject
    lateinit var coinomatManager: CoinomatManager
    var assetBalance: AssetBalance? = null
}
