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
import com.wavesplatform.sdk.utils.arrayWithSize
import com.wavesplatform.sdk.utils.isAlias
import com.wavesplatform.sdk.utils.parseAlias
import java.nio.charset.Charset

class CreateLeasingTransaction(
        @SerializedName("recipient") var recipient: String,
        @SerializedName("amount") var amount: Long)
    : BaseTransaction(CREATE_LEASING) {

    @SerializedName("scheme")
    var scheme: String = WavesPlatform.getEnvironment().scheme.toString()

    override fun toBytes(): ByteArray {
        return try {
            Bytes.concat(byteArrayOf(type.toByte()),
                    byteArrayOf(version.toByte()),
                    byteArrayOf(0.toByte()),
                    Base58.decode(senderPublicKey),
                    resolveRecipientBytes(recipient.isAlias()),
                    Longs.toByteArray(amount),
                    Longs.toByteArray(fee),
                    Longs.toByteArray(timestamp))
        } catch (e: Exception) {
            Log.e("Sign", "Can't create bytes for sign in CreateLeasing Transaction", e)
            ByteArray(0)
        }
    }

    private fun resolveRecipientBytes(recipientIsAlias: Boolean): ByteArray? {
        return if (recipientIsAlias) {
            Bytes.concat(byteArrayOf(version.toByte()),
                    byteArrayOf(scheme.toByte()),
                    recipient.parseAlias().toByteArray(
                            Charset.forName("UTF-8")).arrayWithSize())
        } else {
            Base58.decode(recipient)
        }
    }
}