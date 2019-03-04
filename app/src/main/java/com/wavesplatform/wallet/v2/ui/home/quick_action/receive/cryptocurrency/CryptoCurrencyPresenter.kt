package com.wavesplatform.wallet.v2.ui.home.quick_action.receive.cryptocurrency

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.data.rxjava.RxUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.manager.CoinomatManager
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.data.model.remote.response.coinomat.GetTunnel
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import javax.inject.Inject

@InjectViewState
class CryptoCurrencyPresenter @Inject constructor() : BasePresenter<CryptoCurrencyView>() {

    @Inject
    lateinit var coinomatManager: CoinomatManager
    var assetBalance: AssetBalance? = null
    var tunnel: GetTunnel? = null
    private var address: String? = App.getAccessManager().getWallet()!!.address
    private var lang: String = "ru_RU"
    var nextStepValidation = false

    fun getTunnel(assetId: String) {
        val currencyFrom = Constants.coinomatCryptoCurrencies()[assetId]
        if (currencyFrom.isNullOrEmpty()) {
            viewState.onShowError(App.getAppContext()
                    .getString(R.string.receive_error_network))
            return
        }

        val currencyTo = "W$currencyFrom"

        addSubscription(coinomatManager.createTunnel(currencyFrom, currencyTo, address, null)
                .flatMap { createTunnel ->
                    coinomatManager.getTunnel(
                            createTunnel.tunnelId,
                            createTunnel.k1,
                            createTunnel.k2,
                            lang)
                }
                .compose(RxUtil.applySchedulersToObservable())
                .subscribe({ tunnel ->
                    this.tunnel = tunnel
                    viewState.onShowTunnel(tunnel)
                }, {
                    viewState.onGatewayError()
                    it.printStackTrace()
                }))
    }
}
