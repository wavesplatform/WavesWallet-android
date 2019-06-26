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
import com.wavesplatform.sdk.utils.arrayWithSize
import com.wavesplatform.sdk.utils.isAlias
import com.wavesplatform.sdk.utils.parseAlias
import java.nio.charset.Charset

/**
 * The transaction leases amount of Waves to node operator.
 * it can be address or alias by Proof-of-Stake consensus. It will perform at non-node address.
 * You always can reverse the any leased amount by [LeaseCancelTransaction]
 */
class LeaseTransaction(
        /**
         * Address or alias of Waves blockchain to lease
         */
        @SerializedName("recipient") var recipient: String,
        /**
         * Amount to lease of Waves in satoshi
         */
        @SerializedName("amount") var amount: Long)
    : BaseTransaction(CREATE_LEASING) {

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
            Log.e("Sign", "Can't create bytes for sign in Create Leasing Transaction", e)
            ByteArray(0)
        }
    }

    private fun resolveRecipientBytes(recipientIsAlias: Boolean): ByteArray? {
        return if (recipientIsAlias) {
            Bytes.concat(byteArrayOf(version.toByte()),
                    byteArrayOf(chainId),
                    recipient.parseAlias().toByteArray(
                            Charset.forName("UTF-8")).arrayWithSize())
        } else {
            Base58.decode(recipient)
        }
    }
}