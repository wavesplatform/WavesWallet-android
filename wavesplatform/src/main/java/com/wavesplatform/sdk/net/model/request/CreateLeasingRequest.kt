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
import com.wavesplatform.sdk.crypto.Base58
import com.wavesplatform.sdk.crypto.CryptoProvider
import com.wavesplatform.sdk.net.model.response.TransactionResponse
import com.wavesplatform.sdk.utils.*
import java.nio.charset.Charset

data class CreateLeasingRequest(
        @SerializedName("type") val type: Int = TransactionResponse.LEASE,
        @SerializedName("senderPublicKey") var senderPublicKey: String = "",
        @SerializedName("scheme") var scheme: String? = Wavesplatform.getEnvironment().scheme.toString(),
        @SerializedName("amount") var amount: Long = 0,
        @SerializedName("fee") var fee: Long = 0,
        @SerializedName("recipient") var recipient: String = "",
        @SerializedName("timestamp") var timestamp: Long = Wavesplatform.getEnvironment().getTime(),
        @SerializedName("version") var version: Int = WavesConstants.VERSION,
        @SerializedName("proofs") var proofs: MutableList<String?>? = null
) {

    fun toSignBytes(recipientIsAlias: Boolean): ByteArray {
        return try {
            Bytes.concat(byteArrayOf(type.toByte()),
                    byteArrayOf(WavesConstants.VERSION.toByte()),
                    byteArrayOf(0.toByte()),
                    Base58.decode(senderPublicKey),
                    resolveRecipientBytes(recipientIsAlias),
                    Longs.toByteArray(amount),
                    Longs.toByteArray(fee),
                    Longs.toByteArray(timestamp))
        } catch (e: Exception) {
            Log.e("CreateLeasingRequest", "Couldn't create toSignBytes", e)
            ByteArray(0)
        }
    }

    private fun resolveRecipientBytes(recipientIsAlias: Boolean): ByteArray? {
        return if (recipientIsAlias) {
            Bytes.concat(byteArrayOf(WavesConstants.VERSION.toByte()),
                    byteArrayOf(Wavesplatform.getEnvironment().scheme),
                    recipient.parseAlias().toByteArray(Charset.forName("UTF-8")).arrayWithSize())
        } else {
            Base58.decode(recipient)
        }
    }

    fun sign(privateKey: ByteArray, recipientIsAlias: Boolean) {
        proofs = mutableListOf(Base58.encode(CryptoProvider.sign(privateKey, toSignBytes(recipientIsAlias))))
    }
}