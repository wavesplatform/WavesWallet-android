/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.net.model.request

import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.Wavesplatform
import com.wavesplatform.sdk.utils.Constants
import com.wavesplatform.sdk.crypto.Base58
import com.wavesplatform.sdk.crypto.CryptoProvider
import com.wavesplatform.sdk.net.model.response.TransactionResponse
import com.wavesplatform.sdk.utils.arrayWithSize
import java.nio.charset.Charset

data class AliasRequest(
        @SerializedName("type") val type: Int = TransactionResponse.CREATE_ALIAS,
        @SerializedName("senderPublicKey") var senderPublicKey: String = "",
        @SerializedName("fee") var fee: Long = 0,
        @SerializedName("timestamp") var timestamp: Long = Wavesplatform.getServers().getTime(),
        @SerializedName("version") var version: Int = Constants.VERSION,
        @SerializedName("proofs") var proofs: MutableList<String?>? = null,
        @SerializedName("alias") var alias: String? = ""
) {

    fun toSignBytes(): ByteArray {
        return try {
            Bytes.concat(byteArrayOf(type.toByte()),
                    byteArrayOf(Wavesplatform.getServers().netCode),
                    Base58.decode(senderPublicKey),
                    Bytes.concat(byteArrayOf(Constants.VERSION.toByte()),
                            byteArrayOf(Wavesplatform.getServers().netCode),
                            alias?.toByteArray(Charset.forName("UTF-8"))?.arrayWithSize())
                            .arrayWithSize(),
                    Longs.toByteArray(fee),
                    Longs.toByteArray(timestamp))
        } catch (e: Exception) {
            Log.e("AliasRequest", "Couldn't create toSignBytes", e)
            ByteArray(0)
        }
    }

    fun sign(privateKey: ByteArray) {
        proofs = mutableListOf(Base58.encode(CryptoProvider.sign(privateKey, toSignBytes())))
    }
}