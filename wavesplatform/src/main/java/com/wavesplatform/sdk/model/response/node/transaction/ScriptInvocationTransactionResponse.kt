package com.wavesplatform.sdk.model.response.node.transaction

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.model.request.node.BaseTransaction
import kotlinx.android.parcel.Parcelize

@Parcelize
class ScriptInvocationTransactionResponse(@SerializedName("feeAssetId")
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
    class Call(
            /**
             * Function name
             */
            @SerializedName("function") var function: String = "",
            /**
             * Array of arguments
             */
            @SerializedName("args") var args: Array<Args> = arrayOf()) : Parcelable

    @Parcelize
    class Args(
            /**
             * Type can be of four types - integer(0), boolean(1), binary array(2) and string(3).
             */
            @SerializedName("type") var type: String = "",
            /**
             * Value can of four types - integer(0), boolean(1), binary array(2) and string(3).
             * And it depends on type.
             */
            @SerializedName("value") var value: String = "") : Parcelable
}