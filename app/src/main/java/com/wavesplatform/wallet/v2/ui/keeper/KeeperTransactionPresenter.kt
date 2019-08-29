package com.wavesplatform.wallet.v2.ui.keeper

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.sdk.WavesSdk
import com.wavesplatform.sdk.model.request.node.BaseTransaction
import com.wavesplatform.sdk.model.request.node.DataTransaction
import com.wavesplatform.sdk.model.request.node.InvokeScriptTransaction
import com.wavesplatform.sdk.model.request.node.TransferTransaction
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.executeInBackground
import javax.inject.Inject

@InjectViewState
class KeeperTransactionPresenter @Inject constructor() : BasePresenter<KeeperTransactionView>() {


    fun receiveAsset(assetId: String) {
        addSubscription(nodeServiceManager.assetDetails(assetId)
                .executeInBackground()
                .subscribe({ asset ->
                    viewState.onReceivedAsset(asset)
                }, {
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
