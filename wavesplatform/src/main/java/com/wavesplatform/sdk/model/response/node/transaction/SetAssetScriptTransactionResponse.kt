package com.wavesplatform.sdk.model.response.node.transaction

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.model.request.node.BaseTransaction
import com.wavesplatform.sdk.model.request.node.SetAssetScriptTransaction
import kotlinx.android.parcel.Parcelize

/**
 * See [com.wavesplatform.sdk.model.request.node.SetAssetScriptTransaction]
 */
@Parcelize
class SetAssetScriptTransactionResponse(@SerializedName("assetId")
                                     val assetId: String = "",
                                        @SerializedName("script")
                                     val script: String = "")
    : BaseTransactionResponse(type = BaseTransaction.ASSET_SCRIPT), Parcelable