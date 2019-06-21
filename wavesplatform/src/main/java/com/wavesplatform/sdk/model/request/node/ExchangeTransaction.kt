package com.wavesplatform.sdk.model.request.node

import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.WavesPlatform
import com.wavesplatform.sdk.crypto.Base58

class ExchangeTransaction(
        @SerializedName("data") var data: ByteArray)
    : BaseTransaction(EXCHANGE) {

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
            Log.e("Sign", "Can't create bytes for sign in Exchange Transaction", e)
            ByteArray(0)
        }
    }
}