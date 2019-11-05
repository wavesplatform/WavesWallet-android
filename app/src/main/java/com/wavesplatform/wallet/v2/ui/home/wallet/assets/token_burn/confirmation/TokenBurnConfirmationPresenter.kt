/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet.assets.token_burn.confirmation

import moxy.InjectViewState
import com.wavesplatform.sdk.model.request.node.BurnTransaction
import com.wavesplatform.wallet.App
import com.wavesplatform.sdk.model.response.node.AssetBalanceResponse
import com.wavesplatform.sdk.utils.MoneyUtil
import com.wavesplatform.sdk.utils.isSmartError
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.errorBody
import com.wavesplatform.wallet.v2.util.executeInBackground
import javax.inject.Inject

@InjectViewState
class TokenBurnConfirmationPresenter @Inject constructor() : BasePresenter<TokenBurnConfirmationView>() {

    var assetBalance: AssetBalanceResponse? = null
    var amount: Double = 0.0
    var fee = 0L
    var success = false
    var totalBurn = false

    fun burn() {
        val quantity = MoneyUtil.getUnscaledValue(amount.toString(), assetBalance)
        totalBurn = quantity >= assetBalance?.balance ?: 0

        val request = BurnTransaction(
                assetId = assetBalance!!.assetId,
                quantity = quantity)
        request.fee = fee
        request.sign(App.getAccessManager().getWallet()?.seedStr ?: "")

        addSubscription(nodeServiceManager.burn(request, totalBurn)
                .executeInBackground()
                .subscribe({
                    success = true
                    viewState.onShowBurnSuccess(it, totalBurn)
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
