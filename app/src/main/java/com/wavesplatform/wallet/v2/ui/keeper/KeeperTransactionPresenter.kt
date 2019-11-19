package com.wavesplatform.wallet.v2.ui.keeper

import moxy.InjectViewState
import com.wavesplatform.sdk.keeper.interfaces.KeeperTransaction
import com.wavesplatform.sdk.keeper.model.DApp
import com.wavesplatform.sdk.keeper.model.KeeperActionType
import com.wavesplatform.sdk.model.request.node.BaseTransaction
import com.wavesplatform.sdk.model.request.node.DataTransaction
import com.wavesplatform.sdk.model.request.node.InvokeScriptTransaction
import com.wavesplatform.sdk.model.request.node.TransferTransaction
import com.wavesplatform.sdk.model.response.node.AssetsDetailsResponse
import com.wavesplatform.sdk.model.response.node.ScriptInfoResponse
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.service.configs.GlobalTransactionCommissionResponse
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.TransactionCommissionUtil
import com.wavesplatform.wallet.v2.util.executeInBackground
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import javax.inject.Inject

@InjectViewState
class KeeperTransactionPresenter @Inject constructor() : BasePresenter<KeeperTransactionView>() {

    var actionType: KeeperActionType = KeeperActionType.SIGN
    var dApp: DApp? = null
    var transaction: KeeperTransaction? = null
    var fee = 0L

    fun receiveTransactionData(transaction: KeeperTransaction, address: String) {
        fee = 0L
        this.transaction = transaction

        val assetDetails = hashMapOf<String, AssetsDetailsResponse>()
        var commission: GlobalTransactionCommissionResponse? = null
        var scriptInfo: ScriptInfoResponse? = null

        addSubscription(Observable.zip(
                githubServiceManager.getGlobalCommission(),
                nodeServiceManager.scriptAddressInfo(address),
                BiFunction { commissionResponse: GlobalTransactionCommissionResponse,
                             scriptInfoResponse: ScriptInfoResponse ->
                    return@BiFunction Pair(commissionResponse, scriptInfoResponse)
                })
                .flatMap {
                    commission = it.first
                    scriptInfo = it.second
                    createObservableAssetsDetailsResponse()
                }
                .executeInBackground()
                .subscribe({
                    assetDetails[it.assetId] = it
                }, {
                    it.printStackTrace()
                    viewState.onError(it)
                }, {
                    try {
                        val dAppAddress =
                                when (transaction) {
                                    is InvokeScriptTransaction -> transaction.dApp
                                    else -> ""
                                }
                        if (commission != null && scriptInfo != null) {
                            fee = countFee(transaction, commission!!, scriptInfo!!, assetDetails)
                        }

                        viewState.onReceiveTransactionData(this.transaction, dAppAddress, assetDetails)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        viewState.onError(e)
                    }
                }))
    }

    private fun countFee(transaction: KeeperTransaction,
                         commission: GlobalTransactionCommissionResponse,
                         scriptInfo: ScriptInfoResponse,
                         assetDetails: HashMap<String, AssetsDetailsResponse> = hashMapOf()): Long {
        val params = GlobalTransactionCommissionResponse.ParamsResponse()
        params.smartAccount = scriptInfo.extraFee != 0L
        return when (transaction) {
            is TransferTransaction -> {
                params.transactionType = transaction.type
                val assetDetail = assetDetails.values.toList()
                if (assetDetail.isNotEmpty()) {
                    params.smartAsset = assetDetail[0].scripted
                }
                TransactionCommissionUtil.countCommission(commission, params)
            }
            is DataTransaction -> {
                params.transactionType = transaction.type
                params.bytesCount = transaction.getDataSize()
                TransactionCommissionUtil.countCommission(commission, params)
            }
            else -> {
                (transaction as BaseTransaction).fee // Can't count commission for InvokeScript
            }
        }
    }

    private fun createObservableAssetsDetailsResponse(): Observable<AssetsDetailsResponse> {
        return when (transaction) {
            is TransferTransaction -> {
                nodeServiceManager.assetDetails((transaction as TransferTransaction).assetId)
            }
            is InvokeScriptTransaction -> {
                var temp: Observable<AssetsDetailsResponse>? = null
                val invokeTx = transaction as InvokeScriptTransaction
                invokeTx.payment.forEach { paymentItem ->
                    temp = if (temp == null) {
                        nodeServiceManager.assetDetails(paymentItem.assetId)
                    } else {
                        temp!!.mergeWith(nodeServiceManager.assetDetails(paymentItem.assetId))
                    }
                }
                return temp ?: Observable.empty()
            }
            else -> {
                Observable.empty()
            }
        }
    }
}
