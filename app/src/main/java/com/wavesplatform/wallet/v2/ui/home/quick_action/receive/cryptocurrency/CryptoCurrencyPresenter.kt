/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.quick_action.receive.cryptocurrency

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.data.rxjava.RxUtil
import com.wavesplatform.wallet.v1.ui.auth.EnvironmentManager
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.manager.gateway.manager.CoinomatDataManager
import com.wavesplatform.wallet.v2.data.manager.gateway.manager.GatewayDataManager
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.clearBalance
import javax.inject.Inject

@InjectViewState
class CryptoCurrencyPresenter @Inject constructor() : BasePresenter<CryptoCurrencyView>() {

    @Inject
    lateinit var coinomatManager: CoinomatDataManager
    @Inject
    lateinit var gatewayDataManager: GatewayDataManager

    var assetBalance: AssetBalance? = null
    private var lang: String = "ru_RU"
    var nextStepValidation = false

    var depositAddress: String? = null

    fun initDeposit(assetId: String) {
        val asset = EnvironmentManager.globalConfiguration.generalAssets.find { it.assetId == assetId }

        when (asset?.gatewayType) {
            Constants.GatewayType.COINOMAT -> {
                initCoinomatDeposit(assetId)
            }
            Constants.GatewayType.GATEWAY -> {
                initGatewayDeposit(assetId)
            }
            else -> {
                initCoinomatDeposit(assetId)
            }
        }
    }

    private fun initGatewayDeposit(assetId: String) {
        val currencyFrom = Constants.coinomatCryptoCurrencies()[assetId]

        addSubscription(gatewayDataManager.receiveTransaction(getWavesAddress(), assetId)
                .compose(RxUtil.applySchedulersToObservable())
                .subscribe({ response ->
                    depositAddress = response.address
                    val gatewayMin = MoneyUtil.getScaledText(response.minAmount, assetBalance).clearBalance().toBigDecimal()
                    viewState.onSuccessInitDeposit(currencyFrom, gatewayMin)
                }, {
                    viewState.onGatewayError()
                    it.printStackTrace()
                }))
    }

    private fun initCoinomatDeposit(assetId: String) {
        val currencyFrom = Constants.coinomatCryptoCurrencies()[assetId]
        if (currencyFrom.isNullOrEmpty()) {
            viewState.onShowError(App.getAppContext()
                    .getString(R.string.receive_error_network))
            return
        }

        val currencyTo = "W$currencyFrom"

        addSubscription(coinomatManager.createTunnel(currencyFrom, currencyTo, getWavesAddress(), null)
                .flatMap { createTunnel ->
                    coinomatManager.getTunnel(
                            createTunnel.tunnelId,
                            createTunnel.k1,
                            createTunnel.k2,
                            lang)
                }
                .compose(RxUtil.applySchedulersToObservable())
                .subscribe({ tunnel ->
                    depositAddress = tunnel.tunnel?.walletFrom
                    viewState.onSuccessInitDeposit(currencyFrom, tunnel.tunnel?.inMin?.toBigDecimal())
                }, {
                    viewState.onGatewayError()
                    it.printStackTrace()
                }))
    }
}
