/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet.assets.token_burn.confirmation

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.model.remote.request.BurnRequest
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.errorBody
import com.wavesplatform.wallet.v2.util.executeInBackground
import com.wavesplatform.wallet.v2.util.isSmartError
import javax.inject.Inject

@InjectViewState
class TokenBurnConfirmationPresenter @Inject constructor() : BasePresenter<TokenBurnConfirmationView>() {

    var assetBalance: AssetBalance? = null
    var amount: Double = 0.0
    var fee = 0L
    var success = false
    var totalBurn = false

    fun burn() {
        val quantity = MoneyUtil.getUnscaledValue(amount.toString(), assetBalance)
        totalBurn = quantity >= assetBalance?.balance ?: 0

        val request = BurnRequest(
                assetId = assetBalance!!.assetId,
                fee = fee,
                quantity = quantity,
                senderPublicKey = App.getAccessManager().getWallet()!!.publicKeyStr)

        addSubscription(nodeDataManager.burn(request, totalBurn)
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
