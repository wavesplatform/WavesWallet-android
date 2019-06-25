package com.wavesplatform.sdk.model.response.node.transaction

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.model.request.node.BaseTransaction
import kotlinx.android.parcel.Parcelize

@Parcelize
class DataTransactionResponse
    : BaseTransactionResponse(type = BaseTransaction.DATA), Parcelable {

    @SerializedName("data")
    var data: List<com.wavesplatform.sdk.model.request.node.DataEntity>? = null
}