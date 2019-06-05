package com.wavesplatform.sdk.model.transaction.node

import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.WavesPlatform
import com.wavesplatform.sdk.crypto.Base58

// todo check Not completed!
class DataTransaction(
        @SerializedName("data") var data: ByteArray)
    : BaseTransaction(CREATE_LEASING) {

    @SerializedName("scheme")
    var scheme: String = WavesPlatform.getEnvironment().scheme.toString()

    override fun toBytes(): ByteArray {
        return try {
            Bytes.concat(byteArrayOf(type.toByte()),
                    byteArrayOf(version.toByte()),
                    Base58.decode(senderPublicKey),
                    data,
                    Longs.toByteArray(timestamp),
                    Longs.toByteArray(fee))
        } catch (e: Exception) {
            Log.e("Sign", "Can't create bytes for sign in CreateLeasing Transaction", e)
            ByteArray(0)
        }
    }
}