package com.wavesplatform.wallet.v2.data.model.db

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.net.model.response.AssetInfo
import com.wavesplatform.sdk.net.model.response.Order
import com.wavesplatform.sdk.net.model.response.Payment
import com.wavesplatform.sdk.utils.notNull
import io.realm.RealmModel
import io.realm.annotations.RealmClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@RealmClass
open class PaymentDb(
        @SerializedName("amount")
        var amount: Long = 0,
        @SerializedName("assetId")
        var assetId: String? = null,
        var asset: AssetInfo? = AssetInfo()
) : RealmModel, Parcelable {

    constructor(order: Payment?) : this() {
        order.notNull {
            this.amount = it.amount
            this.assetId = it.assetId
            this.asset = it.asset
        }
    }

    fun convertFromDb(): Payment {
        return Payment(
                amount = amount,
                assetId = assetId,
                asset = asset)
    }
}