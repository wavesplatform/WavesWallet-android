package com.wavesplatform.sdk.model.response.node.transaction

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.model.request.node.BaseTransaction
import kotlinx.android.parcel.Parcelize

/**
 * See [com.wavesplatform.sdk.model.request.node.SponsorshipTransaction]
 */
@Parcelize
class SponsorshipTransactionResponse(var assetId: String,
                                     @SerializedName("minSponsoredAssetFee")
                                     var minSponsoredAssetFee: Long)
    : BaseTransactionResponse(type = BaseTransaction.SPONSORSHIP), Parcelable