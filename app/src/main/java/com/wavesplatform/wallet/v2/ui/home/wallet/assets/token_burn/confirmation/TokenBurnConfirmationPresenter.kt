/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet.assets.token_burn.confirmation

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.sdk.model.request.node.BurnTransaction
import com.wavesplatform.wallet.App
import com.wavesplatform.sdk.model.response.node.AssetBalanceResponse
import com.wavesplatform.sdk.utils.RxUtil
import com.wavesplatform.sdk.utils.isSmartError
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.errorBody
import javax.inject.Inject

@InjectViewState
class TokenBurnConfirmationPresenter @Inject constructor() : BasePresenter<TokenBurnConfirmationView>() {

    var assetBalance: AssetBalanceResponse? = null
    var amount: Double = 0.0
    var fee = 0L

    var success = false

    fun burn() {
        val decimals = assetBalance!!.getDecimals()
        val quantity = if (amount == 0.0) {
            0
        } else {
            (amount * Math.pow(10.0, decimals.toDouble())).toLong()
        }

        val request = BurnTransaction(
                assetId = assetBalance!!.assetId,
                quantity = quantity)
        request.sign(App.getAccessManager().getWallet().seedStr)

        addSubscription(nodeDataManager.burn(request)
                .compose(RxUtil.applySchedulersToObservable())
                .subscribe({
                    success = true
                    viewState.onShowBurnSuccess(it, quantity >= assetBalance?.balance ?: 0)
                }, {
                    it.errorBody()?.let { error ->
                        if (error.isSmartError()) {
                            viewState.failedTokenBurnCauseSmart()
                        } else {
                            viewState.onShowError(error.message)
                        }
                    }
                }))
    }
}
