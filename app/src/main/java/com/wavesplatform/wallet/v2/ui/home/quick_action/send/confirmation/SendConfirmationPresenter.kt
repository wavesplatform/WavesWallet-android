package com.wavesplatform.wallet.v2.ui.home.quick_action.send.confirmation

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryFirst
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.api.NodeManager
import com.wavesplatform.wallet.v1.data.rxjava.RxUtil
import com.wavesplatform.wallet.v1.request.TransferTransactionRequest
import com.wavesplatform.wallet.v1.ui.customviews.ToastCustom
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookUser
import javax.inject.Inject

@InjectViewState
class SendConfirmationPresenter @Inject constructor() : BasePresenter<SendConfirmationView>() {

    private var nodeManager = NodeManager.createInstance(
            App.getAccessManager().getWallet()!!.publicKeyStr)

    var destinationAddress: String? = "0xfedfa2ec820e568da1b826a17c8c95e224cd1843"
    private var customFee: String? = "0.0001"
    private var amount: String? = "0.0001"
    private var attachment: String? = "yo send to ETH"

    var sendingAsset: com.wavesplatform.wallet.v1.payload.AssetBalance? = null
    var selectedAsset: AssetBalance? = null


    fun confirmSend() {
        val singed = signTransaction()
        if (singed != null) {
            submitPayment(singed)
        } else {
            viewState.requestPassCode()
        }
    }

    private fun signTransaction(): TransferTransactionRequest? {
        val pk = App.getAccessManager().getWallet()!!.privateKey
        return if (pk != null) {
            val signed = getTxRequest()
            signed.sign(pk)
            signed
        } else {
            null
        }
    }

    private fun submitPayment(signed: TransferTransactionRequest) {
        nodeManager.broadcastTransfer(signed)
                .compose(RxUtil.applySchedulersToObservable()).subscribe({ tx ->
                    viewState.onShowTransactionSuccess(signed)
                }, { err ->
                    viewState.onShowError(R.string.transaction_failed, ToastCustom.TYPE_ERROR)
                })
    }

    private fun getTxRequest(): TransferTransactionRequest {
        return TransferTransactionRequest(
                sendingAsset!!.assetId,
                App.getAccessManager().getWallet()!!.publicKeyStr,
                destinationAddress,
                MoneyUtil.getUnscaledValue(amount, sendingAsset),
                System.currentTimeMillis(),
                MoneyUtil.getUnscaledWaves(customFee),
                attachment)
    }

    fun getAddressName(address: String) {
        val addressBookUser = queryFirst<AddressBookUser> {
            equalTo("address", address)
        }
        if (addressBookUser == null) {
            viewState.hideAddressBookUser()
        } else {
            viewState.showAddressBookUser(addressBookUser.name)

        }
    }

}
