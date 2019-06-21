package com.wavesplatform.sdk.model.response.node.transaction

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.model.request.node.BaseTransaction
import kotlinx.android.parcel.Parcelize

@Parcelize
class InvokeScriptTransactionResponse(@SerializedName("feeAssetId")
                                      var feeAssetId: String?,
                                      @SerializedName("dApp")
                                      var dApp: String,
                                      @SerializedName("call")
                                      var call: Call?,
                                      @SerializedName("payment")
                                      var payment: Array<Payment>)
    : BaseTransactionResponse(type = BaseTransaction.SCRIPT_INVOCATION), Parcelable {

    @Parcelize
    class Payment(
            @SerializedName("feeAssetId")
            var amount: Long,
            @SerializedName("feeAssetId")
            var assetId: String?) : Parcelable

    @Parcelize
    class Call(@SerializedName("function")
               var function: String = "",
               @SerializedName("args")
               var args: Array<Args> = arrayOf()) : Parcelable

    @Parcelize
    class Args(@SerializedName("type")
               var type: String = "",
               @SerializedName("value")
               var value: Value = Value.VALUE_STRING) : Parcelable
}