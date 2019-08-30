package com.wavesplatform.wallet.v2.ui.keeper

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.sdk.WavesSdk
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
    var feeWaves = 0L
    var assetsDetails:AssetsDetailsResponse? = null

    fun receiveAsset(assetId: String) {
        addSubscription(nodeServiceManager.assetDetails(assetId)
                .executeInBackground()
                .subscribe({ triple ->
                    // viewState.onReceivedAsset(assetsDetails)
                }, {
                    it.printStackTrace()
                    // viewState.onError(it)
                }))
    }

    fun receiveAsset(transaction: TransferTransaction, address: String) {
        //viewState.showCommissionLoading()
        fee = 0L
        addSubscription(Observable.zip(
                githubServiceManager.getGlobalCommission(),
                nodeServiceManager.scriptAddressInfo(address),
                nodeServiceManager.assetDetails(transaction.assetId),
                io.reactivex.functions.Function3 { t1: GlobalTransactionCommissionResponse,
                            t2: ScriptInfoResponse,
                            t3: AssetsDetailsResponse ->
                    return@Function3 Triple(t1, t2, t3)
                })
                .executeInBackground()
                .subscribe({ triple ->
                    val commission = triple.first
                    val scriptInfo = triple.second
                    val assetsDetails = triple.third
                    val params = GlobalTransactionCommissionResponse.ParamsResponse()
                    params.transactionType = transaction.type
                    params.smartAccount = scriptInfo.extraFee != 0L
                    params.smartAsset = assetsDetails.scripted
                    fee = TransactionCommissionUtil.countCommission(commission, params)
                    feeWaves = fee
                    this.assetsDetails = assetsDetails
                    viewState.onReceivedAsset(assetsDetails)
                }, {
                    it.printStackTrace()
                    fee = 0L
                    viewState.onError(it)
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
