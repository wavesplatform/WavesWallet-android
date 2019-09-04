package com.wavesplatform.wallet.v2.ui.keeper

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.sdk.WavesSdk
import com.wavesplatform.sdk.keeper.interfaces.KeeperTransaction
import com.wavesplatform.sdk.model.request.node.DataTransaction
import com.wavesplatform.sdk.model.request.node.InvokeScriptTransaction
import com.wavesplatform.sdk.model.request.node.TransferTransaction
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.executeInBackground
import javax.inject.Inject

@InjectViewState
class KeeperSendTransactionPresenter @Inject constructor() : BasePresenter<KeeperSendTransactionView>() {

    var transaction: KeeperTransaction? = null

    fun sendTransaction(transaction: KeeperTransaction) {
        when (transaction) {
            is TransferTransaction -> {
                addSubscription(WavesSdk.service().getNode().transactionsBroadcast(transaction)
                        .executeInBackground()
                        .subscribe({ tx ->
                            viewState.onSuccessSend(tx)
                        }, {
                            viewState.onError(it)
                        }))

            }
            is DataTransaction -> {
                addSubscription(WavesSdk.service().getNode().transactionsBroadcast(transaction)
                        .executeInBackground()
                        .subscribe({ tx ->
                            viewState.onSuccessSend(tx)
                        }, {
                            viewState.onError(it)
                        }))
            }
            is InvokeScriptTransaction -> {
                addSubscription(WavesSdk.service().getNode().transactionsBroadcast(transaction)
                        .executeInBackground()
                        .subscribe({ tx ->
                            viewState.onSuccessSend(tx)
                        }, {
                            viewState.onError(it)
                        }))
            }
            else -> {
                viewState.onError(Throwable(App.getAppContext().getString(R.string.common_server_error)))
            }
        }
    }

    fun receiveAssetDetails(assetId: String) {
        addSubscription(nodeServiceManager.assetDetails(assetId)
                .executeInBackground()
                .subscribe({
                    viewState.onReceiveAssetDetails(it)
                }, {
                    it.printStackTrace()
                }))
    }
}
