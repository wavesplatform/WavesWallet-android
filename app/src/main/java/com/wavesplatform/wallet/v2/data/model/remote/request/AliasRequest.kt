package com.wavesplatform.wallet.v2.data.model.remote.request

import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.gson.annotations.SerializedName
import com.wavesplatform.wallet.v1.crypto.Base58
import com.wavesplatform.wallet.v1.crypto.CryptoProvider
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.util.arrayWithSize
import java.nio.charset.Charset


data class AliasRequest(
        @SerializedName("type") val type: Int = 10,
        @SerializedName("senderPublicKey") var senderPublicKey: String? = "",
        @SerializedName("fee") var fee: Long = 0,
        @SerializedName("timestamp") var timestamp: Long = 0,
        @SerializedName("signature") var signature: String? = null,
        @SerializedName("alias") var alias: String? = ""

) {

    fun toSignBytes(): ByteArray {
        return try {
            Bytes.concat(byteArrayOf(type.toByte()),
                    Base58.decode(senderPublicKey),
                    Bytes.concat(byteArrayOf(Constants.VERSION.toByte()),
                            byteArrayOf(Constants.ADDRESS_SCHEME.toByte()),
                            alias?.toByteArray(Charset.forName("UTF-8"))?.arrayWithSize()).arrayWithSize(),
                    Longs.toByteArray(fee),
                    Longs.toByteArray(timestamp))
        } catch (e: Exception) {
            Log.e("AliasRequest", "Couldn't create toSignBytes", e)
            ByteArray(0)
        }

    }

    fun sign(privateKey: ByteArray) {
        if (signature == null) {
            signature = Base58.encode(CryptoProvider.sign(privateKey, toSignBytes()))
        }
    }

}