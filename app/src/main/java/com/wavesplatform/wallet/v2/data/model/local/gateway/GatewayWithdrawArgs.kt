/*
 * Created by Eduard Zaydel on 26/6/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.model.local.gateway

import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.model.request.node.TransferTransaction
import com.wavesplatform.sdk.model.response.node.AssetBalanceResponse

data class GatewayWithdrawArgs(var transaction: TransferTransaction,
                               var asset: AssetBalanceResponse?,
                               var coinomatMoneroPaymentId: String? = null) {

    class Transaction(
            transaction: TransferTransaction,
            @SerializedName("sender") var sender: String) : TransferTransaction(
            transaction.assetId,
            transaction.recipient,
            transaction.amount,
            transaction.fee,
            transaction.attachment,
            transaction.feeAssetId) {

        init {
            this.senderPublicKey = transaction.senderPublicKey
            this.timestamp = transaction.timestamp
            this.version = transaction.version
            this.proofs.addAll(transaction.proofs)
            this.signature = transaction.signature
            this.chainId = transaction.chainId
        }
    }
}