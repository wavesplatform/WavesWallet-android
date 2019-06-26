package com.wavesplatform.sdk.model.response.node.transaction

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.model.request.node.BaseTransaction
import kotlinx.android.parcel.Parcelize

@Parcelize
internal class MassTransferTransactionResponse(@SerializedName("assetId")
                                      var assetId: String?,
                                      @SerializedName("attachment")
                                      var attachment: String,
                                      @SerializedName("transferCount")
                                      var transferCount: Int,
                                      @SerializedName("totalAmount")
                                      var totalAmount: Long,
                                      @SerializedName("transfers")
                                      var transfers: Array<Transfer>)
    : BaseTransactionResponse(type = BaseTransaction.MASS_TRANSFER), Parcelable {

    @Parcelize
    class Transfer(
            /**
             * Address or alias of Waves blockchain
             */
            @SerializedName("recipient") var recipient: String = "",
            /**
             * Amount of Waves in satoshi
             */
            @SerializedName("amount") var amount: Long = 0L) : Parcelable
}