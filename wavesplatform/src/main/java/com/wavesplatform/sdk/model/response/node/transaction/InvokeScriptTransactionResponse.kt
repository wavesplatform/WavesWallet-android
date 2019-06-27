package com.wavesplatform.sdk.model.response.node.transaction

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.model.request.node.BaseTransaction
import com.wavesplatform.sdk.model.request.node.InvokeScriptTransaction
import kotlinx.android.parcel.Parcelize

/**
 * See [com.wavesplatform.sdk.model.request.node.InvokeScriptTransaction]
 */
@Parcelize
class InvokeScriptTransactionResponse(@SerializedName("feeAssetId")
                                          var feeAssetId: String?,
                                      @SerializedName("dApp")
                                          var dApp: String,
                                      @SerializedName("call")
                                          var call: InvokeScriptTransaction.Call?,
                                      @SerializedName("payment")
                                          var payment: Array<InvokeScriptTransaction.Payment>)
    : BaseTransactionResponse(type = BaseTransaction.SCRIPT_INVOCATION), Parcelable