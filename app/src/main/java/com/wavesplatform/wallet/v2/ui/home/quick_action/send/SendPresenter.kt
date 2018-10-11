package com.wavesplatform.wallet.v2.ui.home.quick_action.send

import android.text.TextUtils
import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryAsSingle
import com.vicpin.krealmextensions.queryFirst
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.request.TransferTransactionRequest
import com.wavesplatform.wallet.v1.ui.assets.PaymentConfirmationDetails
import com.wavesplatform.wallet.v1.util.AddressUtil
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.manager.CoinomatManager
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetInfo
import com.wavesplatform.wallet.v2.data.model.remote.response.IssueTransaction
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import io.reactivex.Single
import pyxis.uzuki.live.richutilskt.utils.runAsync
import pyxis.uzuki.live.richutilskt.utils.runOnUiThread
import javax.inject.Inject

@InjectViewState
class SendPresenter @Inject constructor() : BasePresenter<SendView>() {

    @Inject
    lateinit var coinomatManager: CoinomatManager

    var selectedAsset: AssetBalance? = null
    var address: String? = ""
    var amount: String? = ""

    fun sendClicked() {
        val res = validateTransfer(getTxRequest())
        if (res == 0) {
            confirmPayment()
        } else {
            viewState.onShowError(res)
        }
    }

    private fun getTxRequest(): TransferTransactionRequest {
        return TransferTransactionRequest(
                selectedAsset!!.assetId,
                App.getAccessManager().getWallet()!!.publicKeyStr,
                address,
                MoneyUtil.getUnscaledValue(amount, selectedAsset),
                System.currentTimeMillis(),
                MoneyUtil.getUnscaledWaves(CUSTOM_FEE),
                "")
    }

    private fun confirmPayment() {
        val details = PaymentConfirmationDetails.fromRequest(
                selectedAsset, getTxRequest())
        viewState.onShowPaymentDetails(details)
    }

    private fun validateTransfer(tx: TransferTransactionRequest): Int {
        if (selectedAsset == null || TextUtils.isEmpty(address) || TextUtils.isEmpty(amount)) {
            R.string.send_transaction_error_check_fields
        } else if (!AddressUtil.isValidAddress(tx.recipient)) {
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
            tx.amount + tx.fee <= selectedAsset!!.balance!!
        } else {
            tx.amount <= selectedAsset!!.balance!!
                    && tx.fee <= queryFirst<AssetBalance> {
                equalTo("assetId", "")
            }?.balance ?: 0
        }
    }

    private fun isSameSendingAndFeeAssets(): Boolean {
        if (selectedAsset != null) {
            if (feeAsset.assetId == null && selectedAsset!!.assetId == null) {
                return true
            } else {
                if (feeAsset.assetId != null && selectedAsset!!.assetId != null)
                    return feeAsset.assetId == selectedAsset!!.assetId
            }
        }
        return false
    }

    fun loadXRate(asset: AssetBalance) {
        runAsync {
            val findAsset: Single<List<AssetInfo>> = queryAsSingle { equalTo("id", asset.assetId) }
            addSubscription(
                    findAsset.toObservable().flatMap {
                        val currencyTo = it[0].ticker
                        val currencyFrom = "W$currencyTo"
                        coinomatManager.getXRate(currencyFrom, currencyTo, LANG)
                    }
                            .subscribe({ xRate ->
                                runOnUiThread {
                                    viewState.showXRate(xRate)
                                }
                            }, {
                                viewState.onShowError(R.string.receive_error_network)
                            }))
        }
    }

    companion object {
        const val CUSTOM_FEE: String = "0.001"
        const val CUSTOM_FEE_ASSET_NAME: String = "Waves"
        private const val LANG: String = "ru_RU"
        private val feeAsset: AssetBalance = AssetBalance(
                quantity = 100000000L * 100000000L,
                issueTransaction = IssueTransaction(
                        name = CUSTOM_FEE_ASSET_NAME, quantity = 0, decimals = 8))
    }
}
