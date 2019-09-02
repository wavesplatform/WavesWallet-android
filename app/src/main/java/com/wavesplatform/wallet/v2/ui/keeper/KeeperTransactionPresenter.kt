package com.wavesplatform.wallet.v2.ui.keeper

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.sdk.WavesSdk
import com.wavesplatform.sdk.keeper.interfaces.KeeperTransaction
import com.wavesplatform.sdk.model.request.node.BaseTransaction
import com.wavesplatform.sdk.model.request.node.DataTransaction
import com.wavesplatform.sdk.model.request.node.InvokeScriptTransaction
import com.wavesplatform.sdk.model.request.node.TransferTransaction
import com.wavesplatform.sdk.model.response.node.AssetsDetailsResponse
import com.wavesplatform.sdk.model.response.node.ScriptInfoResponse
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v2.data.model.service.cofigs.GlobalTransactionCommissionResponse
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.TransactionCommissionUtil
import com.wavesplatform.wallet.v2.util.executeInBackground
import io.reactivex.Observable
import javax.inject.Inject

@InjectViewState
class KeeperTransactionPresenter @Inject constructor() : BasePresenter<KeeperTransactionView>() {

    var fee = 0L

    fun receiveTransactionData(transaction: KeeperTransaction, address: String) {
        //viewState.showCommissionLoading()
        fee = 0L

        var bytesCount = 0
        var dAppAddress = ""
        var type = 0.toByte()

        var observable: Observable<AssetsDetailsResponse>? = null

        when (transaction) {
            is TransferTransaction -> {
                type = transaction.type
                observable = nodeServiceManager.assetDetails(transaction.assetId)
            }
            is DataTransaction -> {
                bytesCount = transaction.getDataSize()
                type = transaction.type
                observable = Observable.empty()
            }
            is InvokeScriptTransaction -> {
                type = transaction.type
                transaction.payment.forEach { paymentItem ->
                    observable = if (observable == null) {
                        nodeServiceManager.assetDetails(paymentItem.assetId)
                    } else {
                        observable!!.mergeWith(nodeServiceManager.assetDetails(paymentItem.assetId))
                    }
                }
                dAppAddress = transaction.dApp
            }
        }

        val assetDetails = hashMapOf<String, AssetsDetailsResponse>()
        var commission: GlobalTransactionCommissionResponse? = null
        var scriptInfo: ScriptInfoResponse? = null

        addSubscription(Observable.zip(
                githubServiceManager.getGlobalCommission(),
                nodeServiceManager.scriptAddressInfo(address),
                io.reactivex.functions.BiFunction { t1: GlobalTransactionCommissionResponse,
                                                    t2: ScriptInfoResponse ->
                    return@BiFunction Pair(t1, t2)
                })
                .flatMap {
                    commission = it.first
                    scriptInfo = it.second
                    observable!!
                }
                .executeInBackground()
                .subscribe({
                    assetDetails[it.assetId] = it
                }, {
                    it.printStackTrace()
                    viewState.onError(it)
                }, {
                    val params = GlobalTransactionCommissionResponse.ParamsResponse()
                    params.transactionType = type
                    params.smartAccount = scriptInfo!!.extraFee != 0L
                    when (type) {
                        BaseTransaction.TRANSFER -> {
                            val assetDetail = assetDetails.values.toList()
                            if (assetDetail.isNotEmpty()) {
                                params.smartAsset = assetDetail[0].scripted
                            }
                        }
                        BaseTransaction.DATA -> {
                            params.bytesCount = bytesCount
                        }
                        BaseTransaction.SCRIPT_INVOCATION -> {
                            // can't calculate fee
                        }
                    }
                    fee = TransactionCommissionUtil.countCommission(commission!!, params)
                    viewState.onReceiveTransactionData(
                            type, transaction, fee, dAppAddress, assetDetails)
                }))
    }


    fun sendTransaction(transaction: BaseTransaction) {

        when {
            transaction.type == BaseTransaction.TRANSFER -> {
                transaction as TransferTransaction
                transaction.sign(App.getAccessManager().getWallet()?.seedStr ?: "")
                addSubscription(WavesSdk.service().getNode().transactionsBroadcast(transaction)
                        .executeInBackground()
                        .subscribe({ tx ->
                            viewState.onSuccessSend(tx)
                        }, {
                            viewState.onError(it)
                        }))

            }
            transaction.type == BaseTransaction.DATA -> {
                transaction as DataTransaction
                transaction.sign(App.getAccessManager().getWallet()?.seedStr ?: "")
                addSubscription(WavesSdk.service().getNode().transactionsBroadcast(transaction)
                        .executeInBackground()
                        .subscribe({ tx ->
                            viewState.onSuccessSend(tx)
                        }, {
                            viewState.onError(it)
                        }))
            }
            transaction.type == BaseTransaction.SCRIPT_INVOCATION -> {
                transaction as InvokeScriptTransaction
                transaction.sign(App.getAccessManager().getWallet()?.seedStr ?: "")
                addSubscription(WavesSdk.service().getNode().transactionsBroadcast(transaction)
                        .executeInBackground()
                        .subscribe({ tx ->
                            viewState.onSuccessSend(tx)
                        }, {
                            viewState.onError(it)
                        }))
            }
            else -> {
                // do nothing
            }
        }


    }

    fun signTransaction(transaction: BaseTransaction) {

        when {
            transaction.type == BaseTransaction.TRANSFER -> {
                transaction as TransferTransaction
                transaction.sign(App.getAccessManager().getWallet()?.seedStr ?: "")
                viewState.onSuccessSign(transaction)
            }
            transaction.type == BaseTransaction.DATA -> {
                transaction as DataTransaction
                transaction.sign(App.getAccessManager().getWallet()?.seedStr ?: "")
                viewState.onSuccessSign(transaction)
            }
            transaction.type == BaseTransaction.SCRIPT_INVOCATION -> {
                transaction as InvokeScriptTransaction
                transaction.sign(App.getAccessManager().getWallet()?.seedStr ?: "")
                viewState.onSuccessSign(transaction)
            }
            else -> {
                // do nothing
            }
        }
    }
}
