package com.wavesplatform.wallet.v2.data.model.db

import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.model.response.Transfer
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
                //
        }

        fun convertFromDb(): Transfer {
                return Transfer()
        }

        companion object {

                fun convertToDb(transfers: List<Transfer>): List<TransferDb> {
                        return listOf()
                }

                fun convertFromDb(transfers: List<TransferDb>): List<Transfer> {
                        return listOf()
                }
        }

}