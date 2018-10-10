package com.wavesplatform.wallet.v2.ui.home.quick_action.send

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.payload.IssueTransaction
import com.wavesplatform.wallet.v1.request.TransferTransactionRequest
import com.wavesplatform.wallet.v1.ui.assets.PaymentConfirmationDetails
import com.wavesplatform.wallet.v1.ui.customviews.ToastCustom
import com.wavesplatform.wallet.v1.util.AddressUtil
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import javax.inject.Inject

@InjectViewState
class SendPresenter @Inject constructor() : BasePresenter<SendView>() {

    var selectedAsset: AssetBalance? = null

    //Views
    var destinationAddress: String? = "3P6mMPokbqHKRh6e5PoiStxY2EAmc7fzN79"
    private var customFee: String? = "0.001"
    private var amount: String? = "0.00000001"
    private var attachment: String? = "yo send"

    var sendingAsset: com.wavesplatform.wallet.v1.payload.AssetBalance? = null
    private var feeAsset: com.wavesplatform.wallet.v1.payload.AssetBalance

    init {
        val issueTransaction = IssueTransaction()
        issueTransaction.decimals = 8
        issueTransaction.quantity = 0
        issueTransaction.name = "WAVES"
        feeAsset = com.wavesplatform.wallet.v1.payload.AssetBalance()
        feeAsset.quantity = 100000000L * 100000000L
        feeAsset.issueTransaction = issueTransaction
    }

    fun sendClicked() {
        val res = validateTransfer(getTxRequest())
        if (res == 0) {
            confirmPayment()
        } else {
            viewState.onShowError(res, ToastCustom.TYPE_ERROR)
        }
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

    /**
     * Sets payment confirmation details to be displayed to user and fires callback to display
     * this.
     */
    private fun confirmPayment() {
        val details = PaymentConfirmationDetails.fromRequest(
                sendingAsset, getTxRequest())
        viewState.onShowPaymentDetails(details)
    }

    private fun validateTransfer(tx: TransferTransactionRequest): Int {
        if (!AddressUtil.isValidAddress(tx.recipient)) {
            return R.string.invalid_address
        } else if (tx.attachmentSize > TransferTransactionRequest.MaxAttachmentSize) {
            return R.string.attachment_too_long
        } else if (tx.amount <= 0) {
            return R.string.invalid_amount
        } else if (tx.amount > java.lang.Long.MAX_VALUE - tx.fee) {
            return R.string.invalid_amount
        } else if (tx.fee <= 0 || tx.fee < TransferTransactionRequest.MinFee) {
            return R.string.insufficient_fee
        } else if (App.getAccessManager().getWallet()!!.address == tx.recipient) {
            return R.string.send_to_same_address_warning
        } else if (!isFundSufficient(tx)) {
            return R.string.insufficient_funds
        }

        return 0
    }

    private fun isFundSufficient(tx: TransferTransactionRequest): Boolean {
        return if (isSameSendingAndFeeAssets()) {
            tx.amount + tx.fee <= sendingAsset!!.balance
        } else {
            tx.amount <= sendingAsset!!.balance && tx.fee <= 100001
                    //todo NodeManager.get().wavesBalance
        }
    }

    private fun isSameSendingAndFeeAssets(): Boolean {
        if (sendingAsset != null) {
            if (feeAsset.assetId == null && sendingAsset!!.assetId == null) {
                return true
            } else {
                if (feeAsset.assetId != null && sendingAsset!!.assetId != null)
                    return feeAsset.assetId == sendingAsset!!.assetId
            }
        }
        return false
    }
}
