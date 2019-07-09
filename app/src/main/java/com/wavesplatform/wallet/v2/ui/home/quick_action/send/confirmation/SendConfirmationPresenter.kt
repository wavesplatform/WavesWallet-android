/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.quick_action.send.confirmation

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryFirst
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.ui.auth.EnvironmentManager
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v1.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.manager.gateway.provider.GatewayProvider
import com.wavesplatform.wallet.v2.data.model.local.gateway.GatewayWithdrawArgs
import com.wavesplatform.wallet.v2.data.model.remote.request.TransactionsBroadcastRequest
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetInfo
import com.wavesplatform.wallet.v2.data.model.userdb.AddressBookUser
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.ui.home.quick_action.send.SendPresenter
import com.wavesplatform.wallet.v2.util.*
import java.math.BigDecimal
import javax.inject.Inject

@InjectViewState
class SendConfirmationPresenter @Inject constructor() : BasePresenter<SendConfirmationView>() {

    @Inject
    lateinit var gatewayProvider: GatewayProvider

    var recipient: String? = ""
    var amount: BigDecimal = BigDecimal.ZERO
    var attachment: String = ""
    var selectedAsset: AssetBalance? = null
    var assetInfo: AssetInfo? = null
    var moneroPaymentId: String? = null
    var type: SendPresenter.Type = SendPresenter.Type.UNKNOWN
    var gatewayCommission: BigDecimal = BigDecimal.ZERO
    var blockchainCommission = 0L
    var feeAsset: AssetBalance = Constants.find(Constants.WAVES_ASSET_ID_EMPTY)!!

    var success = false

    fun confirmWithdrawTransaction() {
        val transaction = getTxRequest()
        when (type) {
            SendPresenter.Type.GATEWAY -> makeWithdrawViaGateway(transaction)
            else -> {
                makeWithdrawViaWavesBlockchain(transaction)
            }
        }
    }

    private fun makeWithdrawViaWavesBlockchain(transaction: TransactionsBroadcastRequest) {
        addSubscription(nodeDataManager.transactionsBroadcast(transaction)
                .executeInBackground()
                .subscribe({ tx ->
                    tx.recipient = tx.recipient.parseAlias()
                    saveLastSentAddress(tx.recipient)
                    success = true
                    viewState.onShowTransactionSuccess(tx)
                }, {
                    if (it.errorBody()?.isSmartError() == true) {
                        viewState.failedSendCauseSmart()
                    } else {
                        viewState.onShowError(R.string.transaction_failed)
                    }
                }))
    }

    private fun makeWithdrawViaGateway(transaction: TransactionsBroadcastRequest) {
        addSubscription(gatewayProvider.getGatewayDataManager(transaction.assetId)
                .makeWithdraw(GatewayWithdrawArgs(transaction, selectedAsset, moneroPaymentId))
                .executeInBackground()
                .subscribe({ tx ->
                    success = true
                    viewState.onShowTransactionSuccess(tx)
                }, {
                    if (it.errorBody()?.isSmartError() == true) {
                        viewState.failedSendCauseSmart()
                    } else {
                        viewState.onShowError(R.string.transaction_failed)
                    }
                }))
    }

    fun getTicker(): String {
        return if (assetInfo == null) {
            ""
        } else if (assetInfo!!.ticker.equals(null) ||
                assetInfo!!.ticker.equals("")) {
            assetInfo!!.name
        } else {
            assetInfo!!.ticker ?: ""
        }
    }

    private fun getTxRequest(): TransactionsBroadcastRequest {
        if (recipient == null || recipient!!.length < 4) {
            recipient = ""
        } else if (recipient!!.length <= 30) {
            recipient = recipient!!.makeAsAlias()
        }

        val totalAmount =
                if (type == SendPresenter.Type.GATEWAY || type == SendPresenter.Type.VOSTOK) amount + gatewayCommission
                else amount

        return TransactionsBroadcastRequest(
                selectedAsset!!.assetId,
                App.getAccessManager().getWallet()!!.publicKeyStr,
                recipient!!,
                MoneyUtil.getUnscaledValue(totalAmount.toPlainString(), selectedAsset),
                EnvironmentManager.getTime(),
                blockchainCommission,
                attachment,
                feeAsset.assetId,
                App.getAccessManager().getWallet()?.address)
    }

    fun getAddressName(address: String) {
        val addressBookUser = queryFirst<AddressBookUser> { equalTo("address", address) }
        if (addressBookUser == null) {
            viewState.hideAddressBookUser()
        } else {
            viewState.showAddressBookUser(addressBookUser.name)
        }
    }

    private fun saveLastSentAddress(newAddress: String) {
        val addresses = prefsUtil.getGlobalValueList(PrefsUtil.KEY_LAST_SENT_ADDRESSES)
        var needAdd = true
        for (address in addresses) {
            if (newAddress == address) {
                needAdd = false
            }
        }
        if (needAdd) {
            val addressesList = addresses.toMutableList()
            if (addresses.size < 5) {
                addressesList.add(newAddress)
            } else {
                addressesList.removeAt(0)
                addressesList.add(newAddress)
            }
            prefsUtil.setGlobalValue(PrefsUtil.KEY_LAST_SENT_ADDRESSES, addressesList.toTypedArray())
        }
    }
}
