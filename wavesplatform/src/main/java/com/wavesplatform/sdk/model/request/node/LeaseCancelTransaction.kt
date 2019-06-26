/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.model.request.node

import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.crypto.Base58

/**
 * The cancel leasing transaction reverse [LeaseTransaction].
 * Lease cancel transaction is used to to cancel
 * and discontinue the WAVES leasing process to a Waves node.
 */
class LeaseCancelTransaction(
        /**
         * Id of Leasing Transaction to cancel
         */
        @SerializedName("leaseId") var leaseId: String = "")
    : BaseTransaction(CANCEL_LEASING) {

    override fun toBytes(): ByteArray {
        return try {
            Bytes.concat(byteArrayOf(type.toByte()),
                    byteArrayOf(version.toByte()),
                    byteArrayOf(chainId),
                    Base58.decode(senderPublicKey),
                    Longs.toByteArray(fee),
                    Longs.toByteArray(timestamp),
                    Base58.decode(leaseId)
            )
        } catch (e: Exception) {
            Log.e("Sign", "Can't create bytes for sign in Cancel Leasing Transaction", e)
            ByteArray(0)
        }
    }
}