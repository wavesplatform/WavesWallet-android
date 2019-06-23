package com.wavesplatform.sdk.model.response.node.transaction

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.model.request.node.BaseTransaction
import kotlinx.android.parcel.Parcelize

@Parcelize
class DataTransactionResponse(@SerializedName("data")
                              var data: Array<Data>)
    : BaseTransactionResponse(type = BaseTransaction.DATA), Parcelable {

    @Parcelize
    class Data : Parcelable {
        @SerializedName("key")
        var key: String = ""
        @SerializedName("type")
        var type: String = ""
        @SerializedName("value")
        var value: ArgsType? = ArgsType.VALUE_STRING
    }
}