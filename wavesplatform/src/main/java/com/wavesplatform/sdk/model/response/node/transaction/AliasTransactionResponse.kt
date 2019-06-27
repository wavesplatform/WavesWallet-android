package com.wavesplatform.sdk.model.response.node.transaction

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.model.request.node.BaseTransaction
import com.wavesplatform.sdk.model.request.node.SetAssetScriptTransaction
import kotlinx.android.parcel.Parcelize

/**
 * See [com.wavesplatform.sdk.model.request.node.AliasTransaction]
 */
@Parcelize
class AliasTransactionResponse(@SerializedName("alias")
                               var alias: String? = "",
                               @SerializedName("address")
                               var address: String? = "",
                               var own: Boolean = false)
    : BaseTransactionResponse(type = BaseTransaction.CREATE_ALIAS), Parcelable