package com.wavesplatform.sdk.model.response.node.transaction

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.model.request.node.BaseTransaction
import com.wavesplatform.sdk.model.request.node.DataTransaction
import kotlinx.android.parcel.Parcelize

/**
 * See [com.wavesplatform.sdk.model.request.node.DataTransaction]
 */
@Parcelize
class DataTransactionResponse
    : BaseTransactionResponse(type = BaseTransaction.DATA), Parcelable {

    @SerializedName("data")
    var data: List<DataTransaction.Data>? = null
}