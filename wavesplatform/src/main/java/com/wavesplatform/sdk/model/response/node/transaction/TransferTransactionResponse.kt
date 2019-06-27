package com.wavesplatform.sdk.model.response.node.transaction

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.model.request.node.BaseTransaction
import kotlinx.android.parcel.Parcelize

/**
 * See [com.wavesplatform.sdk.model.request.node.TransferTransaction]
 */
@Parcelize
class TransferTransactionResponse(@SerializedName("assetId")
                                  var assetId: String = "",
                                  @SerializedName("recipient")
                                  var recipient: String = "",
                                  @SerializedName("amount")
                                  var amount: Long = 0L,
                                  @SerializedName("attachment")
                                  var attachment: String = "",
                                  @SerializedName("feeAssetId")
                                  var feeAssetId: String = "")
    : BaseTransactionResponse(type = BaseTransaction.TRANSFER), Parcelable