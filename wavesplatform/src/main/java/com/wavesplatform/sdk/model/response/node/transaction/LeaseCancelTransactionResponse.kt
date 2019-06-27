package com.wavesplatform.sdk.model.response.node.transaction

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.model.request.node.BaseTransaction
import kotlinx.android.parcel.Parcelize

/**
 * See [com.wavesplatform.sdk.model.request.node.LeaseCancelTransaction]
 */
@Parcelize
class LeaseCancelTransactionResponse(@SerializedName("leaseId")
                                       var leaseId: String = "",
                                     @SerializedName("lease")
                                       var lease: Unit? = null)
    : BaseTransactionResponse(type = BaseTransaction.CANCEL_LEASING), Parcelable