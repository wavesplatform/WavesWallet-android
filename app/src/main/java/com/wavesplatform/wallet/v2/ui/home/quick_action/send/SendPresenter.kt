package com.wavesplatform.wallet.v2.ui.home.quick_action.send

import android.text.TextUtils
import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryAsSingle
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
    var recipient: String? = ""
    var amount: String? = ""
    var useAlias: Boolean = false

    fun sendClicked() {
        val res = validateTransfer(getTxRequest())
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
                                    useAlias = true
                                    runOnUiThread {
                                        viewState.setRecipientValid(true)
                                    }
                                }, {
                                    useAlias = false
                                    runOnUiThread {
                                        viewState.setRecipientValid(false)
                                    }
                                }))
            }
        } else {
            useAlias = false
            viewState.setRecipientValid(false)
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

    private fun validateTransfer(tx: TransactionsBroadcastRequest): Int {
        if (selectedAsset == null || TextUtils.isEmpty(recipient)) {
            R.string.send_transaction_error_check_fields
        } else if (isRecipientValid() != true) {
            return R.string.invalid_address
        } else if (TransactionsBroadcastRequest.getAttachmentSize(tx.attachment)
                > TransferTransactionRequest.MaxAttachmentSize) {
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
                                runOnUiThread {
                                    viewState.showXRateError()
                                }
                            }))
        }
    }

    fun isRecipientValid(): Boolean? {
        if (selectedAsset == null) {
            return null
        }

        return when {
            AssetBalance.isGateway(selectedAsset!!.assetId!!) -> {
                isValid(queryFirst<AssetInfo> {
                    equalTo("id", selectedAsset!!.assetId)
                }!!.ticker, recipient)
            }
            useAlias -> true
            else -> {
                isWavesAddress(recipient)
            }
        }
    }

    companion object {
        const val LANG: String = "ru_RU"
        private val feeAsset: AssetBalance = AssetBalance(
                quantity = 100000000L * 100000000L,
                issueTransaction = IssueTransaction(
                        name = Constants.CUSTOM_FEE_ASSET_NAME, quantity = 0, decimals = 8))

        private fun isValid(ticker: String?, address: String?): Boolean? {
            if (ticker == null) {
                return null
            } else if (ticker == "WAVES" || ticker == "") {
                return address!!.matches("^[13][a-km-zA-HJ-NP-Z1-9]{25,34}$".toRegex())
            } else if (ticker == "ETH") {
                return address!!.matches("^0x[0-9a-f]{40}$".toRegex())
            } else if (ticker == "LTC") {
                return address!!.matches("^[LM3][a-km-zA-HJ-NP-Z1-9]{26,33}$".toRegex())
            } else if (ticker == "ZEC") {
                return address!!.matches("^t[0-9a-z]{34}$".toRegex())
            } else if (ticker == "BCH") {
                return address!!.matches("^([13][a-km-zA-HJ-NP-Z1-9]{25,34}|[qp][a-zA-Z0-9]{41})$".toRegex())
            } else if (ticker == "DASH") {
                return address!!.matches("^X[a-km-zA-HJ-NP-Z1-9]{25,34}$".toRegex())
            } else if (ticker == "XMR") {
                return address!!.matches("([a-km-zA-HJ-NP-Z1-9]{95}|[a-km-zA-HJ-NP-Z1-9]{106})$".toRegex())
            }
            return null
        }

        fun isWavesAddress(address: String?): Boolean {
            if (address == null) {
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
}
