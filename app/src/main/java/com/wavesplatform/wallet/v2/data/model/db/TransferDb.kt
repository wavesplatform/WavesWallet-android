package com.wavesplatform.wallet.v2.data.model.db

import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.model.response.Transfer
import com.wavesplatform.wallet.v2.util.notNull
import io.realm.RealmList
import io.realm.RealmModel
import io.realm.annotations.RealmClass

@RealmClass(name = "Transfer")
open class TransferDb(
        @SerializedName("recipient")
        var recipient: String = "",
        @SerializedName("recipientAddress")
        var recipientAddress: String? = "",
        @SerializedName("amount")
        var amount: Long = 0
) : RealmModel {

    constructor(transfer: Transfer) : this() {
        transfer.notNull {
            this.recipient = it.recipient
            this.recipientAddress = it.recipientAddress
            this.amount = it.amount
        }
    }

    fun convertFromDb(): Transfer {
        return Transfer(
                recipient = this.recipient,
                recipientAddress = this.recipientAddress,
                amount = this.amount)
    }

    companion object {

        fun convertToDb(transfers: List<Transfer>): RealmList<TransferDb> {
            val list = RealmList<TransferDb>()
            transfers.forEach {
                list.add(TransferDb(it))
            }
            return list
        }

        fun convertFromDb(transfers: RealmList<TransferDb>): MutableList<Transfer> {
            val list = mutableListOf<Transfer>()
            transfers.forEach {
                list.add(it.convertFromDb())
            }
            return list
        }
    }
}