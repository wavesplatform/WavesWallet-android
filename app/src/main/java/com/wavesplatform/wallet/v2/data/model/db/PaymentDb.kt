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
        @SerializedName("asset")
        var asset: AssetInfoDb? = AssetInfoDb()
) : RealmModel, Parcelable {

    constructor(payment: Payment?) : this() {
        payment.notNull {
            this.amount = it.amount
            this.assetId = it.assetId
            this.asset = AssetInfoDb(it.asset)
        }
    }

    fun convertFromDb(): Payment {
        return Payment(
                amount = amount,
                assetId = assetId,
                asset = asset?.convertFromDb())
    }
}