package com.wavesplatform.sdk.model.response.node.transaction

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.model.request.node.BaseTransaction
import kotlinx.android.parcel.Parcelize

/**
 * See [com.wavesplatform.sdk.model.request.node.BurnTransaction]
 */
@Parcelize
class BurnTransactionResponse(@SerializedName("assetId")
                              val assetId: String = "",
                              @SerializedName("amount")
                              var amount: Long = 0)
    : BaseTransactionResponse(type = BaseTransaction.BURN), Parcelable