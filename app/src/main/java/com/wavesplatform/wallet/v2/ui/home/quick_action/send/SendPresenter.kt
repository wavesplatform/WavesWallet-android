package com.wavesplatform.wallet.v2.ui.home.quick_action.send

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryFirst
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.crypto.Base58
import com.wavesplatform.wallet.v1.crypto.Hash
import com.wavesplatform.wallet.v1.request.TransferTransactionRequest
import com.wavesplatform.wallet.v1.ui.assets.PaymentConfirmationDetails
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.manager.CoinomatManager
import com.wavesplatform.wallet.v2.data.model.remote.request.TransactionsBroadcastRequest
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.data.model.remote.response.IssueTransaction
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.isValidAddress
import pyxis.uzuki.live.richutilskt.utils.runAsync
import pyxis.uzuki.live.richutilskt.utils.runOnUiThread
import javax.inject.Inject

@InjectViewState
class SendPresenter @Inject constructor() : BasePresenter<SendView>() {

    @Inject
    lateinit var coinomatManager: CoinomatManager

    var selectedAsset: AssetBalance? = null
    var recipient: String? = ""
    var amount: String? = ""
    var attachment: String? = ""
    var moneroPaymentId: String? = null
    var recipientAssetId: String? = null
    var type: Type? = null

    fun sendClicked() {
        val res = validateTransfer()
        if (res == 0) {
            confirmPayment()
        } else {
            viewState.onShowError(res)
        }
    }

    fun checkAlias(alias: String) {
        if (alias.length in 4..30) {
            runAsync {
                addSubscription(
                        apiDataManager.loadAlias(alias)

                                .subscribe({ _ ->
                                    type = Type.ALIAS
                                    runOnUiThread {
                                        viewState.setRecipientValid(true)
                                    }
                                }, {
                                    type = Type.UNKNOWN
                                    runOnUiThread {
                                        viewState.setRecipientValid(false)
                                    }
                                }))
            }
        } else {
            type = Type.UNKNOWN
            runOnUiThread {
                viewState.setRecipientValid(null)
            }
        }
    }

    private fun getTxRequest(): TransactionsBroadcastRequest {
        return TransactionsBroadcastRequest(
                selectedAsset!!.assetId ?: "",
                App.getAccessManager().getWallet()!!.publicKeyStr,
                recipient ?: "",
                MoneyUtil.getUnscaledValue(amount, selectedAsset),
                System.currentTimeMillis(),
                Constants.WAVES_FEE,
                "")
    }

    private fun confirmPayment() {
        val details = PaymentConfirmationDetails.fromRequest(
                selectedAsset, getTxRequest())
        viewState.onShowPaymentDetails(details)
    }

    private fun validateTransfer(): Int {
        if (selectedAsset == null) {
            return R.string.send_transaction_error_check_asset
        } else if (isRecipientValid() != true) {
            return R.string.invalid_address
        } else {
            val tx = getTxRequest()
            if (TransactionsBroadcastRequest.getAttachmentSize(tx.attachment)
                    > TransferTransactionRequest.MaxAttachmentSize) {
                return R.string.attachment_too_long
            } else
                if (tx.amount <= 0 || tx.amount > java.lang.Long.MAX_VALUE - tx.fee) {
                    return R.string.invalid_amount
                } else if (tx.fee <= 0 || tx.fee < TransferTransactionRequest.MinFee) {
                    return R.string.insufficient_fee
                } else if (App.getAccessManager().getWallet()!!.address == tx.recipient) {
                    return R.string.send_to_same_address_warning
                } else if (!isFundSufficient(tx)) {
                    return R.string.insufficient_funds
                } else if (Constants.MONERO_ASSET_ID == recipientAssetId
                        && moneroPaymentId != null
                        && (moneroPaymentId!!.length != MONERO_PAYMENT_ID_LENGTH
                                || !moneroPaymentId!!.matches(" ".toRegex()))) {
                    return R.string.invalid_monero_payment_id
                }
        }
        return 0
    }

    private fun isFundSufficient(tx: TransactionsBroadcastRequest): Boolean {
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

    fun loadXRate(assetId: String) {
        val currencyTo = Constants.coinomatCryptoCurrencies[assetId]
        if (currencyTo.isNullOrEmpty()) {
            type = SendPresenter.Type.UNKNOWN
            runOnUiThread {
                viewState.showXRateError()
            }
            return
        }

        val currencyFrom = "W$currencyTo"
        runAsync {
            addSubscription(coinomatManager.getXRate(currencyFrom, currencyTo, LANG)
                    .subscribe({ xRate ->
                        type = SendPresenter.Type.GATEWAY
                        runOnUiThread {
                            if (xRate == null) {
                                viewState.showXRateError()
                            } else {
                                viewState.showXRate(xRate, currencyTo!!)
                            }
                        }
                    }, {
                        type = SendPresenter.Type.UNKNOWN
                        runOnUiThread {
                            viewState.showXRateError()
                        }
                    }))
        }
    }

    fun isRecipientValid(): Boolean? {
        if (recipient.isNullOrEmpty()) {
            return false
        }

        if (selectedAsset == null || recipientAssetId == null) {
            return null
        }

        if (type == Type.GATEWAY && selectedAsset!!.assetId.equals(recipientAssetId)) {
            return true
        }

        if (type == Type.WAVES && isWavesAddress(recipient)) {
            return true
        }

        if (type == Type.ALIAS) {
            return true
        }

        return false
    }

    companion object {
        const val LANG: String = "ru_RU"
        const val MONERO_PAYMENT_ID_LENGTH = 64
        private val feeAsset: AssetBalance = AssetBalance(
                quantity = 100000000L * 100000000L,
                issueTransaction = IssueTransaction(
                        name = Constants.CUSTOM_FEE_ASSET_NAME, quantity = 0, decimals = 8))

        fun getAssetId(recipient: String?): String? {
            return when {
                recipient!!.matches("^[13][a-km-zA-HJ-NP-Z1-9]{25,34}$".toRegex()) ->
                    Constants.BITCOIN_ASSET_ID
                recipient.matches("^0x[0-9a-f]{40}$".toRegex()) ->
                    Constants.ETHEREUM_ASSET_ID
                recipient.matches("^[LM3][a-km-zA-HJ-NP-Z1-9]{26,33}$".toRegex()) ->
                    Constants.LIGHTCOIN_ASSET_ID
                recipient.matches("^t1[a-zA-Z0-9]{33}$".toRegex()) ->
                    Constants.ZEC_ASSET_ID
                recipient.matches("^([13][a-km-zA-HJ-NP-Z1-9]{25,34}|[qp][a-zA-Z0-9]{41})$".toRegex()) ->
                    Constants.BITCOINCASH_ASSET_ID
                recipient.matches("^X[a-km-zA-HJ-NP-Z1-9]{25,34}$".toRegex()) ->
                    Constants.DASH_ASSET_ID
                recipient.matches("^4([0-9]|[A-B])(.){93}".toRegex()) ->
                    Constants.MONERO_ASSET_ID
                else -> null
            }
        }

        fun isWavesAddress(address: String?): Boolean {
            if (address == null || !address.isValidAddress()) {
                return false
            }

            val addressBytes = Base58.decode(address)

            if (addressBytes[0] != 1.toByte()
                    || addressBytes[1] != Constants.ADDRESS_SCHEME.toByte()) {
                return false
            }

            val key = addressBytes.slice(IntRange(0, 21))
            val check = addressBytes.slice(IntRange(22, 25))
            val keyHash = Hash.secureHash(key.toByteArray())
                    .toList()
                    .slice(IntRange(0, 3))

            for (i in 0..3) {
                if (check[i] != keyHash[i]) {
                    return false
                }
            }
            return true
        }
    }

    enum class Type {
        ALIAS,
        WAVES,
        GATEWAY,
        UNKNOWN
    }
}
