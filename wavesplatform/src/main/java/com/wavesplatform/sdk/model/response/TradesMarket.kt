package com.wavesplatform.sdk.model.response

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
class TradesMarket : Parcelable {
    var timestamp: String? = Date().time.toString()
    var id: String? = "0.0"
    var confirmed = false
    var type: String? = null
    var price: String? = "0.0"
    var amount: String? = "0.0"
    var buyer: String? = null
    var seller: String? = null
    var matcher: String? = null

}
