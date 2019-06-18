package com.wavesplatform.sdk.model.request.node

import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.WavesPlatform
import com.wavesplatform.sdk.crypto.Base58

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
                    data, // todo check data size
                    Longs.toByteArray(timestamp),
                    Longs.toByteArray(fee))
        } catch (e: Exception) {
            Log.e("Sign", "Can't create bytes for sign in Data Transaction", e)
            ByteArray(0)
        }
    }
}