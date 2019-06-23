package com.wavesplatform.sdk.model.request.node

import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.WavesPlatform
import com.wavesplatform.sdk.crypto.Base58

/**
 * Script transactions (set script to account) allow you to extend the available functionality
 * of the standard Waves application. One of the uses of script transaction
 * is creating a multi-signature wallet. Script can be developed
 * with [Waves Ride IDE]({https://ide.wavesplatform.com/)
 *
 * You can also cancel the active script transaction. You must send transaction with null script.
 *
 * Before you start, please keep in mind.
 * We do not recommend you submit script transactions unless you are an experienced user.
 *
 * Errors can lead to permanent loss of access to your account.
 */
class ScriptTransaction(
        /**
         * Base64 binary string with Waves Ride script, starts with "base64:"
         */
        @SerializedName("script") var script: String)
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