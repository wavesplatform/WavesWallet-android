/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.quick_action.send

import android.text.TextUtils
import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryFirst
import com.vicpin.krealmextensions.save
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.request.TransferTransactionRequest
import com.wavesplatform.wallet.v1.ui.auth.EnvironmentManager
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.manager.gateway.manager.CoinomatDataManager
import com.wavesplatform.wallet.v2.data.manager.gateway.manager.GatewayDataManager
import com.wavesplatform.wallet.v2.data.manager.gateway.provider.GatewayProvider
import com.wavesplatform.wallet.v2.data.model.remote.request.TransactionsBroadcastRequest
import com.wavesplatform.wallet.v2.data.model.remote.response.*
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.*
import com.wavesplatform.wallet.v2.util.TransactionUtil.Companion.countCommission
import io.reactivex.Observable
import io.reactivex.functions.Function3
import pyxis.uzuki.live.richutilskt.utils.isEmpty
import pyxis.uzuki.live.richutilskt.utils.runAsync
import pyxis.uzuki.live.richutilskt.utils.runOnUiThread
import java.math.BigDecimal
import javax.inject.Inject

@InjectViewState
class SendPresenter @Inject constructor() : BasePresenter<SendView>() {

    @Inject
    lateinit var coinomatManager: CoinomatDataManager
    @Inject
    lateinit var gatewayProvider: GatewayProvider
    @Inject
    lateinit var gatewayDataManager: GatewayDataManager

    var selectedAsset: AssetBalance? = null
    var recipient: String? = ""
    var amount: BigDecimal = BigDecimal.ZERO
    var attachment: String? = ""
    var moneroPaymentId: String? = null
    var gatewayProcessId: String? = null
    var gatewayRecipientAddress: String? = null
    var recipientAssetId: String? = null
    var type: Type? = null
    var gatewayCommission: BigDecimal = BigDecimal.ZERO
    var gatewayMin: BigDecimal = BigDecimal.ZERO
    var gatewayMax: BigDecimal = BigDecimal.ZERO
    var fee = 0L
    var feeWaves = 0L
    var feeAsset: AssetBalance? = null

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
                selectedAsset?.assetId ?: "",
                App.getAccessManager().getWallet()!!.publicKeyStr,
                recipient ?: "",
                MoneyUtil.getUnscaledValue(amount.toPlainString(), selectedAsset),
                EnvironmentManager.getTime(),
                fee,
                "",
                feeAsset?.assetId ?: "")
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
            } else if (tx.fee <= 0 || (feeAsset?.isWaves() != false && tx.fee < Constants.WAVES_MIN_FEE)) {
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
        if ((type == Type.VOSTOK || type == Type.GATEWAY) && selectedAsset != null && gatewayMax.toFloat() > 0) {
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
            return feeAsset?.assetId == selectedAsset!!.assetId
        }
        return false
    }

    fun loadXRate(assetId: String) {
        val currencyTo = Constants.coinomatCryptoCurrencies()[assetId]
        if (currencyTo.isNullOrEmpty()) {
            type = Type.UNKNOWN
            runOnUiThread {
                viewState.showXRateError()
            }
            return
        }
        gatewayProvider
                .getGatewayDataManager(assetId)
                .loadGatewayMetadata()
        if (type == Type.VOSTOK) {
            addSubscription(gatewayDataManager.prepareSendTransaction(recipient!!, assetId)
                    .compose(RxUtil.applyObservableDefaultSchedulers())
                    .subscribe({ response ->
                        gatewayProcessId = response.processId
                        gatewayRecipientAddress = response.recipientAddress

                        gatewayCommission = MoneyUtil.getScaledText(response.fee, selectedAsset).clearBalance().toBigDecimal()
                        gatewayMin = MoneyUtil.getScaledText(response.minAmount, selectedAsset).clearBalance().toBigDecimal()
                        gatewayMax = MoneyUtil.getScaledText(response.maxAmount, selectedAsset).clearBalance().toBigDecimal()

                        viewState.showXRate(currencyTo)
                    }, {
                        type = Type.UNKNOWN
                        viewState.showXRateError()
                    }))
            return
        }

        val currencyFrom = "W$currencyTo"
        runAsync {
            addSubscription(coinomatManager.getXRate(currencyFrom, currencyTo, LANG)
                    .subscribe({ xRate ->
                        type = Type.GATEWAY
                        gatewayCommission = BigDecimal(xRate.feeOut ?: "0")
                        gatewayMin = BigDecimal(xRate.inMin ?: "0")
                        gatewayMax = BigDecimal(xRate.inMax ?: "0")
                        runOnUiThread {
                            if (xRate == null) {
                                viewState.showXRateError()
                            } else {
                                viewState.showXRate(currencyTo)
                            }
                        }
                    }, {
                        type = Type.UNKNOWN
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

        if (type == Type.GATEWAY && selectedAsset!!.assetId == recipientAssetId) {
            return true
        }

        if (type == Type.WAVES && recipient.isValidWavesAddress()) {
            return true
        }

        if (type == Type.VOSTOK && recipient.isValidVostokAddress() && selectedAsset!!.assetId == recipientAssetId) {
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
                nodeDataManager.scriptAddressInfo(),
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

    fun loadAssetForLink(assetId: String, url: String) {
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
                            assetBalance.save()
                            if (!TextUtils.isEmpty(url)) {
                                viewState.setDataFromUrl(url)
                            } else {
                                viewState.showLoadAssetSuccess(assetBalance)
                            }
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
            val configAsset = EnvironmentManager.globalConfiguration.generalAssets
                    .firstOrNull { it.assetId == assetBalance?.assetId }

            return if (recipient?.matches("${configAsset?.addressRegEx}$".toRegex()) == true
                    || configAsset?.addressRegEx?.isEmpty() == true) {
                configAsset?.assetId
            } else {
                null
            }
        }
    }

    enum class Type {
        ALIAS,
        WAVES,
        VOSTOK,
        GATEWAY,
        UNKNOWN
    }
}
