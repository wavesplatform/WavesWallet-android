package com.wavesplatform.sdk.model.response.node.transaction

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.model.request.node.BaseTransaction
import kotlinx.android.parcel.Parcelize

/**
 * See [com.wavesplatform.sdk.model.request.node.ReissueTransaction]
 */
@Parcelize
class ReissueTransactionResponse(@SerializedName("assetId")
                                 var assetId: String,
                                 @SerializedName("quantity")
                                 var quantity: Long,
                                 @SerializedName("reissuable")
                                 var reissuable: Boolean)
    : BaseTransactionResponse(type = BaseTransaction.REISSUE), Parcelable