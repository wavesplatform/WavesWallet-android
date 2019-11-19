/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.quick_action.send

import android.text.TextUtils
import moxy.InjectViewState
import com.vicpin.krealmextensions.queryFirst
import com.vicpin.krealmextensions.save
import com.wavesplatform.sdk.model.request.node.BaseTransaction
import com.wavesplatform.sdk.model.request.node.TransferTransaction
import com.wavesplatform.sdk.model.response.node.AssetBalanceResponse
import com.wavesplatform.sdk.model.response.node.AssetsDetailsResponse
import com.wavesplatform.sdk.model.response.node.IssueTransactionResponse
import com.wavesplatform.sdk.model.response.node.ScriptInfoResponse
import com.wavesplatform.sdk.utils.*
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.manager.gateway.provider.GatewayProvider
import com.wavesplatform.wallet.v2.data.model.db.AssetBalanceDb
import com.wavesplatform.wallet.v2.data.model.local.gateway.GatewayMetadataArgs
import com.wavesplatform.wallet.v2.data.model.remote.response.gateway.GatewayMetadata
import com.wavesplatform.wallet.v2.data.model.service.configs.GlobalTransactionCommissionResponse
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.*
import io.reactivex.Observable
import io.reactivex.functions.Function3
import java.math.BigDecimal
import javax.inject.Inject

@InjectViewState
class SendPresenter @Inject constructor() : BasePresenter<SendView>() {

    @Inject
    lateinit var gatewayProvider: GatewayProvider
    var gatewayMetadata: GatewayMetadata = GatewayMetadata()

    var type: Type = Type.UNKNOWN
    var selectedAsset: AssetBalanceResponse? = null

    var recipient: String? = ""
    var amount: BigDecimal = BigDecimal.ZERO
    var attachment: String? = ""
    var moneroPaymentId: String? = null
    var recipientAssetId: String? = null
    var fee = 0L
    var feeWaves = 0L
    var feeAsset: AssetBalanceResponse? = null

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
                    dataServiceManager.loadAlias(alias)
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

    private fun getTxRequest(): TransferTransaction {
        val transaction = TransferTransaction(
                assetId = selectedAsset?.assetId ?: "",
                recipient = recipient ?: "",
                amount = MoneyUtil.getUnscaledValue(amount.toPlainString(), selectedAsset),
                attachment = SignUtil.textToBase58(""),
                feeAssetId = feeAsset?.assetId ?: "")
        transaction.fee = fee
        return transaction
    }

    private fun validateTransfer(): Int {
        if (selectedAsset == null) {
            return R.string.send_transaction_error_check_asset
        } else if (isRecipientValid() != true) {
            return R.string.invalid_address
        } else {
            val tx = getTxRequest()
            if (TransferTransaction.getAttachmentSize(tx.attachment)
                    > TransferTransaction.MAX_ATTACHMENT_SIZE) {
                return R.string.attachment_too_long
            } else if (tx.amount <= 0 || tx.amount > java.lang.Long.MAX_VALUE - tx.fee) {
                return R.string.invalid_amount
            } else if (tx.fee <= 0 || (feeAsset?.isWaves() != false && tx.fee < WavesConstants.WAVES_MIN_FEE)) {
                return R.string.insufficient_fee
            } else if (!isFundSufficient(tx)) {
                return R.string.insufficient_funds
            } else if (isGatewayAmountError()) {
                return R.string.insufficient_gateway_funds_error
            } else if (findByGatewayId("XMR")?.assetId == recipientAssetId &&
                    moneroPaymentId != null &&
                    (moneroPaymentId?.length != MONERO_PAYMENT_ID_LENGTH ||
                            moneroPaymentId?.contains(" ".toRegex()) == true)) {
                return R.string.invalid_monero_payment_id
            }
        }
        return 0
    }

    private fun isGatewayAmountError(): Boolean {
        if ((type == Type.ERGO || type == Type.WAVES_ENTERPRISE || type == Type.GATEWAY)
                && selectedAsset != null && gatewayMetadata.maxLimit.toFloat() > 0) {
            val totalAmount = amount + gatewayMetadata.fee
            val balance = BigDecimal.valueOf(selectedAsset!!.balance ?: 0,
                    selectedAsset!!.getDecimals())
            return !(balance >= totalAmount &&
                    totalAmount >= gatewayMetadata.minLimit &&
                    totalAmount <= gatewayMetadata.maxLimit)
        }
        return false
    }

    private fun isFundSufficient(tx: TransferTransaction): Boolean {
        return if (isSameSendingAndFeeAssets()) {
            tx.amount + tx.fee <= selectedAsset!!.getAvailableBalance()
        } else {
            val validFee = if (tx.feeAssetId.isWaves()) {
                tx.fee <= queryFirst<AssetBalanceDb> {
                    equalTo("assetId", "")
                }?.convertFromDb()?.balance ?: 0
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

    fun loadGatewayMetadata(assetId: String) {
        addSubscription(gatewayProvider
                .getGatewayDataManager(assetId)
                .loadGatewayMetadata(GatewayMetadataArgs(selectedAsset, recipient))
                .executeInBackground()
                .subscribe({ metadata ->
                    type = Type.GATEWAY
                    val gatewayTicket = Constants.coinomatCryptoCurrencies()[assetId]
                    gatewayMetadata = metadata
                    viewState.onLoadMetadataSuccess(metadata, gatewayTicket)
                }, {
                    type = Type.UNKNOWN
                    viewState.onLoadMetadataError()
                }))
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

        if (type == Type.WAVES_ENTERPRISE && recipient.isValidWavesEnterpriseAddress() && selectedAsset!!.assetId == recipientAssetId) {
            return true
        }

        if (type == Type.ERGO && recipient.isValidErgoAddress() && selectedAsset!!.assetId == recipientAssetId) {
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
                githubServiceManager.getGlobalCommission(),
                nodeServiceManager.scriptAddressInfo(),
                nodeServiceManager.assetDetails(assetId),
                Function3 { t1: GlobalTransactionCommissionResponse,
                            t2: ScriptInfoResponse,
                            t3: AssetsDetailsResponse ->
                    return@Function3 Triple(t1, t2, t3)
                })
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({ triple ->
                    val commission = triple.first
                    val scriptInfo = triple.second
                    val assetsDetails = triple.third
                    val params = GlobalTransactionCommissionResponse.ParamsResponse()
                    params.transactionType = BaseTransaction.TRANSFER
                    params.smartAccount = scriptInfo.extraFee != 0L
                    params.smartAsset = assetsDetails.scripted
                    fee = TransactionCommissionUtil.countCommission(commission, params)
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
                nodeServiceManager.assetDetails(assetId)
                        .compose(RxUtil.applyObservableDefaultSchedulers())
                        .subscribe({ assetsDetails ->
                            val assetBalance = AssetBalanceResponse(
                                    assetsDetails.assetId,
                                    quantity = assetsDetails.quantity,
                                    issueTransaction = IssueTransactionResponse(
                                            assetId = assetsDetails.assetId,
                                            id = assetsDetails.assetId,
                                            name = assetsDetails.name,
                                            decimals = assetsDetails.decimals,
                                            quantity = assetsDetails.quantity,
                                            description = assetsDetails.description,
                                            reissuable = assetsDetails.reissuable,
                                            timestamp = assetsDetails.issueTimestamp,
                                            sender = assetsDetails.issuer))
                            AssetBalanceDb(assetBalance).save()
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

    fun isAmountValid(amount: BigDecimal): Boolean {
        val tx = getTxRequest()
        tx.amount = MoneyUtil.getUnscaledValue(amount.toPlainString(), selectedAsset)
        return isFundSufficient(tx)
    }

    companion object {
        const val MONERO_PAYMENT_ID_LENGTH = 64

        fun getAssetId(recipient: String?, assetBalance: AssetBalanceResponse?): String? {
            val configAsset = EnvironmentManager.globalConfiguration.generalAssets
                    .firstOrNull { it.assetId == assetBalance?.assetId }

            return if (recipient?.matches("${configAsset?.addressRegEx}$".toRegex()) == true) {
                configAsset?.assetId
            } else {
                null
            }
        }
    }

    enum class Type {
        ALIAS,
        WAVES,
        WAVES_ENTERPRISE,
        ERGO,
        GATEWAY,
        UNKNOWN
    }
}
