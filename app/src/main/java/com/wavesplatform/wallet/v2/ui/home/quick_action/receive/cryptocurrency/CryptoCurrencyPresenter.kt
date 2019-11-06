/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.quick_action.receive.cryptocurrency

import moxy.InjectViewState
import com.wavesplatform.sdk.model.response.node.AssetBalanceResponse
import com.wavesplatform.wallet.v2.data.manager.gateway.provider.GatewayProvider
import com.wavesplatform.wallet.v2.data.model.local.gateway.GatewayDepositArgs
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.executeInBackground
import javax.inject.Inject

@InjectViewState
class CryptoCurrencyPresenter @Inject constructor() : BasePresenter<CryptoCurrencyView>() {

    @Inject
    lateinit var gatewayProvider: GatewayProvider

    var assetBalance: AssetBalanceResponse? = null
    var nextStepValidation = false
    var depositAddress: String? = null

    fun initDeposit(assetId: String) {
        addSubscription(gatewayProvider.getGatewayDataManager(assetId)
                .makeDeposit(GatewayDepositArgs(assetBalance))
                .executeInBackground()
                .subscribe({ response ->
                    depositAddress = response.depositAddress
                    viewState.onSuccessInitDeposit(response)
                }, {
                    viewState.onGatewayError()
                    it.printStackTrace()
                }))
    }
}
