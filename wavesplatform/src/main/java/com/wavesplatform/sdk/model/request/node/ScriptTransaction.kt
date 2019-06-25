package com.wavesplatform.sdk.model.request.node

import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.common.primitives.Shorts
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.WavesPlatform
import com.wavesplatform.sdk.crypto.Base58
import com.wavesplatform.sdk.crypto.WavesCrypto
import com.wavesplatform.sdk.utils.arrayWithSize

/**
 * Script transactions (set script to account) allow you to extend the available functionality
 * of the standard Waves application. One of the uses of script transaction
 * is creating a multi-signature wallet. Script can be developed
 * with [Waves Ride IDE]({https://ide.wavesplatform.com/)
 *
 * An account with the attached script is called a smart account.
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
        @SerializedName("script") var script: String? = null)
    : BaseTransaction(ADDRESS_SCRIPT) {

    override fun toBytes(): ByteArray {
        return try {

            val scriptVersion = if (script.isNullOrEmpty()) {
                byteArrayOf(0)
            } else {
                byteArrayOf(IssueTransaction.SET_SCRIPT_LANG_VERSION)
            }

            val scriptBytes = if (script.isNullOrEmpty()) {
                byteArrayOf()
            } else {
                WavesCrypto.base64decode(script!!.replace("base64:", ""))
            }

            Bytes.concat(byteArrayOf(type.toByte()),
                    byteArrayOf(version.toByte()),
                    byteArrayOf(chainId),
                    Base58.decode(senderPublicKey),
                    scriptVersion,
                    scriptBytes.arrayWithSize(),
                    Longs.toByteArray(fee),
                    Longs.toByteArray(timestamp))

        } catch (e: Exception) {
            Log.e("Sign", "Can't create bytes for sign in SetScript Transaction", e)
            ByteArray(0)
        }
    }

    override fun sign(seed: String): String {
        version = 1
        if (fee == 0L) {
            fee = 100000000L
        }
        signature = super.sign(seed)
        return signature ?: ""
    }

    companion object {
        const val WAVES_SET_SCRIPT_MIN_FEE = 1000000L
    }
}