package com.wavesplatform.sdk.model.response.node.transaction

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.model.request.node.BaseTransaction
import kotlinx.android.parcel.Parcelize

@Parcelize
class SetAssetScriptTransactionResponse(@SerializedName("assetId")
                                     val assetId: String = "",
                                        @SerializedName("script")
                                     val script: String = "")
    : BaseTransactionResponse(type = BaseTransaction.ASSET_SCRIPT), Parcelable