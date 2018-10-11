package com.wavesplatform.wallet.v2.ui.home.quick_action.send.confirmation

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryAsSingle
import com.vicpin.krealmextensions.queryFirst
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.api.NodeManager
import com.wavesplatform.wallet.v1.data.rxjava.RxUtil
import com.wavesplatform.wallet.v1.request.TransferTransactionRequest
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.manager.CoinomatManager
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetInfo
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookUser
import com.wavesplatform.wallet.v2.ui.home.quick_action.send.SendPresenter
import io.reactivex.Single
import pyxis.uzuki.live.richutilskt.utils.runAsync
import pyxis.uzuki.live.richutilskt.utils.runOnUiThread
import javax.inject.Inject

@InjectViewState
class SendConfirmationPresenter @Inject constructor() : BasePresenter<SendConfirmationView>() {

    @Inject
    lateinit var coinomatManager: CoinomatManager
    private var nodeManager = NodeManager.createInstance(
            App.getAccessManager().getWallet()!!.publicKeyStr)

    var address: String? = ""
    var amount: String? = ""
    var attachment: String? = ""
    var selectedAsset: AssetBalance? = null


    fun confirmSend() {
        val singed = signTransaction()
        if (singed != null) {
            submitPayment(singed)
        } else {
            viewState.requestPassCode()
        }
    }

    private fun signTransaction(): TransferTransactionRequest? {
        val pk = App.getAccessManager().getWallet()!!.privateKey
        return if (pk != null) {
            val signed = getTxRequest()
            signed.sign(pk)
            signed
        } else {
            null
        }
    }

    private fun submitPayment(signed: TransferTransactionRequest) {
        if (AssetBalance.isGateway(signed.assetId)) {
            createGateAndPayment()
        } else {
            nodeManager.broadcastTransfer(signed)
                    .compose(RxUtil.applySchedulersToObservable()).subscribe({ tx ->
                        viewState.onShowTransactionSuccess(signed)
                    }, { err ->
                        viewState.onShowError(R.string.transaction_failed)
                    })
        }
    }

    private fun getTxRequest(): TransferTransactionRequest {
        return TransferTransactionRequest(
                selectedAsset!!.assetId,
                App.getAccessManager().getWallet()!!.publicKeyStr,
                address,
                MoneyUtil.getUnscaledValue(amount, selectedAsset),
                System.currentTimeMillis(),
                MoneyUtil.getUnscaledWaves(SendPresenter.CUSTOM_FEE),
                attachment)
    }

    fun getAddressName(address: String) {
        val addressBookUser = queryFirst<AddressBookUser> {
            equalTo("address", address)
        }
        if (addressBookUser == null) {
            viewState.hideAddressBookUser()
        } else {
            viewState.showAddressBookUser(addressBookUser.name)
        }
    }

    private fun createGateAndPayment() {
        runAsync {
            val assetId = selectedAsset!!.assetId
            val findAsset: Single<List<AssetInfo>> = queryAsSingle {
                equalTo("id", assetId)
            }
            addSubscription(
                    findAsset.toObservable()
                            .flatMap {
                                val currencyTo = it[0].ticker
                                val currencyFrom = "W$currencyTo"
                                coinomatManager.createTunnel(
                                        currencyFrom,
                                        currencyTo,
                                        address)
                            }
                            .flatMap { createTunnel ->
                                coinomatManager.getTunnel(
                                        createTunnel.tunnelId,
                                        createTunnel.k1,
                                        createTunnel.k2,
                                        SendPresenter.LANG)
                            }
                            .flatMap {
                                address = it.tunnel!!.walletTo
                                val signedTransaction = signTransaction()
                                if (signedTransaction == null) {
                                    null
                                } else {
                                    nodeManager.broadcastTransfer(signedTransaction)
                                }
                            }
                            .subscribe(
                                    { request ->
                                        runOnUiThread {
                                            viewState.onShowTransactionSuccess(request)
                                        }
                                    },
                                    {
                                        runOnUiThread {
                                            viewState.onShowError(R.string.receive_error_network)
                                        }
                                    }))
        }
    }
}
