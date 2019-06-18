/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.model.request.node

import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.WavesPlatform
import com.wavesplatform.sdk.crypto.Base58

class CancelLeasingTransaction(@SerializedName("leaseId") var leaseId: String = "")
    : BaseTransaction(CANCEL_LEASING) {

    @SerializedName("chainId")
    var scheme: Byte = WavesPlatform.getEnvironment().scheme

    override fun toBytes(): ByteArray {
        return try {
            Bytes.concat(byteArrayOf(type.toByte()),
                    byteArrayOf(version.toByte()),
                    byteArrayOf(scheme),
                    Base58.decode(senderPublicKey),
                    Longs.toByteArray(fee),
                    Longs.toByteArray(timestamp),
                    Base58.decode(leaseId)
            )
        } catch (e: Exception) {
            Log.e("Sign", "Can't create bytes for sign in CancelLeasing Transaction", e)
            ByteArray(0)
        }
    }
}