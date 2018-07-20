package com.wavesplatform.wallet.v2.ui.receive.cryptocurrency

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import javax.inject.Inject

@InjectViewState
class СryptocurrencyPresenter @Inject constructor() : BasePresenter<СryptocurrencyView>() {
    var assetBalance: AssetBalance? = null

}
