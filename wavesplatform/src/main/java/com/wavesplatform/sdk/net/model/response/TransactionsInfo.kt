/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.net.model.response

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class TransactionsInfo(var assetId: String, var name: String, var decimals: Int) : Parcelable {
    var type: Int = 0
    var id: String? = null
    var sender: String? = null
    var senderPublicKey: String? = null
    var fee: Long? = null
    var timestamp: String? = null
    var signature: String? = null
    var description: String? = null
    var quantity: Long? = null
    var reissuable: Boolean = false
    var height: Long? = null
}
