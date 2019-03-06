package com.wavesplatform.wallet.v2.ui.home.quick_action.send

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryFirst
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.crypto.Base58
import com.wavesplatform.wallet.v1.crypto.Hash
import com.wavesplatform.wallet.v1.request.TransferTransactionRequest
import com.wavesplatform.wallet.v1.ui.auth.EnvironmentManager
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.manager.CoinomatManager
import com.wavesplatform.wallet.v2.data.model.remote.request.TransactionsBroadcastRequest
import com.wavesplatform.wallet.v2.data.model.remote.response.*
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import com.wavesplatform.wallet.v2.util.TransactionUtil.Companion.countCommission
import com.wavesplatform.wallet.v2.util.isSpamConsidered
import com.wavesplatform.wallet.v2.util.isValidAddress
import com.wavesplatform.wallet.v2.util.isWaves
import io.reactivex.Observable
import io.reactivex.functions.Function3
import pyxis.uzuki.live.richutilskt.utils.runAsync
import pyxis.uzuki.live.richutilskt.utils.runOnUiThread
import java.math.BigDecimal
import javax.inject.Inject

@InjectViewState
class SendPresenter @Inject constructor() : BasePresenter<SendView>() {

    @Inject
    lateinit var coinomatManager: CoinomatManager

    var selectedAsset: AssetBalance? = null
    var recipient: String? = ""
    var amount: BigDecimal = BigDecimal.ZERO
    var attachment: String? = ""
    var moneroPaymentId: String? = null
    var recipientAssetId: String? = null
    var type: Type? = null
    var gatewayCommission: BigDecimal = BigDecimal.ZERO
    var gatewayMin: BigDecimal = BigDecimal.ZERO
    var gatewayMax: BigDecimal = BigDecimal.ZERO
    var fee = 0L
    var feeWaves = 0L
    var feeAsset: AssetBalance = Constants.find(Constants.WAVES_ASSET_ID_EMPTY)!!

    fun sendClicked() {
        val res = validateTransfer()
        if (res == 0) {
            viewState.onShowPaymentDetails()
        } else {
            viewState.onShowError(res)
        }
    }

    fun checkAlias(alias: String) {
        if (alias.length in 4..30) {
            addSubscription(
                    apiDataManager.loadAlias(alias)
                            .compose(RxUtil.applyObservableDefaultSchedulers())
                            .subscribe({ _ ->
                                type = Type.ALIAS
                                viewState.setRecipientValid(true)
                            }, {
                                type = Type.UNKNOWN
                                viewState.setRecipientValid(false)
                            }))
        } else {
            type = Type.UNKNOWN
            viewState.setRecipientValid(null)
        }
    }

    private fun getTxRequest(): TransactionsBroadcastRequest {
        return TransactionsBroadcastRequest(
                selectedAsset!!.assetId,
                App.getAccessManager().getWallet()!!.publicKeyStr,
                recipient ?: "",
                MoneyUtil.getUnscaledValue(amount.toPlainString(), selectedAsset),
                System.currentTimeMillis(),
                fee,
                "",
                feeAsset.assetId)
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
            } else if (tx.amount <= 0 || tx.amount > java.lang.Long.MAX_VALUE - tx.fee) {
                    return R.string.invalid_amount
                } else if (tx.fee <= 0 || (feeAsset.isWaves() && tx.fee < Constants.WAVES_MIN_FEE)) {
                    return R.string.insufficient_fee
                } else if (!isFundSufficient(tx)) {
                    return R.string.insufficient_funds
                } else if (isGatewayAmountError()) {
                    return R.string.insufficient_gateway_funds_error
                } else if (Constants.findByGatewayId("XMR")!!.assetId == recipientAssetId &&
                        moneroPaymentId != null &&
                        (moneroPaymentId!!.length != MONERO_PAYMENT_ID_LENGTH ||
                                moneroPaymentId!!.contains(" ".toRegex()))) {
                    return R.string.invalid_monero_payment_id
                }
        }
        return 0
    }

    private fun isGatewayAmountError(): Boolean {
        if (type == Type.GATEWAY && selectedAsset != null && gatewayMax.toFloat() > 0) {
            val totalAmount = amount + gatewayCommission
            val balance = BigDecimal.valueOf(selectedAsset!!.balance ?: 0,
                    selectedAsset!!.getDecimals())
            return !(balance >= totalAmount &&
                    totalAmount >= gatewayMin &&
                    totalAmount <= gatewayMax)
        }
        return false
    }

    private fun isFundSufficient(tx: TransactionsBroadcastRequest): Boolean {
        return if (isSameSendingAndFeeAssets()) {
            tx.amount + tx.fee <= selectedAsset!!.balance!!
        } else {
            val validFee = if (tx.feeAssetId?.isWaves() == true) {
                tx.fee <= queryFirst<AssetBalance> { equalTo("assetId", "") }?.balance ?: 0
            } else {
                true
            }

            tx.amount <= selectedAsset!!.balance!! && validFee
        }
    }

    private fun isSameSendingAndFeeAssets(): Boolean {
        if (selectedAsset != null) {
            return feeAsset.assetId == selectedAsset!!.assetId
        }
        return false
    }

    fun loadXRate(assetId: String) {
        val currencyTo = Constants.coinomatCryptoCurrencies()[assetId]
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
                        gatewayCommission = BigDecimal(xRate.feeOut ?: "0")
                        gatewayMin = BigDecimal(xRate.inMin ?: "0")
                        gatewayMax = BigDecimal(xRate.inMax ?: "0")
                        runOnUiThread {
                            if (xRate == null) {
                                viewState.showXRateError()
                            } else {
                                viewState.showXRate(xRate, currencyTo)
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

    fun loadCommission(assetId: String?) {
        viewState.showCommissionLoading()
        fee = 0L
        addSubscription(Observable.zip(
                githubDataManager.getGlobalCommission(),
                nodeDataManager.scriptAddressInfo(App.getAccessManager().getWallet()?.address!!),
                nodeDataManager.assetDetails(assetId),
                Function3 { t1: GlobalTransactionCommission,
                            t2: ScriptInfo,
                            t3: AssetsDetails ->
                    return@Function3 Triple(t1, t2, t3)
                })
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({ triple ->
                    val commission = triple.first
                    val scriptInfo = triple.second
                    val assetsDetails = triple.third
                    val params = GlobalTransactionCommission.Params()
                    params.transactionType = Transaction.TRANSFER
                    params.smartAccount = scriptInfo.extraFee != 0L
                    params.smartAsset = assetsDetails.scripted
                    fee = countCommission(commission, params)
                    feeWaves = fee
                    viewState.showCommissionSuccess(fee)
                }, {
                    it.printStackTrace()
                    fee = 0L
                    viewState.showCommissionError()
                }))
    }

    fun loadAsset(assetId: String) {
        addSubscription(
                nodeDataManager.assetDetails(assetId)
                        .compose(RxUtil.applyObservableDefaultSchedulers())
                        .subscribe({ assetsDetails ->
                            val assetBalance = AssetBalance(
                                    assetsDetails.assetId,
                                    quantity = assetsDetails.quantity,
                                    issueTransaction = IssueTransaction(
                                            assetId = assetsDetails.assetId,
                                            id = assetsDetails.assetId,
                                            name = assetsDetails.name,
                                            decimals = assetsDetails.decimals,
                                            quantity = assetsDetails.quantity,
                                            description = assetsDetails.description,
                                            reissuable = assetsDetails.reissuable,
                                            timestamp = assetsDetails.issueTimestamp,
                                            sender = assetsDetails.issuer))
                            viewState.showLoadAssetSuccess(assetBalance)
                            if (isSpamConsidered(assetId, prefsUtil)) {
                                viewState.onShowError(R.string.send_spam_error)
                            }
                        }, {
                            viewState.showLoadAssetError(R.string.common_server_error)
                        }))
    }

    companion object {
        const val LANG: String = "ru_RU"
        const val MONERO_PAYMENT_ID_LENGTH = 64

        fun getAssetId(recipient: String?, assetBalance: AssetBalance?): String? {
            for (asset in EnvironmentManager.globalConfiguration.generalAssetIds) {
                if (recipient!!.matches("${asset.addressRegEx}$".toRegex())) {
                    return if (assetBalance != null) {
                        asset.assetId
                    } else {
                        null
                    }
                }
            }
            return null
        }

        fun isWavesAddress(address: String?): Boolean {
            if (address == null || !address.isValidAddress()) {
                return false
            }

            val addressBytes = Base58.decode(address)

            if (addressBytes[0] != 1.toByte() ||
                    addressBytes[1] != EnvironmentManager.netCode) {
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
