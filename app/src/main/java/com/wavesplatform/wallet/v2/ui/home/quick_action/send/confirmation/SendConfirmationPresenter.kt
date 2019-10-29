/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.quick_action.send.confirmation

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryFirst
import com.wavesplatform.sdk.model.request.node.TransferTransaction
import com.wavesplatform.sdk.model.response.data.AssetInfoResponse
import com.wavesplatform.sdk.model.response.node.AssetBalanceResponse
import com.wavesplatform.sdk.utils.*
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.manager.gateway.provider.GatewayProvider
import com.wavesplatform.wallet.v2.data.model.db.userdb.AddressBookUserDb
import com.wavesplatform.wallet.v2.data.model.local.gateway.GatewayWithdrawArgs
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.ui.home.quick_action.send.SendPresenter
import com.wavesplatform.wallet.v2.util.PrefsUtil
import com.wavesplatform.wallet.v2.util.errorBody
import com.wavesplatform.wallet.v2.util.executeInBackground
import com.wavesplatform.wallet.v2.util.find
import java.math.BigDecimal
import javax.inject.Inject

@InjectViewState
class SendConfirmationPresenter @Inject constructor() : BasePresenter<SendConfirmationView>() {

    @Inject
    lateinit var gatewayProvider: GatewayProvider

    var recipient: String? = ""
    var amount: BigDecimal = BigDecimal.ZERO
    var attachment: String = ""
    var selectedAsset: AssetBalanceResponse? = null
    var assetInfo: AssetInfoResponse? = null
    var moneroPaymentId: String? = null
    var type: SendPresenter.Type = SendPresenter.Type.UNKNOWN
    var gatewayCommission: BigDecimal = BigDecimal.ZERO
    var blockchainCommission = 0L
    var feeAsset: AssetBalanceResponse? = find(WavesConstants.WAVES_ASSET_ID_EMPTY)

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

    private fun makeWithdrawViaWavesBlockchain(transaction: TransferTransaction) {
        addSubscription(nodeServiceManager.transactionsBroadcast(transaction)
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

    private fun makeWithdrawViaGateway(transaction: TransferTransaction) {
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

    private fun getTxRequest(): TransferTransaction {
        if (recipient == null || recipient!!.length < 4) {
            recipient = ""
        } else if (recipient!!.length <= 30) {
            recipient = recipient!!.makeAsAlias()
        }

        val totalAmount =
                if (type == SendPresenter.Type.GATEWAY
                        || type == SendPresenter.Type.WAVES_ENTERPRISE
                        || type == SendPresenter.Type.ERGO) amount + gatewayCommission
                else amount

        val transaction = TransferTransaction(
                assetId = selectedAsset!!.assetId,
                recipient = recipient!!,
                amount = MoneyUtil.getUnscaledValue(totalAmount.toPlainString(), selectedAsset),
                attachment = SignUtil.textToBase58(attachment),
                feeAssetId = feeAsset?.assetId ?: "")
        transaction.fee = blockchainCommission
        return transaction
    }

    fun getAddressName(address: String) {
        val addressBookUser = queryFirst<AddressBookUserDb> { equalTo("address", address) }
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
