package com.wavesplatform.sdk.model.response.node.transaction

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.model.request.node.BaseTransaction
import kotlinx.android.parcel.Parcelize

/**
 * See [com.wavesplatform.sdk.model.request.node.SetScriptTransaction]
 */
@Parcelize
class SetScriptTransactionResponse(@SerializedName("script")
                                var script: String?)
    : BaseTransactionResponse(type = BaseTransaction.ADDRESS_SCRIPT), Parcelable