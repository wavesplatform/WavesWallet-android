package com.wavesplatform.sdk.model.response.node.transaction

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.model.request.node.BaseTransaction
import kotlinx.android.parcel.Parcelize

/**
 * See [com.wavesplatform.sdk.model.request.node.IssueTransaction]
 */
@Parcelize
class IssueTransactionResponse(@SerializedName("assetId")
                               var assetId: String,
                               @SerializedName("name")
                               var name: String,
                               @SerializedName("quantity")
                               var quantity: Long,
                               @SerializedName("reissuable")
                               var reissuable: Boolean,
                               @SerializedName("decimals")
                               var decimals: Int,
                               @SerializedName("description")
                               var description: String,
                               @SerializedName("script")
                               var script: String?)
    : BaseTransactionResponse(type = BaseTransaction.ISSUE), Parcelable