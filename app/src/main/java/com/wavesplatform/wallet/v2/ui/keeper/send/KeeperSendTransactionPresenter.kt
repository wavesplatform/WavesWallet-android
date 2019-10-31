package com.wavesplatform.wallet.v2.ui.keeper.send

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.sdk.WavesSdk
import com.wavesplatform.sdk.keeper.interfaces.KeeperTransaction
import com.wavesplatform.sdk.keeper.interfaces.KeeperTransactionResponse
import com.wavesplatform.sdk.model.request.node.DataTransaction
import com.wavesplatform.sdk.model.request.node.InvokeScriptTransaction
import com.wavesplatform.sdk.model.request.node.TransferTransaction
import com.wavesplatform.sdk.utils.isSmartError
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.errorBody
import com.wavesplatform.wallet.v2.util.executeInBackground
import javax.inject.Inject

@InjectViewState
class KeeperSendTransactionPresenter @Inject constructor() : BasePresenter<KeeperSendTransactionView>() {

    var transaction: KeeperTransaction? = null
    var transactionResponse: KeeperTransactionResponse? = null

    fun sendTransaction(transaction: KeeperTransaction) {
        when (transaction) {
            is TransferTransaction -> {
                addSubscription(WavesSdk.service().getNode().transactionsBroadcast(transaction)
                        .executeInBackground()
                        .subscribe({ tx ->
                            viewState.onSuccessSend(tx)
                        }, {
                            onError(it)
                        }))

            }
            is DataTransaction -> {
                addSubscription(WavesSdk.service().getNode().transactionsBroadcast(transaction)
                        .executeInBackground()
                        .subscribe({ tx ->
                            viewState.onSuccessSend(tx)
                        }, {
                            onError(it)
                        }))
            }
            is InvokeScriptTransaction -> {
                addSubscription(WavesSdk.service().getNode().transactionsBroadcast(transaction)
                        .executeInBackground()
                        .subscribe({ tx ->
                            viewState.onSuccessSend(tx)
                        }, {
                            onError(it)
                        }))
            }
            else -> {
                viewState.onError(Throwable(App.appContext.getString(R.string.common_server_error)))
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

    private fun onError(error: Throwable) {
        val errorBody = error.errorBody()
        if (errorBody == null) {
            viewState.onError(error)
            return
        }

        viewState.onError(Throwable(errorBody.message))
    }
}
