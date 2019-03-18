package com.wavesplatform.wallet.v2.ui.home.wallet.assets.token_burn.confirmation

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.sdk.net.model.request.BurnRequest
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v2.util.RxUtil
import com.wavesplatform.sdk.net.model.response.AssetBalance
import com.wavesplatform.sdk.utils.isSmartError
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.errorBody
import javax.inject.Inject

@InjectViewState
class TokenBurnConfirmationPresenter @Inject constructor() : BasePresenter<TokenBurnConfirmationView>() {

    var assetBalance: AssetBalance? = null
    var amount: Double = 0.0
    var fee = 0L

    fun burn() {
        val decimals = assetBalance!!.getDecimals()
        val quantity = if (amount == 0.0) {
            0
        } else {
            (amount * Math.pow(10.0, decimals.toDouble())).toLong()
        }

        val request = BurnRequest(
                assetId = assetBalance!!.assetId,
                fee = fee,
                quantity = quantity,
                senderPublicKey = App.getAccessManager().getWallet()!!.publicKeyStr)
        request.sign(App.getAccessManager().getWallet()!!.privateKey)

        addSubscription(nodeDataManager.burn(request)
                .compose(RxUtil.applySchedulersToObservable()).subscribe({
                    viewState.onShowBurnSuccess(it,
                            quantity >= assetBalance?.getAvailableBalance() ?: 0)
                }, {
                    if (it.errorBody()?.isSmartError() == true) {
                        viewState.failedTokenBurnCauseSmart()
                    } else {
                        it.errorBody()?.let {
                            viewState.onShowError(it.message)
                        }
                    }
                }))
    }
}
