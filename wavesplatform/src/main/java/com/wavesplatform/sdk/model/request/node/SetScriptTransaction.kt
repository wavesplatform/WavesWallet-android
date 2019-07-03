package com.wavesplatform.sdk.model.request.node

import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.crypto.Base58
import com.wavesplatform.sdk.utils.scriptBytes

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
 * !!! Errors can lead to permanent loss of access to your account.
 *
 * Set script transaction is used to setup an smart account,
 * The account needs to issue Set script transaction which contains the predicate.
 * Upon success, every outgoing transaction will be validated not by the default mechanism
 * of signature validation, but according to the predicate logic.
 *
 * Account script can be changed or cleared if the script installed allows
 * the new set script transaction to process. The set script transaction can be changed
 * by another set script transaction call unless it’s prohibited by a previous set script.
 */
class SetScriptTransaction(
    /**
     * Base64 binary string with Waves Ride script
     * Null for cancel script. Watch out about commissions on set and cancel script
     * You can use "base64:compiledScriptStringInBase64" and just "compiledScriptStringInBase64".
     * Can't be empty string
     */
    @SerializedName("script") var script: String? = null
) : BaseTransaction(ADDRESS_SCRIPT) {

    override fun toBytes(): ByteArray {
        return try {
            Bytes.concat(
                byteArrayOf(type.toByte()),
                byteArrayOf(version.toByte()),
                byteArrayOf(chainId),
                Base58.decode(senderPublicKey),
                scriptBytes(script),
                Longs.toByteArray(fee),
                Longs.toByteArray(timestamp)
            )

        } catch (e: Exception) {
            Log.e("Sign", "Can't create bytes for sign in SetScript Transaction", e)
            ByteArray(0)
        }
    }

    override fun sign(seed: String): String {
        version = 1
        signature = super.sign(seed)
        return signature ?: ""
    }

    companion object {
        const val WAVES_SET_SCRIPT_MIN_FEE = 1000000L
        const val WAVES_CANCEL_SET_SCRIPT_MIN_FEE = 1000000L
    }
}