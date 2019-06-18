package com.wavesplatform.sdk.model.request.node

import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.WavesPlatform
import com.wavesplatform.sdk.crypto.Base58

class SetScriptTransaction(@SerializedName("script") var script: String)
    : BaseTransaction(ADDRESS_SCRIPT) {

    override fun toBytes(): ByteArray {
        return try {
            Bytes.concat(byteArrayOf(type.toByte()),
                    byteArrayOf(version.toByte()),
                    byteArrayOf(WavesPlatform.getEnvironment().scheme),
                    Base58.decode(senderPublicKey),
                    Base58.decode(script),
                    Longs.toByteArray(fee),
                    Longs.toByteArray(timestamp))
            // todo check
        } catch (e: Exception) {
            Log.e("Sign", "Can't create bytes for sign in SetScript Transaction", e)
            ByteArray(0)
        }
    }
}